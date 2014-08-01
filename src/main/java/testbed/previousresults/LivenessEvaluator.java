package testbed.previousresults;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import metrics.response.liveness.ResponseLivenessMetric;
import testbed.dataset.actions.messages.MessageDataset;
import data.representation.actionbased.messages.MessageThread;
import data.representation.actionbased.messages.SingleMessage;

public abstract class LivenessEvaluator<Id, Recipient, Message extends SingleMessage<Recipient>, ThreadType extends MessageThread<Recipient, Message>>
		implements Evaluator {
	
	public static interface LivenessEvaluatorFactory<Id, Recipient, Message extends SingleMessage<Recipient>, ThreadType extends MessageThread<Recipient, Message>> {

		public LivenessEvaluator<Id, Recipient, Message, ThreadType> create(
				MessageDataset<Id, Recipient, Message, ThreadType> dataset,
				Collection<ResponseLivenessMetric> metrics);
	}

	protected MessageDataset<Id, Recipient, Message, ThreadType> dataset;
	protected File resultsFolder;
	protected File livenessFolder;
	
	@Override
	public Collection<Integer> getTestIds() throws IOException {
		return dataset.getResponesTimesTestingTimes().keySet();
	}

	public LivenessEvaluator(MessageDataset<Id, Recipient, Message, ThreadType> dataset) {
		this.dataset = dataset;
		this.resultsFolder = dataset.getResponseTimesResultsFolder();
		livenessFolder = new File(resultsFolder, "liveness");
	}
}
