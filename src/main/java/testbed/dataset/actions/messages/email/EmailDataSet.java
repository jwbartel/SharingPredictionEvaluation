package testbed.dataset.actions.messages.email;

import java.io.File;

import testbed.dataset.actions.messages.MessageDataset;
import data.representation.actionbased.messages.email.EmailMessage;
import data.representation.actionbased.messages.email.EmailThread;

public abstract class EmailDataSet<IdType, RecipientType, MessageType extends EmailMessage<RecipientType>, ThreadType extends EmailThread<RecipientType, MessageType>>
		extends MessageDataset<IdType, RecipientType, MessageType, ThreadType> {

	public EmailDataSet(String name, IdType[] accountIds, File rootFolder) {
		super(name, accountIds, rootFolder);
	}

}
