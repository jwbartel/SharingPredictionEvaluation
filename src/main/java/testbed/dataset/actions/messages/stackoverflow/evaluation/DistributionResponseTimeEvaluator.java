package testbed.dataset.actions.messages.stackoverflow.evaluation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import metrics.response.time.ResponseTimeMetric;

import org.apache.commons.io.FileUtils;

import testbed.dataset.actions.messages.stackoverflow.StackOverflowDataset;
import data.representation.actionbased.messages.stackoverflow.StackOverflowMessage;
import data.representation.actionbased.messages.stackoverflow.StackOverflowThread;

public class DistributionResponseTimeEvaluator<Recipient, Message extends StackOverflowMessage<Recipient>, ThreadType extends StackOverflowThread<Recipient, Message>>
		extends ResponseTimeEvaluator<Recipient, Message, ThreadType> {

	public static <Recipient, Message extends StackOverflowMessage<Recipient>, ThreadType extends StackOverflowThread<Recipient, Message>> ResponseTimeEvaluatorFactory<Recipient, Message, ThreadType> factory(
			Class<Recipient> recipientClass,
			Class<Message> messageClass,
			Class<ThreadType> threadClass,
			final DistributionResponseTimePredictor predictor) {
		return new ResponseTimeEvaluatorFactory<Recipient, Message, ThreadType>() {

			@Override
			public ResponseTimeEvaluator<Recipient, Message, ThreadType> create(
					StackOverflowDataset<Recipient, Message, ThreadType> dataset,
					Collection<ResponseTimeMetric> metrics) {
				return new DistributionResponseTimeEvaluator<>(dataset, metrics, predictor);
			}
		};
	}

	private DistributionResponseTimePredictor predictor;
	private File gradientAscentFolder;

	public DistributionResponseTimeEvaluator(
			StackOverflowDataset<Recipient, Message, ThreadType> dataset,
			Collection<ResponseTimeMetric> metrics, DistributionResponseTimePredictor predictor) {
		super(dataset, metrics);
		this.predictor = predictor;
		this.gradientAscentFolder = new File(new File(timeFolder, "gradient ascent"),
				"expected times");
	}

	@Override
	protected List<ResponseTimeRange> getPredictedResponseTimes(Integer test) throws IOException {
		File resultsFolder = new File(gradientAscentFolder, "" + test);
		File[] iterationFiles = resultsFolder.listFiles();
		Arrays.sort(iterationFiles, new Comparator<File>() {

			@Override
			public int compare(File o1, File o2) {
				Integer val1 = Integer.parseInt(o1.getName());
				Integer val2 = Integer.parseInt(o2.getName());
				return val1.compareTo(val2);
			}
		});
		List<String> lines = FileUtils.readLines(iterationFiles[iterationFiles.length - 1]);
		List<ResponseTimeRange> predictions = new ArrayList<>();
		for (String line : lines) {
			predictions.add(new ResponseTimeRange(predictor.getPrediction(), null));
		}
		return predictions;
	}

	@Override
	public String getType() {
		return "distribution-based prediction - " + predictor.getLabel() + ",N/A";
	}
}
