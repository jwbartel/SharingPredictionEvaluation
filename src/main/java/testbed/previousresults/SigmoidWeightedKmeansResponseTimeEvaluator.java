package testbed.previousresults;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import metrics.response.time.ResponseTimeMetric;

import org.apache.commons.io.FileUtils;

import prediction.response.time.ResponseTimeRange;
import testbed.dataset.actions.messages.MessageDataset;
import data.representation.actionbased.messages.MessageThread;
import data.representation.actionbased.messages.SingleMessage;

public class SigmoidWeightedKmeansResponseTimeEvaluator<Id, Recipient, Message extends SingleMessage<Recipient>, ThreadType extends MessageThread<Recipient, Message>>
		extends ResponseTimeEvaluator<Id, Recipient, Message, ThreadType> {
	
	public static <Id, Recipient, Message extends SingleMessage<Recipient>, ThreadType extends MessageThread<Recipient, Message>>
	ResponseTimeEvaluatorFactory<Id, Recipient, Message, ThreadType> factory(
			Class<Id> idClass,
			Class<Recipient> recipientClass,
			Class<Message> messageClass,
			Class<ThreadType> threadClass,
			final int k) {
		return new ResponseTimeEvaluatorFactory<Id, Recipient, Message, ThreadType>() {

			@Override
			public ResponseTimeEvaluator<Id, Recipient, Message, ThreadType> create(
					MessageDataset<Id, Recipient, Message, ThreadType> dataset,
					Collection<ResponseTimeMetric> metrics) {
				return new SigmoidWeightedKmeansResponseTimeEvaluator<>(dataset, k, metrics);
			}
		};
	}

	private int k;
	private File weightedKmeansFolder;
	
	public SigmoidWeightedKmeansResponseTimeEvaluator(
			MessageDataset<Id, Recipient, Message, ThreadType> dataset,
			int k,
			Collection<ResponseTimeMetric> metrics) {
		super(dataset, metrics);
		this.k = k;
		this.weightedKmeansFolder = new File(timeFolder, "sigmoid weighted k-means");
	}
	
	private Double parseTime(String timeStr) {
		if (timeStr.equals("Inf")) {
			return Double.POSITIVE_INFINITY;
		} else {
			return Double.parseDouble(timeStr)*60*60;
		}
	}

	@Override
	protected List<ResponseTimeRange> getPredictedResponseTimes(Integer test) throws IOException {
		
		File predictionsFolder = new File(weightedKmeansFolder, "predictions");
		File predictionsFile = new File(new File(predictionsFolder, ""+test), k+".csv");
		
		List<ResponseTimeRange> predictions = new ArrayList<>();
		List<String> lines = FileUtils.readLines(predictionsFile);
		for (String line : lines) {
			String[] lineParts = line.split(",");
			Double minTime = parseTime(lineParts[0]);
			Double maxTime = parseTime(lineParts[1]);
			predictions.add(new ResponseTimeRange(minTime, maxTime));
		}
		return predictions;
	}

	@Override
	public String getType() {
		return "sigmoid weighted k-means,"+k;
	}
}
