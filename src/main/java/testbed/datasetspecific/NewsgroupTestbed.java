package testbed.datasetspecific;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import testbed.PreviousResultsResponseTimeTestbed;
import testbed.dataset.actions.messages.MessageDataset;
import testbed.dataset.actions.messages.newsgroups.Newsgroups20Dataset;
import data.representation.actionbased.messages.ComparableAddress;
import data.representation.actionbased.messages.newsgroup.JavaMailNewsgroupPost;
import data.representation.actionbased.messages.newsgroup.NewsgroupThread;

public class NewsgroupTestbed {

	static Class<Integer> idClass = Integer.class;
	static Class<ComparableAddress> collaboratorClass = ComparableAddress.class;
	static Class<JavaMailNewsgroupPost> messageClass = JavaMailNewsgroupPost.class;
	@SuppressWarnings("unchecked")
	static Class<NewsgroupThread<ComparableAddress, JavaMailNewsgroupPost>> threadClass = (Class<NewsgroupThread<ComparableAddress, JavaMailNewsgroupPost>>) (new NewsgroupThread<ComparableAddress, JavaMailNewsgroupPost>()
			.getClass());

	static Collection<MessageDataset<Integer, ComparableAddress, JavaMailNewsgroupPost, NewsgroupThread<ComparableAddress, JavaMailNewsgroupPost>>> datasets = new ArrayList<>();

	static {

		// Add data sets
		datasets.add(new Newsgroups20Dataset("20Newsgroups", new File(
				"data/20 Newsgroups")));

	}

	public static void main(String[] args) throws IOException {

		runPreviousResultsResponseTime();

	}
	
	static void runPreviousResultsResponseTime() throws IOException {
		PreviousResultsResponseTimeTestbed<Integer, ComparableAddress, JavaMailNewsgroupPost, NewsgroupThread<ComparableAddress, JavaMailNewsgroupPost>> previousResultsResponseTimeTestbed =
				new PreviousResultsResponseTimeTestbed<>(
						datasets, idClass, collaboratorClass, messageClass, threadClass);
		previousResultsResponseTimeTestbed.runTestbed();
	}
	

}
