package testbed.datasetspecific;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import prediction.response.time.InverseGaussianDistribution;
import prediction.response.time.LogNormalDistribution;
import testbed.ConstantValues;
import testbed.SurveyResultsTestbed;
import testbed.dataset.actions.messages.MessageDataset;
import testbed.dataset.actions.messages.email.ResponseTimeStudyDataSet;
import data.preprocess.graphbuilder.InteractionRankWeightedActionBasedGraphBuilder;
import data.preprocess.graphbuilder.SimpleActionBasedGraphBuilder;
import data.preprocess.graphbuilder.TimeThresholdActionBasedGraphBuilder;
import data.representation.actionbased.messages.email.EmailMessage;
import data.representation.actionbased.messages.email.EmailThread;

public class EmailResponseStudyTestbed
		extends
		MessagesSpecificTestbed<String, String, EmailMessage<String>, EmailThread<String, EmailMessage<String>>> {

	static Class<String> idClass = String.class;
	static Class<String> collaboratorClass = String.class;
	@SuppressWarnings("unchecked")
	static Class<EmailMessage<String>> messageClass = (Class<EmailMessage<String>>) new EmailMessage<String>(null).getClass();
	@SuppressWarnings("unchecked")
	static Class<EmailThread<String, EmailMessage<String>>> threadClass = (Class<EmailThread<String, EmailMessage<String>>>) (new EmailThread<String, EmailMessage<String>>()
			.getClass());
	
	public EmailResponseStudyTestbed() {
		super(idClass, collaboratorClass, messageClass, threadClass);
	}
	
	public Collection<ResponseTimeStudyDataSet> getResponseTimeStudyDatasets() {
	
		Collection<ResponseTimeStudyDataSet> datasets = new ArrayList<>();
		datasets.add(new ResponseTimeStudyDataSet("response time", new File(
				"data/Email Response Study")));
		return datasets;
	}
	
	@Override
	public Collection<MessageDataset<String, String, EmailMessage<String>, EmailThread<String, EmailMessage<String>>>> getMessageDatasets() {
	
		return new ArrayList<MessageDataset<String, String, EmailMessage<String>, EmailThread<String, EmailMessage<String>>>>(getResponseTimeStudyDatasets());
	}

	@Override
	public InverseGaussianDistribution getResponseTimeInverseGaussianDistribution() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LogNormalDistribution getResponseTimeLogNormalDistribution() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Collection<ConstantValues>> getGraphConstants() {
		
		Map<String, Collection<ConstantValues>> constants = new HashMap<>();
		
		Collection<ConstantValues> simpleConstants = new ArrayList<>();
		Object[] simpleConstantSet = {};
		simpleConstants.add(new ConstantValues(simpleConstantSet));
		constants.put(SimpleActionBasedGraphBuilder.class.getName(), simpleConstants);
		
		Collection<ConstantValues> timeThresholdConstants = new ArrayList<>();
		Object[] timeThresholdConstantSet1 = {1000L*60*60*24*7*2}; //2.0 weeks
		Object[] timeThresholdConstantSet2 = {1000L*60*60*24*7*4}; //1 month
		timeThresholdConstants.add(new ConstantValues(timeThresholdConstantSet1));
		timeThresholdConstants.add(new ConstantValues(timeThresholdConstantSet2));
		constants.put(TimeThresholdActionBasedGraphBuilder.class.getName(), timeThresholdConstants);
		
		Collection<ConstantValues> interactionRankConstants = new ArrayList<>();
		Object[] interactionRankConstantSet1 = {1.0, 1000L*60*60*24*7, 0.25}; //wOut=1.0, halfLife=1.0 weeks, threshold=0.25
		Object[] interactionRankConstantSet2 = {1.0, 1000L*60*60*24*7*2, 0.25}; //wOut=1.0, halfLife=2.0 weeks, threshold=0.25
		Object[] interactionRankConstantSet3 = {1.0, 1000L*60*60*24*7*4, 0.25}; //wOut=1.0, halfLife=1 month, threshold=0.25
		Object[] interactionRankConstantSet4 = {1.0, 1000L*60*60*24*7*4*2, 0.25}; //wOut=1.0, halfLife=2 months, threshold=0.25
		interactionRankConstants.add(new ConstantValues(interactionRankConstantSet1));
		interactionRankConstants.add(new ConstantValues(interactionRankConstantSet2));
		interactionRankConstants.add(new ConstantValues(interactionRankConstantSet3));
		interactionRankConstants.add(new ConstantValues(interactionRankConstantSet4));
		constants.put(InteractionRankWeightedActionBasedGraphBuilder.class.getName(), interactionRankConstants);
		
		return constants;
	}
	
	@Override
	public void runTestbed() throws Exception{
//		super.runTestbed();
		runResponseStudyTests(getResponseTimeStudyDatasets());
	}
	
	
	public void runResponseStudyTests(Collection<ResponseTimeStudyDataSet> datasets) throws Exception {
		SurveyResultsTestbed testbed = new SurveyResultsTestbed(datasets);
		testbed.runTestbed();
	}

	
	public static void main(String[] args) throws Exception {

		EmailResponseStudyTestbed testbed = new EmailResponseStudyTestbed();
		testbed.runTestbed();

	}

}
