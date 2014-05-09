package testbed.dataset.messages;

import java.io.File;
import java.util.Collection;

import testbed.dataset.DataSet;

public abstract class MessageDataSet<IdType,RecipientType> extends DataSet<IdType>{

	public MessageDataSet(String name, IdType[] accountIds, File rootFolder,
			Class<IdType> genericClass) {
		super(name, accountIds, rootFolder, genericClass);
	}
	
	public abstract Collection<SingleMessage<RecipientType>> getAllMesssages();
	public abstract Collection<SingleMessage<RecipientType>> getTrainMesssages(double percentTrain);
	public abstract Collection<SingleMessage<RecipientType>> getTestMesssages(double percentTest);
	
	public abstract Collection<MessageThread<RecipientType>> getAllThreads();
	public abstract Collection<MessageThread<RecipientType>> getTrainThreads(double percentTrain);
	public abstract Collection<MessageThread<RecipientType>> getTestThreads(double percentTest);

}
