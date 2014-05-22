package testbed.dataset.actions.messages.stackoverflow;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;

public class StackOverflowPreprocessor {

	File questionsFile;
	File questionTimesFile;
	File rootFolder;
	
	public StackOverflowPreprocessor(File questionsFile, File questionTimesFile, File rootFolder) {
		this.questionsFile = questionsFile;
		this.questionTimesFile = questionTimesFile;
		this.rootFolder = rootFolder;
	}
	
	public Map<Long,Long> loadThreadStartTimes() throws IOException {
		Map<Long,Long> threadStartTimes = new TreeMap<>();
		
		BufferedReader in = new BufferedReader(new FileReader(questionTimesFile));
		String line = in.readLine();
		while (line != null) {
			String[] split = line.split(",");
			threadStartTimes.put(Long.parseLong(split[0]), Long.parseLong(split[1]));
			line = in.readLine();
		}
		in.close();
		
		return threadStartTimes;
	}
	
	public Map<Long,String> loadThreadTitles() throws IOException {
		Map<Long,String> threadTitles = new TreeMap<>();
		
		BufferedReader in = new BufferedReader(new FileReader(questionsFile));
		String line = in.readLine();
		while (line != null) {
			
			if (line.length() > 0) {
				String idStr = line.substring(line.indexOf("ID:")+3, line.indexOf("\t"));
				Long id = Long.parseLong(idStr);

				String title = line.substring(line.indexOf("Title:")+6);
				threadTitles.put(id, title);
			}
			line = in.readLine();
		}
		in.close();
		
		return threadTitles;
	}
	
	public Map<Long,String> loadQuestionOwners(Set<Long> threadIds) throws IOException {
		Map<Long,String> threadOwners = new TreeMap<>();
		
		BufferedReader in = new BufferedReader(new FileReader(questionsFile));
		String line = in.readLine();
		while (line != null) {
			
			if (line.length() > 0) {
				String idStr = line.substring(line.indexOf("ID:")+3, line.indexOf("\t"));
				Long id = Long.parseLong(idStr);
				if (!threadIds.contains(id)) {
					line = in.readLine();
					continue;
				}

				String owner = line.substring(line.indexOf("Owner:")+6);
				owner = owner.substring(0,owner.indexOf('\t'));
				threadOwners.put(id, owner);
			}
			line = in.readLine();
		}
		in.close();
		
		return threadOwners;
	}

	private static class CreationDateOwnerPair {
		final Date creationDate;
		final Long owner;
		
		public CreationDateOwnerPair(Date creationDate, Long owner) {
			this.creationDate = creationDate;
			this.owner = owner;
		}
	}
	
	public Map<Long, Collection<CreationDateOwnerPair>> loadAnswerOwners(
			Set<Long> threadIds, File answersFile) throws IOException {
		Map<Long, Collection<CreationDateOwnerPair>> answerOwners = new TreeMap<>();
		
		BufferedReader in = new BufferedReader(new FileReader(answersFile));
		String line = in.readLine();
		while (line != null) {
			
			if (line.length() > 0) {
				String idStr = line.substring(line.indexOf("Parent:")+7);
				idStr = idStr.substring(0, idStr.indexOf("\t"));
				Long id = Long.parseLong(idStr);
				if (!threadIds.contains(id)) {
					line = in.readLine();
					continue;
				}
				
				String creationDateStr = line.substring(line.indexOf("CreationDate:")+13);
				creationDateStr = creationDateStr.substring(0, creationDateStr.indexOf('\t'));
				Date creationDate = new Date(Long.parseLong(creationDateStr));

				String ownerStr = line.substring(line.indexOf("Owner:")+6);
				ownerStr = ownerStr.substring(0,ownerStr.indexOf('\t'));
				Long owner;
				if (ownerStr.equals("null")) {
					owner = null;
				} else{
					owner = Long.parseLong(ownerStr);
				}
				
				Collection<CreationDateOwnerPair> pairs = answerOwners.get(id);
				if (pairs == null) {
					pairs = new ArrayList<>();
					answerOwners.put(id, pairs);
				}
				pairs.add(new CreationDateOwnerPair(creationDate, owner));
			}
			line = in.readLine();
		}
		in.close();
		
		return answerOwners;
	}
	
	public void addDate() throws IOException {
		Map<Long,Long> threadStartTimes = loadThreadStartTimes();
		
		File interactionsFolder = new File(rootFolder, "interactions");
		File[] interactions = interactionsFolder.listFiles();
		Arrays.sort(interactions);
		for (File interaction : interactions) {
			String content = FileUtils.readFileToString(interaction);
			if (!content.startsWith("Date:")) {
				String threadIdStr = content.substring(content.indexOf("Thread ID:")+10);
				threadIdStr = threadIdStr.substring(0,threadIdStr.indexOf("\n"));
				Long threadId = Long.parseLong(threadIdStr);
				
				String timeSoFarStr = content.substring(content.indexOf("Time so far:")+12);
				timeSoFarStr = timeSoFarStr.substring(0,timeSoFarStr.indexOf('\n'));
				Long timeSoFar = Long.parseLong(timeSoFarStr);
				
				if (threadStartTimes.containsKey(threadId)) {
					long startTime = threadStartTimes.get(threadId);
					Date date = new Date(startTime + timeSoFar);
					content = "Date:" + date + "\n" + content;
					FileUtils.write(interaction, content);
				} else {
					throw new RuntimeException("No start time for thread :" + threadId);
				}
			}
		}
	}
	
	
	public void addTitle() throws IOException {
		Map<Long,String> threadTitles = loadThreadTitles();
		
		File interactionsFolder = new File(rootFolder, "interactions");
		File[] interactions = interactionsFolder.listFiles();
		Arrays.sort(interactions);
		for (File interaction : interactions) {
			String content = FileUtils.readFileToString(interaction);
			
			String prefix = "";
			String processedLine = content;
			if (content.startsWith("Date:")) {
				prefix = processedLine.substring(0,processedLine.indexOf('\n')+1);
				processedLine = processedLine.substring(processedLine.indexOf('\n')+1);
			}
			if (!content.startsWith("Title:")) {
				String threadIdStr = content.substring(content.indexOf("Thread ID:")+10);
				threadIdStr = threadIdStr.substring(0,threadIdStr.indexOf("\n"));
				Long threadId = Long.parseLong(threadIdStr);
				
				if (threadTitles.containsKey(threadId)) {
					String title = threadTitles.get(threadId);
					content = prefix + "Title:" + title + "\n" + processedLine;
					FileUtils.write(interaction, content);
				} else {
					throw new RuntimeException("No start time for thread :" + threadId);
				}
			}
		}
	}
	
	DateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
	

	private Set<Long> loadCandidateThreadIds() throws IOException {
		Set<Long> threadIds = new HashSet<>();
		
		File interactionsFolder = new File(rootFolder, "interactions");
		File[] interactions = interactionsFolder.listFiles();
		Arrays.sort(interactions);
		for (File interaction : interactions) {
			String content = FileUtils.readFileToString(interaction);
			
			String threadIdStr = content.substring(content.indexOf("Thread ID:")+10);
			threadIdStr = threadIdStr.substring(0,threadIdStr.indexOf("\n"));
			Long threadId = Long.parseLong(threadIdStr);
			
			threadIds.add(threadId);
		}
		
		return threadIds;
	}
	
	public void addAnswerOwners(File answersFile) throws IOException, ParseException {
		Set<Long> threadIds = loadCandidateThreadIds();
		Map<Long,String> questionOwners = loadQuestionOwners(threadIds);
		Map<Long,Collection<CreationDateOwnerPair>> answerOwners = loadAnswerOwners(threadIds, answersFile);
		
		
		File interactionsFolder = new File(rootFolder, "interactions");
		File[] interactions = interactionsFolder.listFiles();
		Arrays.sort(interactions);
		for (File interaction : interactions) {
			String content = FileUtils.readFileToString(interaction);

			String prefix = "";
			String processedLine = content;
			
			if (!processedLine.startsWith("Date:")) {
				continue;
			}
			
			prefix += processedLine.substring(0,processedLine.indexOf('\n')+1);
			processedLine = processedLine.substring(processedLine.indexOf('\n')+1);
			
			if (!processedLine.startsWith("Title:")) {
				continue;
			}
			prefix += processedLine.substring(0,processedLine.indexOf('\n')+1);
			processedLine = processedLine.substring(processedLine.indexOf('\n')+1);
			
			if (processedLine.startsWith("Owner:")) {
				continue;
			}
			
			String dateStr = content.substring(content.indexOf("Date:")+5);
			dateStr = dateStr.substring(0, dateStr.indexOf('\n'));
			Date date = dateFormat.parse(dateStr);
			
			String type = content.substring(content.indexOf("Type:")+5);
			type = type.substring(0,type.indexOf("\n"));
			
			String threadIdStr = content.substring(content.indexOf("Thread ID:")+10);
			threadIdStr = threadIdStr.substring(0,threadIdStr.indexOf("\n"));
			Long threadId = Long.parseLong(threadIdStr);
			
			String owner = null;
			if (type.equals("question")) {
				if (questionOwners.containsKey(threadId)) {
					owner = questionOwners.get(threadId);
				} else {
					throw new RuntimeException("No owner for question: "
							+ interaction);
				}
			} else if (type.equals("answer")) {
				if (answerOwners.containsKey(threadId)) {
					Collection<CreationDateOwnerPair> pairs = answerOwners.get(threadId);
					for (CreationDateOwnerPair pair : pairs) {
						if (pair.creationDate.toString().equals(date.toString())) {
							if (owner  != null) {
								throw new RuntimeException("Multipe owners for answer:"+interaction);
							}
							owner = ""+pair.owner;
						}
					}
				} else {
					throw new RuntimeException(
							"No owners for question answers:" + interaction);
				}
			} else {
				System.out.println("No owners for type: " + type);
			}
			
			if (owner != null) {
				content = prefix + "Owner:" + owner + "\n" + processedLine;
				FileUtils.write(interaction, content);
			}
			
		}
	}
	
	public static void main(String[] args) throws IOException, ParseException {
		File folder = new File("data/Stack Overflow");
		File questionsFile = new File(folder, "questions.txt");
		File answersFile = new File(folder, "answers.txt");
		File questionTimesFile = new File(folder, "question times.csv");
		File rootFolder = new File(folder, "10000 Random Questions");

		StackOverflowPreprocessor preprocessor = new StackOverflowPreprocessor(
				questionsFile, questionTimesFile, rootFolder);
//		preprocessor.addDate();
//		preprocessor.addTitle();
//		preprocessor.addQuestionOwners();
		preprocessor.addAnswerOwners(answersFile);
	}
}
