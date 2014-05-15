package testbed.dataset.actions.messages.email;

import java.io.File;

import recommendation.general.actionbased.messages.email.EmailMessage;
import recommendation.general.actionbased.messages.email.EmailThread;
import testbed.dataset.actions.messages.MessageDataSet;

public abstract class EmailDataSet<IdType, RecipientType> extends
		MessageDataSet<IdType, RecipientType, EmailMessage<RecipientType>, EmailThread<RecipientType, EmailMessage<RecipientType>>> {

	public EmailDataSet(String name, IdType[] accountIds, File rootFolder,
			Class<IdType> genericClass) {
		super(name, accountIds, rootFolder, genericClass);
	}

}
