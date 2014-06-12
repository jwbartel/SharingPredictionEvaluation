package testbed.dataset.actions.messages.stackoverflow;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;

import testbed.dataset.actions.messages.MessageDataSet;
import data.representation.actionbased.messages.stackoverflow.StackOverflowMessage;
import data.representation.actionbased.messages.stackoverflow.StackOverflowThread;

public abstract class StackOverflowDataset<Recipient, MessageType extends StackOverflowMessage<Recipient>, ThreadType extends StackOverflowThread<Recipient, MessageType>>
		extends MessageDataSet<Long, Recipient, MessageType, ThreadType> {

	protected Collection<MessageType> messages = new TreeSet<>();
	protected Map<Long, ThreadType> threads = new TreeMap<>();

	public StackOverflowDataset(String name, Long[] accountIds, File rootFolder) {
		super(name, accountIds, rootFolder, Long.class);
	}

	@Override
	public Collection<MessageType> getAllMessages(Long account) {
		return new TreeSet<>(messages);
	}

	@Override
	public Collection<ThreadType> getAllThreads(Long account) {
		return new TreeSet<>(threads.values());
	}

	public static <Recipient, MessageType extends StackOverflowMessage<Recipient>> Collection<MessageType> getQuestions(
			Collection<MessageType> messages) {
		Collection<MessageType> questions = new ArrayList<>();
		for (MessageType message : messages) {
			if (message.getType() == data.representation.actionbased.messages.stackoverflow.StackOverflowMessage.MessageType.Question) {
				questions.add(message);
			}
		}
		return questions;
	}

	public Collection<MessageType> getTrainQuestions(Long account, double percentTrain) {

		Collection<MessageType> questions = getQuestions(getAllMessages(account));
		return getTrainMessages(questions, percentTrain);
	}

	public Collection<MessageType> getTestQuestions(Long account, double percentTrain) {

		Collection<MessageType> questions = getQuestions(getAllMessages(account));
		return getTestMessages(questions, percentTrain);
	}

	public abstract ThreadType createThread();

	protected void addToThread(MessageType message) {
		Long id = message.getId();
		ThreadType thread = threads.get(id);
		if (thread == null) {
			thread = createThread();
			threads.put(id, thread);
		}
		thread.addThreadedAction(message);
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
}
