package metrics.response.time.survey;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import metrics.response.time.survey.QuestionAnswer.OtherValue;
import metrics.response.time.survey.QuestionAnswer.QuestionAnswerPair;
import metrics.response.time.survey.QuestionAnswer.SelectedItem;
import metrics.response.time.survey.QuestionAnswer.SurveyElaborationAnswer;

import org.json.JSONObject;

public class SurveyResults {

	public static class ResponseTimeQuestionResults {

		Map<Integer, Map<Long, List<QuestionAnswerPair>>> usersAnswers = new TreeMap<>();

		public void addUser(Integer user) {
			if (!usersAnswers.containsKey(user)) {
				usersAnswers.put(user, new TreeMap<Long, List<QuestionAnswerPair>>());
			}
		}
		
		public void addUserResponseTime(Integer user, Long time) {

			if (!usersAnswers.containsKey(user)) {
				addUser(user);
			}
			if (!usersAnswers.get(user).containsKey(time)) {
				usersAnswers.get(user).put(time, new ArrayList<QuestionAnswerPair>());
			}
		}

		public void addAnswer(Integer user, Long time, QuestionAnswerPair answer) {

			addUserResponseTime(user, time);
			List<QuestionAnswerPair> userAnswers = usersAnswers.get(user).get(time);
			if (!userAnswers.contains(answer)) {
				userAnswers.add(answer);
			}
		}
		
		public Map<Integer, Map<Long, List<QuestionAnswerPair>>> getAnswers() {
			return usersAnswers;
		}
	}

	public static class QuestionResults {

		final String questionText;
		Map<Integer, List<QuestionAnswer>> usersAnswers = new TreeMap<>();

		public QuestionResults(String questionText) {
			this.questionText = questionText;
		}

		public void addUser(Integer user) {
			if (!usersAnswers.containsKey(user)) {
				usersAnswers.put(user, new ArrayList<QuestionAnswer>());
			}
		}

		public void addAnswer(Integer user, QuestionAnswer answer) {

			if (!usersAnswers.containsKey(user)) {
				addUser(user);
			}
			List<QuestionAnswer> userAnswers = usersAnswers.get(user);
			if (!userAnswers.contains(answer)) {
				userAnswers.add(answer);
			}
		}
	}

	private final ResponseTimeQuestionResults responseTimeResults = new ResponseTimeQuestionResults();

	private final QuestionResults deadlineResults;
	private final QuestionResults reactionResults;
	private final QuestionResults reactionRemoveResults;
	private final QuestionResults reactionKeepResults;
	private final QuestionResults reactionFindResults;
	private final QuestionResults otherResults;
	private final QuestionResults selfResults;
	private final QuestionResults harmResults;
	private final QuestionResults commentsResults;

	private Map<String, Integer> surveyToUserId = new TreeMap<>();

	public SurveyResults() {

		deadlineResults = new QuestionResults(
				"Have you been in any of following situation(s) where you needed a response to an email or a post on an online forum (such as Piazza or Stack Overflow) quickly enough to meet some deadline?");
		reactionResults = new QuestionResults(
				"In the above situation(s) that you selected, suppose that as you were composing your message or post we predicted if and when you would receive a response (with a small chance of error). Based on that prediction, assume you determined that the response would not arrive quickly enough for you to meet your deadline. Would you do any of the following?");
		reactionRemoveResults = new QuestionResults(
				"Why would you remove them? (only shown if \"I would remove one or more of the already listed recipients before sending\" is selected");
		reactionKeepResults = new QuestionResults(
				"Why would you keep them? (only shown if \"I would keep one or more of the original recipients\" is selected");
		reactionFindResults = new QuestionResults(
				"How would you find your answer? (only shown if \"I would use means other than sending an email or posting on forums\" is selected");
		otherResults = new QuestionResults(
				"There may be reasons other than trying to meet a deadline where you care about if and when you will receive a response. Please select any other reasons why you in particular would want to know if and when you will receive a response.");
		selfResults = new QuestionResults(
				"Suppose we were able to predict (with a small chance of error) how long it would take you to respond to a particular post or message and notify you when you took longer than normal to respond. Which, if any, of the following situations have you experienced where that would helpful?");
		harmResults = new QuestionResults(
				"Based on your experience, how might predicting if and when responses occur for senders or receivers be useless or harmful?");
		commentsResults = new QuestionResults(
				"If you have any additional comments or feedback, please type it in the box below.");
	}
	
	public ResponseTimeQuestionResults getResponseTimeResults() {
		return responseTimeResults;
	}

	private Integer getUserId(String surveyId) {

		String userString = surveyId;
		if (userString.contains("_")) {
			userString = surveyId.substring(0,surveyId.indexOf('_'));
		}

		if (surveyToUserId.containsKey(userString)) {
			return surveyToUserId.get(userString);
		}

		Integer userId = surveyToUserId.size() + 1;
		surveyToUserId.put(userString, userId);
		return userId;
	}
	
	private void addResponseTimeResults(Integer user, Integer questionId, Long responseTime, JSONObject surveyAnswers) {
		
		JSONObject threadResults = surveyAnswers.getJSONObject(questionId.toString());
		
		if (threadResults.getString("notAnswered").equals("true")) {
			return;
		}
		
		responseTimeResults.addAnswer(user, responseTime,
				new QuestionAnswerPair(
						"Would it have helped to know that a response was coming?",
						threadResults.getString("1")));
		
		responseTimeResults.addAnswer(user, responseTime,
				new QuestionAnswerPair(
						"Would it have helped to know when the response would occur?",
						threadResults.getString("2")));
		
		responseTimeResults.addAnswer(user, responseTime,
				new QuestionAnswerPair(
						"Would the response time still be helpful if it were off by 1 minute",
						threadResults.getString("3")));
		
		responseTimeResults.addAnswer(user, responseTime,
				new QuestionAnswerPair(
						"Would the response time still be helpful if it were off by 5 minutes",
						threadResults.getString("4")));
		
		responseTimeResults.addAnswer(user, responseTime,
				new QuestionAnswerPair(
						"Would the response time still be helpful if it were off by 30 minutes",
						threadResults.getString("5")));
		
		responseTimeResults.addAnswer(user, responseTime,
				new QuestionAnswerPair(
						"Would the response time still be helpful if it were off by 1 hour",
						threadResults.getString("6")));
		
		responseTimeResults.addAnswer(user, responseTime,
				new QuestionAnswerPair(
						"Would the response time still be helpful if it were off by 1 day",
						threadResults.getString("7")));
		
		responseTimeResults.addAnswer(user, responseTime,
				new QuestionAnswerPair(
						"Would the response time still be helpful if it were off by 1 week",
						threadResults.getString("8")));
		
	}
	
	private boolean addDeadlineResults(Integer user, JSONObject surveyAnswers) {
		
		boolean foundDeadlineAnswer = false;
		JSONObject deadlineAnswers = surveyAnswers.getJSONObject("deadline");
		deadlineResults.addUser(user);
		if (deadlineAnswers.getString("meeting").equals("true")) {
			deadlineResults.addAnswer(user, new SelectedItem(
					"Coordinating with people about meeting later"));
			foundDeadlineAnswer = true;
		}
		if (deadlineAnswers.getString("clarifying").equals("true")) {
			deadlineResults
					.addAnswer(
							user,
							new SelectedItem(
									"Clarifying assignments or projects with professors, TAs, bosses, colleagues, coworkers, or others before they were due"));
			foundDeadlineAnswer = true;
		}
		if (deadlineAnswers.getString("collaborate").equals("true")) {
			deadlineResults
					.addAnswer(
							user,
							new SelectedItem(
									"Coordinating with colleagues, coworkers, or others about upcoming assignments or projects you are collaborating on"));
			foundDeadlineAnswer = true;
		}
		if (deadlineAnswers.getString("information").equals("true")) {
			deadlineResults
					.addAnswer(
							user,
							new SelectedItem(
									"Coordinating with colleagues, coworkers, or others about upcoming assignments or projects you are collaborating on"));
			foundDeadlineAnswer = true;
		}
		if (deadlineAnswers.getJSONObject("other").getString("checked")
				.equals("true")
				|| deadlineAnswers.getJSONObject("other").getString("val")
						.trim().length() > 0) {

			deadlineResults.addAnswer(user, new OtherValue(deadlineAnswers
					.getJSONObject("other").getString("val").trim()));
			foundDeadlineAnswer = true;
		}
		if (deadlineAnswers.getString("elaboration").trim().length() > 0) {
			deadlineResults.addAnswer(user, new SurveyElaborationAnswer(
					deadlineAnswers.getString("elaboration").trim()
				));
		}
		return foundDeadlineAnswer;
	}
	
	private void addReactionResults(Integer user, JSONObject surveyAnswers) {
		reactionResults.addUser(user);
		JSONObject reactionAnswers = surveyAnswers.getJSONObject("reaction");
		
		if (reactionAnswers.getJSONObject("remove").getString("checked").equals("true")) {
			reactionResults.addAnswer(user, new SelectedItem(
					"I would remove one or more of the already listed recipients before sending"));
			addReactionRemoveResults(user, surveyAnswers);
		}
		
		if (reactionAnswers.getJSONObject("keep").getString("checked").equals("true")) {
			reactionResults.addAnswer(user, new SelectedItem(
					"I would keep one or more of the original recipients"));
			addReactionKeepResults(user, surveyAnswers);
		}
		
		if (reactionAnswers.getString("add").equals("true")) {
			reactionResults.addAnswer(user, new SelectedItem(
					"I would send it to more people."));
		}
		
		if (reactionAnswers.getString("notSend").equals("true")) {
			reactionResults.addAnswer(user, new SelectedItem(
					"I would not send or post it."));
		}
		
		if (reactionAnswers.getString("change").equals("true")) {
			reactionResults.addAnswer(user, new SelectedItem(
					"If the message was an email, I would post it on a forum, and if it was a forum post, I would send it via email."));
		}
		
		if (reactionAnswers.getJSONObject("find").getString("checked").equals("true")) {
			reactionResults.addAnswer(user, new SelectedItem(
					"I would use means other than sending an email or posting on forums (e.g. searching Google, meeting someone n person, sending an IM) to find an answer."));
			addReactionFindResults(user, surveyAnswers);
		}
		if (reactionAnswers.getJSONObject("other").getString("checked").equals("true")
				|| reactionAnswers.getJSONObject("other").getString("val")
						.trim().length() > 0) {

			reactionResults.addAnswer(user, new OtherValue(
					reactionAnswers.getJSONObject("other").getString("val").trim()));
		}
		if (reactionAnswers.getString("elaboration").trim().length() > 0) {
			reactionResults.addAnswer(user, new SurveyElaborationAnswer(
					reactionAnswers.getString("elaboration").trim()
				));
		}
	}
	
	private void addReactionRemoveResults(Integer user, JSONObject surveyAnswers) {
		reactionRemoveResults.addUser(user);
		JSONObject reactionRemoveAnswers = surveyAnswers
				.getJSONObject("reaction").getJSONObject("remove")
				.getJSONObject("reason");
		
		if (reactionRemoveAnswers.getString("bother").equals("true")) {
			reactionRemoveResults.addAnswer(user, new SelectedItem(
					"I would not want to bother them."));
		}
		if (reactionRemoveAnswers.getString("privacy").equals("true")) {
			reactionRemoveResults.addAnswer(user, new SelectedItem(
					"I would want to avoid unnecessarily sharing sensitive or private information with them."));
		}
		if (reactionRemoveAnswers.getJSONObject("other").getString("checked").equals("true")
				|| reactionRemoveAnswers.getJSONObject("other").getString("val")
						.trim().length() > 0) {

			reactionRemoveResults.addAnswer(user, new OtherValue(
					reactionRemoveAnswers.getJSONObject("other").getString("val").trim()));
		}
	}
	
	private void addReactionKeepResults(Integer user, JSONObject surveyAnswers) {
		reactionKeepResults.addUser(user);
		JSONObject reactionKeepAnswers = surveyAnswers
				.getJSONObject("reaction").getJSONObject("keep")
				.getJSONObject("reason");
		
		if (reactionKeepAnswers.getString("error").equals("true")) {
			reactionKeepResults.addAnswer(user, new SelectedItem(
					"There is a small chance of error."));
		}
		if (reactionKeepAnswers.getString("useful").equals("true")) {
			reactionKeepResults.addAnswer(user, new SelectedItem(
					"The sent or posted information would still be useful to the readers."));
		}
		if (reactionKeepAnswers.getJSONObject("other").getString("checked").equals("true")
				|| reactionKeepAnswers.getJSONObject("other").getString("val")
						.trim().length() > 0) {

			reactionKeepResults.addAnswer(user, new OtherValue(
					reactionKeepAnswers.getJSONObject("other").getString("val").trim()));
		}
		
	}
	
	private void addReactionFindResults(Integer user, JSONObject surveyAnswers) {
		reactionFindResults.addUser(user);
		JSONObject reactionFindAnswers = surveyAnswers
				.getJSONObject("reaction").getJSONObject("find");
		
		if (reactionFindAnswers.getString("search").equals("true")) {
			reactionFindResults.addAnswer(user, new SelectedItem(
					"Search engine (Google, etc.)"));
		}
		if (reactionFindAnswers.getString("im").equals("true")) {
			reactionFindResults.addAnswer(user, new SelectedItem(
					"Sending an instant message"));
		}
		if (reactionFindAnswers.getString("phone").equals("true")) {
			reactionFindResults.addAnswer(user, new SelectedItem(
					"Call someone on the phone"));
		}
		if (reactionFindAnswers.getString("meetRecipient").equals("true")) {
			reactionFindResults.addAnswer(user, new SelectedItem(
					"Meet in person with recipient(s)"));
		}
		if (reactionFindAnswers.getString("meetOthers").equals("true")) {
			reactionFindResults.addAnswer(user, new SelectedItem(
					"Meet in person with someone else"));
		}
		if (reactionFindAnswers.getJSONObject("other").getString("checked").equals("true")
				|| reactionFindAnswers.getJSONObject("other").getString("val")
						.trim().length() > 0) {

			reactionFindResults.addAnswer(user, new OtherValue(
					reactionFindAnswers.getJSONObject("other").getString("val").trim()));
		}
		
	}
	
	private void addOtherResults(Integer user, JSONObject surveyAnswers) {
		otherResults.addUser(user);
		JSONObject otherAnswers = surveyAnswers
				.getJSONObject("other");

		if (otherAnswers.getString("ignored").equals("true")) {
			otherResults.addAnswer(user, new SelectedItem(
					"I would need to confirm people were not ignoring me."));
		}
		if (otherAnswers.getString("schedule").equals("true")) {
			otherResults.addAnswer(user, new SelectedItem(
					"Knowing if and when responses will occur would help me plan my schedule."));
		}
		if (otherAnswers.getString("ok").equals("true")) {
			otherResults.addAnswer(user, new SelectedItem(
					"I would need to make sure everything is ok with my recipient(s)."));
		}
		if (otherAnswers.getString("excitement").equals("true")) {
			otherResults.addAnswer(user, new SelectedItem(
					"I would like to determine if people are excited about my message or post."));
		}
		if (otherAnswers.getString("reliable").equals("true")) {
			otherResults.addAnswer(user, new SelectedItem(
					"It would help me determine how reliable people are."));
		}
		if (otherAnswers.getJSONObject("other").getString("checked").equals("true")
				|| otherAnswers.getJSONObject("other").getString("val")
						.trim().length() > 0) {

			otherResults.addAnswer(user, new OtherValue(
					otherAnswers.getJSONObject("other").getString("val").trim()));
		}
		if (otherAnswers.getString("elaboration").trim().length() > 0) {
			otherResults.addAnswer(user, new SurveyElaborationAnswer(
					otherAnswers.getString("elaboration").trim()
				));
		}
	}
	
	private void addSelfResults(Integer user, JSONObject surveyAnswers) {
		selfResults.addUser(user);
		JSONObject selfAnswers = surveyAnswers
				.getJSONObject("self");

		if (selfAnswers.getString("judged").equals("true")) {
			selfResults.addAnswer(user, new SelectedItem(
					"I needed to ensure I would not be judged poorly."));
		}
		if (selfAnswers.getString("opportunity").equals("true")) {
			selfResults.addAnswer(user, new SelectedItem(
					"I needed to ensure I would not miss some opportunity."));
		}
		if (selfAnswers.getJSONObject("other").getString("checked").equals("true")
				|| selfAnswers.getJSONObject("other").getString("val")
						.trim().length() > 0) {

			selfResults.addAnswer(user, new OtherValue(
					selfAnswers.getJSONObject("other").getString("val").trim()));
		}
		if (selfAnswers.getString("elaboration").trim().length() > 0) {
			selfResults.addAnswer(user, new SurveyElaborationAnswer(
					selfAnswers.getString("elaboration").trim()
				));
		}
	}
	
	private void addHarmResults(Integer user, JSONObject surveyAnswers) {
		harmResults.addUser(user);
		JSONObject harmAnswers = surveyAnswers
				.getJSONObject("harm");
		
		if (harmAnswers.getString("privacy").equals("true")) {
			harmResults.addAnswer(user, new SelectedItem(
					"A sender can determine private information about the receiver(s) based on the predicted response times."));
		}
		if (harmAnswers.getString("error").equals("true")) {
			harmResults.addAnswer(user, new SelectedItem(
					"Because of potential error, senders or receivers may take wrong actions or have unreasonable expectations."));
		}
		if (harmAnswers.getString("reminder").equals("true")) {
			harmResults.addAnswer(user, new SelectedItem(
					"Others may already remind senders if they miss or are close to missing a deadline."));
		}
		if (harmAnswers.getJSONObject("other").getString("checked").equals("true")
				|| harmAnswers.getJSONObject("other").getString("val")
						.trim().length() > 0) {

			harmResults.addAnswer(user, new OtherValue(
					harmAnswers.getJSONObject("other").getString("val").trim()));
		}
		if (harmAnswers.getString("elaboration").trim().length() > 0) {
			harmResults.addAnswer(user, new SurveyElaborationAnswer(
					harmAnswers.getString("elaboration").trim()
				));
		}
	}
	
	private void addCommentsResults(Integer user, JSONObject surveyAnswers) {
		commentsResults.addUser(user);
		if (surveyAnswers.has("comments") && surveyAnswers.getString("comments").trim().length() > 0) {
			commentsResults.addAnswer(user, new SurveyElaborationAnswer(
					surveyAnswers.getString("comments").trim()));
		}
	}

	public void addUserResults(String surveyId, Map<Integer,Long> responseTimeQuestions, JSONObject surveyAnswers) {

		Integer user = getUserId(surveyId);
		
		for(Integer questionId : responseTimeQuestions.keySet()) {
			Long responseTime = responseTimeQuestions.get(questionId);
			addResponseTimeResults(user, questionId, responseTime, surveyAnswers);
		}

		if (surveyAnswers.has("converted_times") && surveyAnswers.getBoolean("converted_times")) {
			return;
		}
		
		boolean foundDeadlineAnswer = addDeadlineResults(user, surveyAnswers);
		if (foundDeadlineAnswer) {
			addReactionResults(user, surveyAnswers);
		}
		addOtherResults(user, surveyAnswers);
		addSelfResults(user, surveyAnswers);
		addHarmResults(user, surveyAnswers);
		addCommentsResults(user, surveyAnswers);
	}
}
