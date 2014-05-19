package testbed.dataset.actions.messages.newsgroups;

import java.io.File;

import testbed.dataset.actions.messages.email.EmailDataSet;
import data.representation.actionbased.messages.newsgroup.NewsgroupPost;
import data.representation.actionbased.messages.newsgroup.NewsgroupThread;

public abstract class NewsgroupDataset<IdType, RecipientType, PostType extends NewsgroupPost<RecipientType>, ThreadType extends NewsgroupThread<RecipientType, PostType>>
		extends EmailDataSet<IdType, RecipientType, PostType, ThreadType> {

	public NewsgroupDataset(String name, IdType[] accountIds, File rootFolder,
			Class<IdType> genericClass) {
		super(name, accountIds, rootFolder, genericClass);
	}

}
