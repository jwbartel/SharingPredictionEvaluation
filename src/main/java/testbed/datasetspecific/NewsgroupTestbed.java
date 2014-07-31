package testbed.datasetspecific;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import prediction.response.time.InverseGaussianDistribution;
import prediction.response.time.LogNormalDistribution;
import testbed.ConstantValues;
import testbed.PreviousResultsResponseTimeTestbed;
import testbed.ResponseTimeTestbed;
import testbed.dataset.actions.messages.MessageDataset;
import testbed.dataset.actions.messages.newsgroups.Newsgroups20Dataset;
import data.preprocess.graphbuilder.ActionBasedGraphBuilder;
import data.preprocess.graphbuilder.InteractionRankWeightedActionBasedGraphBuilder;
import data.preprocess.graphbuilder.SimpleActionBasedGraphBuilder;
import data.preprocess.graphbuilder.TimeThresholdActionBasedGraphBuilder;
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

	@SuppressWarnings("rawtypes")
	@Override
	public Map<Class<? extends ActionBasedGraphBuilder>, Collection<ConstantValues>> getGraphConstants() {

		Map<Class<? extends ActionBasedGraphBuilder>, Collection<ConstantValues>> constants = new HashMap<>();
		Collection<ConstantValues> simpleConstants = new ArrayList<>();
		Object[] simpleConstantSet = {};
		simpleConstants.add(new ConstantValues(simpleConstantSet));
		constants.put(SimpleActionBasedGraphBuilder.class, simpleConstants);
		
		Collection<ConstantValues> timeThresholdConstants = new ArrayList<>();
		Object[] timeThresholdConstantSet1 = {1000L*60*60*24*7}; //1.0 weeks
		Object[] timeThresholdConstantSet2 = {1000L*60*60*24*7*4}; //4.0 weeks
		Object[] timeThresholdConstantSet3 = {1000L*60*60*24*365/2}; //0.5 years
		Object[] timeThresholdConstantSet4 = {1000L*60*60*24*365}; //1 year
		Object[] timeThresholdConstantSet5 = {1000L*60*60*24*365*2}; //2 years
		timeThresholdConstants.add(new ConstantValues(timeThresholdConstantSet1));
		timeThresholdConstants.add(new ConstantValues(timeThresholdConstantSet2));
		timeThresholdConstants.add(new ConstantValues(timeThresholdConstantSet3));
		timeThresholdConstants.add(new ConstantValues(timeThresholdConstantSet4));
		timeThresholdConstants.add(new ConstantValues(timeThresholdConstantSet5));
		constants.put(TimeThresholdActionBasedGraphBuilder.class, timeThresholdConstants);
		
		Collection<ConstantValues> interactionRankConstants = new ArrayList<>();
		Object[] interactionRankConstantSet1 = {1.0, 1000L*60*60*24*7, 0.02}; //wOut=1.0, halfLife=1.0 weeks, threshold=0.02
		Object[] interactionRankConstantSet2 = {1.0, 1000L*60*60*24*7*4, 0.6}; //wOut=1.0, halfLife=4 weeks, threshold=0.6
		Object[] interactionRankConstantSet3 = {1.0, 1000L*60*60*24*365/2, 1.7}; //wOut=1.0, halfLife=0.5 years, threshold=1.7
		Object[] interactionRankConstantSet4 = {1.0, 1000L*60*60*24*365, 1.8}; //wOut=1.0, halfLife=1 year, threshold=1.8
		Object[] interactionRankConstantSet5 = {1.0, 1000L*60*60*24*365*2, 0.01}; //wOut=1.0, halfLife=2 years, threshold=0.01
		interactionRankConstants.add(new ConstantValues(interactionRankConstantSet1));
		interactionRankConstants.add(new ConstantValues(interactionRankConstantSet2));
		interactionRankConstants.add(new ConstantValues(interactionRankConstantSet3));
		interactionRankConstants.add(new ConstantValues(interactionRankConstantSet4));
		interactionRankConstants.add(new ConstantValues(interactionRankConstantSet5));
		constants.put(InteractionRankWeightedActionBasedGraphBuilder.class, interactionRankConstants);
		return constants;
	}

	public static void main(String[] args) throws Exception {

		NewsgroupTestbed testbed = new NewsgroupTestbed();
		testbed.runTestbed();

	}

}
