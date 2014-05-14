package testbed.dataset.messages;

import java.io.File;
import java.util.Collection;

import recommendation.general.actionbased.messages.MessageThread;
import recommendation.general.actionbased.messages.SingleMessage;
import testbed.dataset.DataSet;

public abstract class MessageDataSet<IdType,RecipientType, MessageType extends SingleMessage<RecipientType>> extends DataSet<IdType>{

	public MessageDataSet(String name, IdType[] accountIds, File rootFolder,
			Class<IdType> genericClass) {
		super(name, accountIds, rootFolder, genericClass);
	}
	
	public abstract Collection<MessageType> getAllMessages(IdType account);
	public abstract Collection<MessageType> getTrainMessages(IdType account, double percentTrain);
	public abstract Collection<MessageType> getTestMessages(IdType account, double percentTrain);
	
	public abstract Collection<MessageThread<RecipientType,MessageType>> getAllThreads(IdType account);
	public abstract Collection<MessageThread<RecipientType,MessageType>> getTrainThreads(IdType account, double percentTrain);
	public abstract Collection<MessageThread<RecipientType,MessageType>> getTestThreads(IdType account, double percentTest);

	public abstract File getRecipientRecommendationMetricsFile();
	public abstract File getHierarchicalRecipientRecommendationMetricsFile();

}
