package testbed.dataset.messages.email;

import java.io.File;

import recommendation.general.actionbased.messages.email.EmailMessage;
import testbed.dataset.messages.MessageDataSet;

public abstract class EmailDataSet<IdType, RecipientType> extends
		MessageDataSet<IdType, RecipientType, EmailMessage<RecipientType>> {

	public EmailDataSet(String name, IdType[] accountIds, File rootFolder,
			Class<IdType> genericClass) {
		super(name, accountIds, rootFolder, genericClass);
	}

}
