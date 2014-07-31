package testbed.datasetspecific;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import prediction.response.time.InverseGaussianDistribution;
import prediction.response.time.LogNormalDistribution;
import testbed.PreviousResultsResponseTimeTestbed;
import testbed.ResponseTimeTestbed;
import testbed.dataset.actions.messages.MessageDataset;
import testbed.dataset.actions.messages.newsgroups.Newsgroups20Dataset;
import data.representation.actionbased.messages.ComparableAddress;
import data.representation.actionbased.messages.newsgroup.JavaMailNewsgroupPost;
import data.representation.actionbased.messages.newsgroup.NewsgroupThread;

public class NewsgroupTestbed
		extends
		MessagesSpecificTestbed<Integer, ComparableAddress, JavaMailNewsgroupPost, NewsgroupThread<ComparableAddress, JavaMailNewsgroupPost>> {

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
	
	static void runPreviousResultsResponseTimeEvaluation() throws IOException {
		PreviousResultsResponseTimeTestbed<Integer, ComparableAddress, JavaMailNewsgroupPost, NewsgroupThread<ComparableAddress, JavaMailNewsgroupPost>> previousResultsResponseTimeTestbed =
				new PreviousResultsResponseTimeTestbed<>(
						datasets, idClass, collaboratorClass, messageClass, threadClass);
		previousResultsResponseTimeTestbed.runTestbed();
	}
	
	static void runResponseTimeTests() throws Exception {
		ResponseTimeTestbed<Integer, ComparableAddress, JavaMailNewsgroupPost, NewsgroupThread<ComparableAddress, JavaMailNewsgroupPost>> testbed =
				new ResponseTimeTestbed<>(
						datasets, 
						new InverseGaussianDistribution(87621.1, 1042.61),
						new LogNormalDistribution(10.4017, 1.74268),
						collaboratorClass, messageClass, threadClass);
		testbed.runTestbed();
	}

	public NewsgroupTestbed() {
		super(idClass, collaboratorClass, messageClass, threadClass);
	}

	@Override
	public Collection<MessageDataset<Integer, ComparableAddress, JavaMailNewsgroupPost, NewsgroupThread<ComparableAddress, JavaMailNewsgroupPost>>> getMessageDatasets() {

		Collection<MessageDataset<Integer, ComparableAddress, JavaMailNewsgroupPost, NewsgroupThread<ComparableAddress, JavaMailNewsgroupPost>>> datasets = new ArrayList<>();

		datasets.add(new Newsgroups20Dataset("20Newsgroups", new File(
				"data/20 Newsgroups")));

		return datasets;
	}

	@Override
	public InverseGaussianDistribution getResponseTimeInverseGaussianDistribution() {
		return new InverseGaussianDistribution(87621.1, 1042.61);
	}

	@Override
	public LogNormalDistribution getResponseTimeLogNormalDistribution() {
		return new LogNormalDistribution(10.4017, 1.74268);
	}

	public static void main(String[] args) throws Exception {

		NewsgroupTestbed testbed = new NewsgroupTestbed();
		testbed.runTestbed();

	}

}
