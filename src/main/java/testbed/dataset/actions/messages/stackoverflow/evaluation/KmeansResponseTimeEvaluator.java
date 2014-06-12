package testbed.dataset.actions.messages.stackoverflow.evaluation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import metrics.response.time.ResponseTimeMetric;

import org.apache.commons.io.FileUtils;

import testbed.dataset.actions.messages.stackoverflow.StackOverflowDataset;
import data.representation.actionbased.messages.stackoverflow.StackOverflowMessage;
import data.representation.actionbased.messages.stackoverflow.StackOverflowThread;

public class KmeansResponseTimeEvaluator<Recipient, Message extends StackOverflowMessage<Recipient>, ThreadType extends StackOverflowThread<Recipient, Message>>
		extends ResponseTimeEvaluator<Recipient, Message, ThreadType> {

	private int k;
	private File weightedKmeansFolder;
	
	public KmeansResponseTimeEvaluator(
			StackOverflowDataset<Recipient, Message, ThreadType> dataset,
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
			return Double.parseDouble(timeStr);
		}
	}

	@Override
	protected List<ResponseTimeRange> getPredictedResponseTimes(Integer test) throws IOException {
		
		File predictionsFolder = new File(weightedKmeansFolder, "predictions");
		File predictionsFile = new File(new File(predictionsFolder, ""+test), ""+k);
		
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
		return "k-means";
	}
}
