package testbed.dataset.actions.messages;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.mail.MessagingException;

import org.apache.commons.io.FileUtils;

import testbed.dataset.actions.ActionsDataSet;
import data.representation.actionbased.messages.MessageThread;
import data.representation.actionbased.messages.SingleMessage;

public abstract class MessageDataset<IdType, RecipientType, MessageType extends SingleMessage<RecipientType>, ThreadType extends MessageThread<RecipientType, MessageType>>
		extends ActionsDataSet<IdType, RecipientType, MessageType, ThreadType> {

	public MessageDataset(String name, IdType[] accountIds, File rootFolder,
			Class<IdType> genericClass) {
		super(name, accountIds, rootFolder, genericClass);
	}
	
	public abstract Collection<MessageType> getAllMessages(IdType account);
	
	public abstract Collection<ThreadType> getAllThreads(IdType account);

	public List<ThreadType> getAllThreadsTimeSorted(IdType account) {
		
		List<ThreadType> sortedThreads = new ArrayList<>(new TreeSet<>(getAllThreads(account)));
		Collections.sort(sortedThreads);
		return sortedThreads;
	}
	
	protected File getMetricsFolder() {
		return new File(getRootFolder(), "metric statistics");
	}

	public File getLivenessModelsFolder() {
		return new File(getRootFolder(), "liveness models");
	}
	
	public File getLivenessModelsFile(String type, Integer testNum) {
		return new File(getLivenessModelsFolder(),
				type + "_" + testNum + ".txt");
	}

	public File getResponseTimesModelsFolder() {
		return new File(getRootFolder(), "response time models");
	}
	
	public File getResponseTimeModelsFile(String type, Integer testNum) {
		return new File(getResponseTimesModelsFolder(),
				type + "_" + testNum + ".txt");
	}

	public File getLivenessMetricsFile() {
		return new File(getMetricsFolder(),
				"liveness prediction results.csv");
	}

	public File getResponseTimeMetricsFile() {
		return new File(getMetricsFolder(),
				"response time prediction results.csv");
	}

	public File getPreviousResultsLivenessMetricsFile() {
		return new File(getMetricsFolder(),
				"previous results liveness prediction results.csv");
	}

	public File getPreviousResultsResponseTimeMetricsFile() {
		return new File(getMetricsFolder(),
				"previous results response time prediction results.csv");
	}

	public File getRecipientRecommendationMetricsFile() {
		return new File(getMetricsFolder(),
				"recipient recommendation results.csv");
	}

	public File getHierarchicalRecipientRecommendationMetricsFile() {
		return new File(getMetricsFolder(),
				"hierarchical recipient recommendation results.csv");
	}

	@Override
	public File getActionBasedSeedlessGroupsMetricsFile() {
		return new File(getMetricsFolder(),
				"action based seedless group recommendation results.csv");
	}

	@Override
	public File getBurstyGroupsMetricsFile() {
		return new File(getMetricsFolder(),
				"bursty group recommendation results.csv");
	}
	
	@Override
	public File getEvolutionMetricsFile() {
		return new File(getMetricsFolder(), "group evolution results.csv");
	}
	
	public File getResponseTimesAnalysisFile() {
		return new File(getAnalysisFolder(), "response times.csv");
	}

	private File getResponseTimesExperimentsFolder() {
		return new File(getRootFolder(), "response times");
	}

	private File getResponseTimesFoldsFolder() {
		return new File(getResponseTimesExperimentsFolder(), "folds");
	}

	private File getResponseTimesFoldSetsFolder() {
		return new File(getResponseTimesExperimentsFolder(), "fold sets for experiments");
	}

	private File getResponseTimesTrainingSetsFile() {
		return new File(getResponseTimesFoldSetsFolder(), "train_sets.csv");
	}

	private List<Double> loadTimes(String fold) throws IOException {
		List<Double> retVal = new ArrayList<>();
		File foldFile = new File(getResponseTimesFoldsFolder(), fold + "_y.csv");
		List<String> lines = FileUtils.readLines(foldFile);
		for (String line : lines) {
			if (line.equals("Inf")) {
				retVal.add(Double.POSITIVE_INFINITY);
			} else {
				retVal.add(Double.parseDouble(line)*60*60);
			}
		}
		return retVal;
	}

	public Map<Integer, List<Double>> getResponesTimesTrainingTimes() throws IOException {
		Map<Integer, List<Double>> retVal = new TreeMap<>();

		List<String> trainSetsLines = FileUtils.readLines(getResponseTimesTrainingSetsFile());
		Integer setId = 1;
		for (String trainSetsLine : trainSetsLines) {
			String[] trainingSet = trainSetsLine.split(",");
			List<Double> trainingTimes = new ArrayList<>();
			for (String trainingFold : trainingSet) {
				trainingTimes.addAll(loadTimes(trainingFold));
			}
			retVal.put(setId, trainingTimes);
			setId++;
		}

		return retVal;
	}

	private File getResponseTimesTestingSetsFile() {
		return new File(getResponseTimesFoldSetsFolder(), "test_sets.csv");
	}

	public Map<Integer, List<Double>> getResponesTimesTestingTimes() throws IOException {
		Map<Integer, List<Double>> retVal = new TreeMap<>();

		List<String> testSetsLines = FileUtils.readLines(getResponseTimesTestingSetsFile());
		Integer setId = 1;
		for (String testSetsLine : testSetsLines) {
			String[] testingSet = testSetsLine.split(",");
			List<Double> testingTimes = new ArrayList<>();
			for (String testingFold : testingSet) {
				testingTimes.addAll(loadTimes(testingFold));
			}
			retVal.put(setId, testingTimes);
			setId++;
		}

		return retVal;
	}

	public File getResponseTimesResultsFolder() {
		return new File(getResponseTimesExperimentsFolder(), "results");
	}

	@Override
	public Collection<MessageType> getTrainMessages(IdType account,
			double percentTrain) {

		Collection<MessageType> allMessages = getAllMessages(account);
		if (allMessages == null) {
			return null;
		}

		return getTrainMessages(allMessages, percentTrain);
	}
	
	protected Collection<MessageType> getTrainMessages(
			Collection<MessageType> messages, double percentTrain) {

		int numTrain = (int) (messages.size() * percentTrain);
		Collection<MessageType> trainingMessages = new ArrayList<>();
		for (MessageType message : messages) {
			if (trainingMessages.size() == numTrain) {
				break;
			}
			trainingMessages.add(message);
		}
		return trainingMessages;
	}

	@Override
	public Collection<MessageType> getTestMessages(IdType account,
			double percentTrain) {
		Collection<MessageType> allMessages = getAllMessages(account);
		if (allMessages == null) {
			return null;
		}

		return getTestMessages(allMessages, percentTrain);
	}
	
	protected Collection<MessageType> getTestMessages(
			Collection<MessageType> messages, double percentTrain) {
		
		int numTrain = (int) (messages.size() * percentTrain);
		Collection<MessageType> testMessages = new ArrayList<>();
		int count = 0;
		for (MessageType message : messages) {
			if (count < numTrain) {
				count++;
				continue;
			}
			testMessages.add(message);
			count++;
		}
		return testMessages;
	}

	@Override
	public Collection<ThreadType> getTrainThreads(
			IdType account, double percentTrain) {
		
		Collection<ThreadType> allThreads = getAllThreadsTimeSorted(account);
		if (allThreads == null) {
			return null;
		}

		int numTrain = (int) (allThreads.size() * percentTrain);
		Collection<ThreadType> trainingThreads = new ArrayList<>();
		for (ThreadType thread : allThreads) {
			if (trainingThreads.size() == numTrain) {
				break;
			}
			trainingThreads.add(thread);
		}
		return trainingThreads;
	}

	@Override
	public Collection<ThreadType> getTestThreads(
			IdType account, double percentTrain) {
		
		Collection<ThreadType> allThreads = getAllThreadsTimeSorted(account);
		if (allThreads == null) {
			return null;
		}

		int numTrain = (int) (allThreads.size() * percentTrain);
		Collection<ThreadType> testThreads = new ArrayList<>();
		int count = 0;
		for (ThreadType thread : allThreads) {
			if (count < numTrain) {
				count++;
				continue;
			}
			testThreads.add(thread);
			count++;
		}
		return testThreads;
	}
	
	public Collection<Double> getAllResponseTimes(double trainingSize) throws MessagingException {
		
		Collection<Double> responseTimes = new ArrayList<>();
		for (IdType account : getAccountIds()) {
			Collection<ThreadType> threads = getTrainThreads(account, trainingSize);
			for (ThreadType thread : threads) {
				responseTimes.add(thread.getTimeToResponse());
			}
		}
		return responseTimes;
	}
	
	public void writeAllResponseTimes(double trainingSize) throws MessagingException, IOException {
		Collection<Double> responseTimes = getAllResponseTimes(trainingSize);
		
		String outputStr = "";
		for (Double responseTime : responseTimes) {
			if (responseTime != null && !responseTime.isInfinite()) {
				outputStr += responseTime + "\n";
			}
		}
		
		File outputFile = getResponseTimesAnalysisFile();
		FileUtils.write(outputFile, outputStr);
	}

}
