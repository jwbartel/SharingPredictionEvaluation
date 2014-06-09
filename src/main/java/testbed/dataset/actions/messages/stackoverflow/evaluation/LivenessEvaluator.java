package testbed.dataset.actions.messages.stackoverflow.evaluation;

import java.io.File;

import testbed.dataset.actions.messages.stackoverflow.StackOverflowDataset;
import data.representation.actionbased.messages.stackoverflow.StackOverflowMessage;
import data.representation.actionbased.messages.stackoverflow.StackOverflowThread;

public abstract class LivenessEvaluator<Recipient, Message extends StackOverflowMessage<Recipient>, ThreadType extends StackOverflowThread<Recipient, Message>>
		implements Evaluator {

	protected StackOverflowDataset<Recipient, Message, ThreadType> dataset;
	protected File resultsFolder;
	protected File livenessFolder;

	public LivenessEvaluator(StackOverflowDataset<Recipient, Message, ThreadType> dataset) {
		this.dataset = dataset;
		this.resultsFolder = dataset.getResponseTimesResultsFolder();
		livenessFolder = new File(resultsFolder, "liveness");
	}
}
