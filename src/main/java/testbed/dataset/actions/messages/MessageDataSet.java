package testbed.dataset.actions.messages;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import data.representation.actionbased.messages.MessageThread;
import data.representation.actionbased.messages.SingleMessage;
import testbed.dataset.actions.ActionsDataSet;

public abstract class MessageDataSet<IdType, RecipientType, MessageType extends SingleMessage<RecipientType>, ThreadType extends MessageThread<RecipientType, MessageType>>
		extends ActionsDataSet<IdType, RecipientType, MessageType, ThreadType> {

	public MessageDataSet(String name, IdType[] accountIds, File rootFolder,
			Class<IdType> genericClass) {
		super(name, accountIds, rootFolder, genericClass);
	}
	
	public abstract Collection<MessageType> getAllMessages(IdType account);
	
	public abstract Collection<ThreadType> getAllThreads(IdType account);

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
		
		Collection<ThreadType> allThreads = getAllThreads(account);
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
		
		Collection<ThreadType> allThreads = getAllThreads(account);
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

}
