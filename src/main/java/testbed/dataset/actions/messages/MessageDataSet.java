package testbed.dataset.actions.messages;

import java.io.File;
import java.util.Collection;

import recommendation.general.actionbased.messages.MessageThread;
import recommendation.general.actionbased.messages.SingleMessage;
import testbed.dataset.actions.ActionsDataSet;

public abstract class MessageDataSet<IdType, RecipientType, MessageType extends SingleMessage<RecipientType>, ThreadType extends MessageThread<RecipientType, MessageType>>
		extends ActionsDataSet<IdType, RecipientType, MessageType, ThreadType> {

	public MessageDataSet(String name, IdType[] accountIds, File rootFolder,
			Class<IdType> genericClass) {
		super(name, accountIds, rootFolder, genericClass);
	}
	
	public abstract Collection<MessageType> getAllMessages(IdType account);
	public abstract Collection<MessageType> getTrainMessages(IdType account, double percentTrain);
	public abstract Collection<MessageType> getTestMessages(IdType account, double percentTrain);
	
	public abstract Collection<ThreadType> getAllThreads(IdType account);
	public abstract Collection<ThreadType> getTrainThreads(IdType account, double percentTrain);
	public abstract Collection<ThreadType> getTestThreads(IdType account, double percentTest);

	public abstract File getRecipientRecommendationMetricsFile();
	public abstract File getHierarchicalRecipientRecommendationMetricsFile();

}
