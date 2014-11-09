package testbed.datasetspecific;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import prediction.response.time.InverseGaussianDistribution;
import prediction.response.time.LogNormalDistribution;
import testbed.ConstantValues;
import testbed.dataset.actions.messages.MessageDataset;
import testbed.dataset.actions.messages.stackoverflow.SampledStackOverflowDataset;
import data.preprocess.graphbuilder.InteractionRankWeightedActionBasedGraphBuilder;
import data.preprocess.graphbuilder.SimpleActionBasedGraphBuilder;
import data.preprocess.graphbuilder.TimeThresholdActionBasedGraphBuilder;
import data.representation.actionbased.messages.stackoverflow.StackOverflowMessage;
import data.representation.actionbased.messages.stackoverflow.StackOverflowThread;

public class StackOverflowTestbed
		extends
		MessagesSpecificTestbed<Long, String, StackOverflowMessage<String>, StackOverflowThread<String, StackOverflowMessage<String>>> {

	static Class<Long> idClass = Long.class;
	static Class<String> collaboratorClass = String.class;
	@SuppressWarnings("unchecked")
	static Class<StackOverflowMessage<String>> messageClass = (Class<StackOverflowMessage<String>>) new StackOverflowMessage<String>(0,null,null,0,null,null,null,false).getClass();
	@SuppressWarnings("unchecked")
	static Class<StackOverflowThread<String, StackOverflowMessage<String>>> threadClass = (Class<StackOverflowThread<String, StackOverflowMessage<String>>>) (new StackOverflowThread<String, StackOverflowMessage<String>>()
			.getClass());

	public StackOverflowTestbed() {
		super(idClass, collaboratorClass, messageClass, threadClass);
	}

	@Override
	public Collection<MessageDataset<Long, String, StackOverflowMessage<String>, StackOverflowThread<String, StackOverflowMessage<String>>>> getMessageDatasets() {

		Collection<MessageDataset<Long, String, StackOverflowMessage<String>, StackOverflowThread<String, StackOverflowMessage<String>>>> datasets = new ArrayList<>();

		try {
			datasets.add(new SampledStackOverflowDataset("Sampled StackOverflow", new File(
								"data/Stack Overflow/10000 Random Questions")));
		} catch (IOException | ParseException e) {
			e.printStackTrace();
			System.exit(0);
		}

		return datasets;
	}

	@Override
	public InverseGaussianDistribution getResponseTimeInverseGaussianDistribution() {
		return new InverseGaussianDistribution(867.482, 571.108);
	}

	@Override
	public LogNormalDistribution getResponseTimeLogNormalDistribution() {
		return new LogNormalDistribution(6.35702, 0.927127);
	}

	@Override
	public Map<String, Collection<ConstantValues>> getGraphConstants() {

		Map<String, Collection<ConstantValues>> constants = new HashMap<>();
		
		Collection<ConstantValues> simpleConstants = new ArrayList<>();
		Object[] simpleConstantSet = {};
		simpleConstants.add(new ConstantValues(simpleConstantSet));
		constants.put(SimpleActionBasedGraphBuilder.class.getName(), simpleConstants);
		
		Collection<ConstantValues> timeThresholdConstants = new ArrayList<>();
		Object[] timeThresholdConstantSet = {1000L*60*60*24*365*2}; //2 years
		timeThresholdConstants.add(new ConstantValues(timeThresholdConstantSet));
		constants.put(TimeThresholdActionBasedGraphBuilder.class.getName(), timeThresholdConstants);
		
		Collection<ConstantValues> interactionRankConstants = new ArrayList<>();
		Object[] interactionRankConstantSet = {1.0, 1000L*60*60*24*365*2, 0.01}; //wOut=1.0, halfLife=2 years, threshold=0.01
		interactionRankConstants.add(new ConstantValues(interactionRankConstantSet));
		constants.put(InteractionRankWeightedActionBasedGraphBuilder.class.getName(), interactionRankConstants);
		return constants;
	}

	public static void main(String[] args) throws Exception {

		StackOverflowTestbed testbed = new StackOverflowTestbed();
		testbed.runTestbed();
		
//		SampledStackOverflowDataset dataset = new SampledStackOverflowDataset("Sampled StackOverflow", new File(
//				"data/Stack Overflow/10000 Random Questions"));
//		dataset.printStats();

	}

}
