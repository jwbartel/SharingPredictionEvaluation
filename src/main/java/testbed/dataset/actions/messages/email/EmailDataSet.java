package testbed.dataset.actions.messages.email;

import java.io.File;

import testbed.dataset.actions.messages.MessageDataSet;
import data.representation.actionbased.messages.email.EmailMessage;
import data.representation.actionbased.messages.email.EmailThread;

public abstract class EmailDataSet<IdType, RecipientType, MessageType extends EmailMessage<RecipientType>, ThreadType extends EmailThread<RecipientType, MessageType>>
		extends MessageDataSet<IdType, RecipientType, MessageType, ThreadType> {

	public EmailDataSet(String name, IdType[] accountIds, File rootFolder,
			Class<IdType> genericClass) {
		super(name, accountIds, rootFolder, genericClass);
	}

}
