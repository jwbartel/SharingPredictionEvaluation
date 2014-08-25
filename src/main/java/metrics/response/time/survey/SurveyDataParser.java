package metrics.response.time.survey;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.codehaus.jettison.json.JSONException;
import org.json.JSONObject;

public class SurveyDataParser {
	
	private static long findTimeValue(String timeLabel, String timeDescription) {
		
		Matcher matcher = Pattern.compile("\\d+ "+timeLabel).matcher(timeDescription);
		if (matcher.find()) {
			String foundStr = matcher.group();
			int index = foundStr.indexOf(' ');
			String strVal = foundStr.substring(0, index);
			
			return Long.parseLong(strVal);
		} else {
			return 0L;
		}
	}
	
	public static Map<Integer,Long> getSurveyedResponseTimes(File surveyTimesFile) throws IOException {
		
		Map<Integer, Long> surveyTimes = new TreeMap<>();
		List<String> lines = FileUtils.readLines(surveyTimesFile);
		
		int question = 0;
		for (String line : lines) {
			if (line.startsWith("Time to Response:")) {
				
				Long time = 0L;
				
				time += findTimeValue("seconds", line) * 1000L;
				time += findTimeValue("minutes", line) * 1000L * 60;
				time += findTimeValue("hours", line) * 1000L * 60 * 60;
				time += findTimeValue("days", line) * 1000L * 60 * 60 * 24;
				time += findTimeValue("weeks", line) * 1000L * 60 * 60 * 24 * 7;
				time += findTimeValue("years", line) * 1000L * 60 * 60 * 24 * 365;
				
				surveyTimes.put(question, time);
				question++;
			}
		}
		return surveyTimes;
	}
	
	private static String convertFromPHPToJson(String phpOutput) {
		
		String jsonOutput = phpOutput.replaceAll("Array\n\\s*[(]", "{");
		
		Pattern closeObjectPattern = Pattern.compile("\n[ ]*[)]");
		Matcher matcher = closeObjectPattern.matcher(jsonOutput);
		while(matcher.find()) {
			
			String changedStr = matcher.group();
			changedStr = changedStr.substring(0, changedStr.length()-1) + "}";
			if (changedStr.length() > 2) {
				changedStr += ",";
			}
			
			jsonOutput = jsonOutput.substring(0, matcher.start()) + changedStr + jsonOutput.substring(matcher.end());
			matcher = closeObjectPattern.matcher(jsonOutput);
		}
		
		Pattern keyPattern = Pattern.compile("\\Q[\\E.+\\Q]\\E => [^\n]*\n");
		matcher = keyPattern.matcher(jsonOutput);
		while(matcher.find()) {
			
			String changedStr = matcher.group();
			changedStr = changedStr.replace('[', '"');
			changedStr = changedStr.replace(']', '"');
			changedStr = changedStr.replace("=>", ":");
			
			int indexEndKey = changedStr.indexOf(':');
			char valStartChar = changedStr.charAt(indexEndKey + 2);
			if (valStartChar == '\n') {
				changedStr = changedStr.substring(0, changedStr.length() - 1) + "\"\"\n";
			} else if (valStartChar != '{') {
				
				String newVal = changedStr.substring(0, indexEndKey + 2);
				newVal += "\"";
				newVal += changedStr.substring(indexEndKey + 2,
						changedStr.length() - 1);
				newVal += "\"\n";
				
				changedStr = newVal;
			}
			if (!changedStr.endsWith("{\n")) {
				changedStr = changedStr.substring(0,changedStr.length() - 1) + ",\n";
			}
			
			jsonOutput = jsonOutput.substring(0, matcher.start()) + changedStr + jsonOutput.substring(matcher.end());
			matcher = keyPattern.matcher(jsonOutput);
		}
		
		return jsonOutput;
	}
	
	public static JSONObject parseSurveyAnswers(File resultsFile) throws IOException, JSONException {
		
		String phpStr = FileUtils.readFileToString(resultsFile);
		String jsonStr = convertFromPHPToJson(phpStr);
		
		return new JSONObject(jsonStr);
		
	}
	
	public static void main(String[] args) throws IOException, JSONException {
		
		File timesFile = new File("/home/bartizzi/Workspaces/SharingPredictionEvaluation/data/Email Response Study/email_threads/private_data/1401944292_1/survey_questions.txt");
		System.out.println(getSurveyedResponseTimes(timesFile));
		
		
//		File resultsFile = new File("/home/bartizzi/Workspaces/SharingPredictionEvaluation/data/Email Response Study/email_threads/private_data/1401944292_1/survey_data.txt");
//		parseSurveyAnswers(resultsFile);
	}

}
