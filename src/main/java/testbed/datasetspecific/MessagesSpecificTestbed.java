package testbed.datasetspecific;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import prediction.response.time.InverseGaussianDistribution;
import prediction.response.time.LogNormalDistribution;
import testbed.PreviousResultsResponseTimeTestbed;
import testbed.ResponseTimeTestbed;
import testbed.dataset.actions.ActionsDataSet;
import testbed.dataset.actions.messages.MessageDataset;
import data.representation.actionbased.messages.MessageThread;
import data.representation.actionbased.messages.SingleMessage;

public abstract class MessagesSpecificTestbed<Id, Collaborator extends Comparable<Collaborator>, Message extends SingleMessage<Collaborator>, MsgThread extends MessageThread<Collaborator, Message>>
		extends ActionsSpecificTestbed<Id, Collaborator, Message, MsgThread> {

	public MessagesSpecificTestbed(Class<Id> idClass,
			Class<Collaborator> collaboratorClass,
			Class<Message> messageClass, Class<MsgThread> threadClass) {

		super(idClass, collaboratorClass, messageClass, threadClass);
	}
	
	@Override
	public void runTestbed() throws Exception{
		Collection<MessageDataset<Id, Collaborator, Message, MsgThread>> datasets = getMessageDatasets();
		
		Collection<ActionsDataSet<Id, Collaborator, Message, MsgThread>> actionDatasets = new ArrayList<>();
		for (MessageDataset<Id, Collaborator, Message, MsgThread> dataset : datasets) {
			actionDatasets.add(dataset);
		}
		runActionTests(actionDatasets);
		
		runMessageTests(datasets);
		
	}
	
	public void runMessageTests(
			Collection<MessageDataset<Id, Collaborator, Message, MsgThread>> datasets)
			throws Exception {
		runPreviousResultsResponseTimeEvaluation(datasets);
		runResponseTimeTests(datasets);
	}
	
	public abstract Collection<MessageDataset<Id, Collaborator, Message, MsgThread>> getMessageDatasets();
	
	public Collection<ActionsDataSet<Id, Collaborator, Message, MsgThread>> getActionsDatasets() {
		return new ArrayList<ActionsDataSet<Id, Collaborator, Message, MsgThread>> (getMessageDatasets());
	}
	
	public abstract InverseGaussianDistribution getResponseTimeInverseGaussianDistribution();
	public abstract LogNormalDistribution getResponseTimeLogNormalDistribution();
	
	public void runPreviousResultsResponseTimeEvaluation(Collection<MessageDataset<Id, Collaborator, Message, MsgThread>> datasets) throws IOException {
		PreviousResultsResponseTimeTestbed<Id, Collaborator, Message, MsgThread> previousResultsResponseTimeTestbed =
				new PreviousResultsResponseTimeTestbed<Id, Collaborator, Message, MsgThread>(
						datasets, idClass, collaboratorClass, actionClass, threadClass);
		previousResultsResponseTimeTestbed.runTestbed();
	}
	
	public void runResponseTimeTests(Collection<MessageDataset<Id, Collaborator, Message, MsgThread>> datasets) throws Exception {
		ResponseTimeTestbed<Id, Collaborator, Message, MsgThread> testbed =
				new ResponseTimeTestbed<>(
						datasets, 
						getResponseTimeInverseGaussianDistribution(),
						getResponseTimeLogNormalDistribution(),
						collaboratorClass, actionClass, threadClass);
		testbed.runTestbed();
	}
}
