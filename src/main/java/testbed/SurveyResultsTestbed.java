package testbed;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import metrics.response.time.survey.QuestionAnswer;
import metrics.response.time.survey.QuestionAnswer.OtherValue;
import metrics.response.time.survey.QuestionAnswer.QuestionAnswerPair;
import metrics.response.time.survey.QuestionAnswer.SelectedItem;
import metrics.response.time.survey.QuestionAnswer.SurveyElaborationAnswer;
import metrics.response.time.survey.SurveyDataParser;
import metrics.response.time.survey.SurveyResults;
import metrics.response.time.survey.SurveyResults.QuestionResults;
import metrics.response.time.survey.SurveyResults.ResponseTimeQuestionResults;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import testbed.dataset.actions.messages.email.ResponseTimeStudyDataSet;

public class SurveyResultsTestbed {

	Collection<ResponseTimeStudyDataSet> datasets;

	public SurveyResultsTestbed(
			Collection<ResponseTimeStudyDataSet> datasets) {
		
		this.datasets = datasets;
	}
	
	
	private void printResponseTimesResults(ResponseTimeStudyDataSet dataset, SurveyResults results) throws IOException {
		
		File outputFile = dataset.getSurveyResponseErrorResults();
		String header = "user,response time,"
				+ "Would it have helped to know that a response was coming?,"
				+ "Would it have helped to know when the response would occur?,"
				+ "1 minute error acceptable,"
				+ "5 minutes error acceptable,"
				+ "30 minutes error acceptable,"
				+ "1 hour error acceptable,"
				+ "1 day error acceptable,"
				+ "1 week error acceptable," ;
		FileUtils.write(outputFile, header);
		
		ResponseTimeQuestionResults responseTimeResults = results.getResponseTimeResults();
		Map<Integer, Map<Long, List<QuestionAnswerPair>>> usersAnswers = responseTimeResults.getAnswers();
		for (Integer user : usersAnswers.keySet()) {
			Map<Long, List<QuestionAnswerPair>> responseTimeVals = usersAnswers.get(user);
			for (Long responseTime : responseTimeVals.keySet()) {
				
				List<QuestionAnswerPair> answers = responseTimeVals.get(responseTime);
				
				String line = user + "," + responseTime + ",";
				for(QuestionAnswerPair answer : answers) {
					if (answer.question.equals("Would it have helped to know that a response was coming?")) {
						line += answer.value + ",";
						break;
					}
				}
				for(QuestionAnswerPair answer : answers) {
					if (answer.question.equals("Would it have helped to know when the response would occur?")) {
						line += answer.value + ",";
						break;
					}
				}
				for(QuestionAnswerPair answer : answers) {
					if (answer.question.equals("Would the response time still be helpful if it were off by 1 minute")) {
						line += answer.value + ",";
						break;
					}
				}
				for(QuestionAnswerPair answer : answers) {
					if (answer.question.equals("Would the response time still be helpful if it were off by 5 minutes")) {
						line += answer.value + ",";
						break;
					}
				}
				for(QuestionAnswerPair answer : answers) {
					if (answer.question.equals("Would the response time still be helpful if it were off by 30 minutes")) {
						line += answer.value + ",";
						break;
					}
				}
				for(QuestionAnswerPair answer : answers) {
					if (answer.question.equals("Would the response time still be helpful if it were off by 1 hour")) {
						line += answer.value + ",";
						break;
					}
				}
				for(QuestionAnswerPair answer : answers) {
					if (answer.question.equals("Would the response time still be helpful if it were off by 1 day")) {
						line += answer.value + ",";
						break;
					}
				}
				for(QuestionAnswerPair answer : answers) {
					if (answer.question.equals("Would the response time still be helpful if it were off by 1 week")) {
						line += answer.value + ",";
						break;
					}
				}
				line += "\n";
				
				FileUtils.write(outputFile, line, true);
			}
		}
		
	}
	
	private void printGeneralQuestionsResults(ResponseTimeStudyDataSet dataset,
			SurveyResults results) throws IOException {
		File outputFile = dataset.getSurveyGeneralQuestionResults();
		String header = ",number of answers, percent selected\n";
		FileUtils.write(outputFile, header);
		
		printQuestionResults(outputFile, results.getDeadlineResults());
		printQuestionResults(outputFile, results.getReactionResults());
		printQuestionResults(outputFile, results.getReactionRemoveResults());
		printQuestionResults(outputFile, results.getReactionKeepResults());
		printQuestionResults(outputFile, results.getReactionFindResults());
		printQuestionResults(outputFile, results.getOtherResults());
		printQuestionResults(outputFile, results.getSelfResults());
		printQuestionResults(outputFile, results.getHarmResults());
	}

	private void printQuestionResults(File outputFile,
			QuestionResults questionResults) throws IOException {

		int totalAnswers = questionResults.getTotalAnswerCount(); 
		FileUtils.write(outputFile, questionResults.questionText.replaceAll(",","_") + "," + totalAnswers + " answers \n", true);
		
		Map<String, Integer> selectionCounts = new TreeMap<>();
		
		Map<Integer, List<QuestionAnswer>> usersAnswers = questionResults.getAnswers();
		for (Integer user : usersAnswers.keySet()) {
			List<QuestionAnswer> answers = usersAnswers.get(user);
			for (QuestionAnswer answer : answers) {
				String value = null;
				if (answer instanceof SelectedItem) {
					value = ((SelectedItem) answer).value;
				} else if (answer instanceof OtherValue){
					value = "other";
				}
				
				if (value != null) {
					Integer count = selectionCounts.get(value);
					count = (count == null)? 1 : count + 1;
					selectionCounts.put(value, count);
				}
			}
		}
		
		for (String selection : selectionCounts.keySet()) {
			int count = selectionCounts.get(selection);
			selection = selection.replaceAll(",", "_");
			double percentage = ((double) count)/totalAnswers;
			FileUtils.write(outputFile, selection + "," + count + "," + percentage + "\n", true);
		}
		FileUtils.write(outputFile, "\n", true);
	}
	
	private void printShortAnswersResults(ResponseTimeStudyDataSet dataset,
			SurveyResults results) throws IOException {
		
		File outputFile = dataset.getSurveyShortAnswerResults();
		FileUtils.write(outputFile, "");
		
		printShortAnswerResults(outputFile, results.getDeadlineResults());
		printShortAnswerResults(outputFile, results.getReactionResults());
		printShortAnswerResults(outputFile, results.getReactionRemoveResults());
		printShortAnswerResults(outputFile, results.getReactionKeepResults());
		printShortAnswerResults(outputFile, results.getReactionFindResults());
		printShortAnswerResults(outputFile, results.getOtherResults());
		printShortAnswerResults(outputFile, results.getSelfResults());
		printShortAnswerResults(outputFile, results.getHarmResults());
		printShortAnswerResults(outputFile, results.getCommentsResults());
	}
	
	private void printShortAnswerResults(File outputFile,
			QuestionResults questionResults) throws IOException {
		
		FileUtils.write(outputFile, "========================\n", true);
		FileUtils.write(outputFile, questionResults.questionText + "\n", true);
		FileUtils.write(outputFile, "========================\n", true);
		
		Map<Integer, List<QuestionAnswer>> usersAnswers = questionResults.getAnswers();
		
		FileUtils.write(outputFile, "Other (Please Specify)\n", true);
		FileUtils.write(outputFile, "------------------------\n", true);
		for (Integer user : usersAnswers.keySet()) {
			List<QuestionAnswer> answers = usersAnswers.get(user);
			for (QuestionAnswer answer : answers) {
				if (answer instanceof OtherValue && ((OtherValue) answer).value.trim().length() > 0){
					String outputStr = "\tParticipant " + user + ": ";
					outputStr += ((OtherValue) answer).value;
					outputStr += "\n";
					FileUtils.write(outputFile, outputStr, true);
					break;
				}
			}
		}
		
		FileUtils.write(outputFile, "Please elaborate on your answer (e.g. give details about your selected option(s) or reasons why you did not select any of the above options)\n", true);
		FileUtils.write(outputFile, "------------------------\n", true);
		for (Integer user : usersAnswers.keySet()) {
			List<QuestionAnswer> answers = usersAnswers.get(user);
			for (QuestionAnswer answer : answers) {
				if (answer instanceof SurveyElaborationAnswer
						&& ((SurveyElaborationAnswer) answer).value.trim()
								.length() > 0) {
					String outputStr = "\tParticipant " + user + ": ";
					outputStr += ((SurveyElaborationAnswer) answer).value;
					outputStr += "\n";
					FileUtils.write(outputFile, outputStr, true);
					break;
				}
			}
		}
		
		FileUtils.write(outputFile, "\n\n\n", true);
	}
	
	public void runTestbed() throws Exception {

		for(ResponseTimeStudyDataSet dataset : datasets) {
			
			String[] accountIds = dataset.getPrivateAccountIds();
			SurveyResults results = new SurveyResults();
			for (String accountId : accountIds) {
				
				File surveyQuestionFile = dataset.getSurveyQuestionsFile(accountId);
				File surveyResultsFile = dataset.getSurveyResultsFile(accountId);
				
				Map<Integer,Long> surveyedResponseTimes = SurveyDataParser.getSurveyedResponseTimes(surveyQuestionFile);
				JSONObject surveyResults = SurveyDataParser.parseSurveyAnswers(surveyResultsFile);
				
				results.addUserResults(accountId, surveyedResponseTimes, surveyResults);
				
			}
			printResponseTimesResults(dataset, results);
			printGeneralQuestionsResults(dataset, results);
			printShortAnswersResults(dataset, results);
		}

	}

}
