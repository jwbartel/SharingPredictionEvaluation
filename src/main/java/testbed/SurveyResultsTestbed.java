package testbed;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import metrics.response.time.survey.SurveyDataParser;
import metrics.response.time.survey.SurveyResults;
import metrics.response.time.survey.QuestionAnswer.QuestionAnswerPair;
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
			
		}

	}

}
