package testbed.dataset.actions.messages.stackoverflow.evaluation;

import java.io.File;
import java.util.Collection;

import metrics.response.liveness.ResponseLivenessMetric;
import testbed.dataset.actions.messages.stackoverflow.StackOverflowDataset;
import data.representation.actionbased.messages.stackoverflow.StackOverflowMessage;
import data.representation.actionbased.messages.stackoverflow.StackOverflowThread;

public abstract class LivenessEvaluator<Recipient, Message extends StackOverflowMessage<Recipient>, ThreadType extends StackOverflowThread<Recipient, Message>>
		implements Evaluator {
	
	public static interface LivenessEvaluatorFactory<Recipient, Message extends StackOverflowMessage<Recipient>, ThreadType extends StackOverflowThread<Recipient, Message>> {

		public LivenessEvaluator<Recipient, Message, ThreadType> create(
				StackOverflowDataset<Recipient, Message, ThreadType> dataset,
				Collection<ResponseLivenessMetric> metrics);
	}

	protected StackOverflowDataset<Recipient, Message, ThreadType> dataset;
	protected File resultsFolder;
	protected File livenessFolder;

	public LivenessEvaluator(StackOverflowDataset<Recipient, Message, ThreadType> dataset) {
		this.dataset = dataset;
		this.resultsFolder = dataset.getResponseTimesResultsFolder();
		livenessFolder = new File(resultsFolder, "liveness");
	}
}
