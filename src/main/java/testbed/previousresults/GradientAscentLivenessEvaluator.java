package testbed.previousresults;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import metrics.MetricResult;
import metrics.response.liveness.ResponseLivenessMetric;

import org.apache.commons.io.FileUtils;

import testbed.dataset.actions.messages.MessageDataset;
import data.representation.actionbased.messages.MessageThread;
import data.representation.actionbased.messages.SingleMessage;

public class GradientAscentLivenessEvaluator<Id, Recipient, Message extends SingleMessage<Recipient>, ThreadType extends MessageThread<Recipient, Message>>
		extends LivenessEvaluator<Id, Recipient, Message, ThreadType> {
	
	public static <Id, Recipient, Message extends SingleMessage<Recipient>, ThreadType extends MessageThread<Recipient, Message>>
			LivenessEvaluatorFactory<Id, Recipient, Message, ThreadType> factory(
					Class<Id> idClass,
					Class<Recipient> recipientClass,
					Class<Message> messageClass,
					Class<ThreadType> threadClass) {
		return new LivenessEvaluatorFactory<Id, Recipient, Message, ThreadType>() {

			@Override
			public LivenessEvaluator<Id, Recipient, Message, ThreadType> create(
					MessageDataset<Id, Recipient, Message, ThreadType> dataset,
					Collection<ResponseLivenessMetric> metrics) {
				return new GradientAscentLivenessEvaluator<>(dataset, metrics);
			}
		};
	}

	private File gradientAscentFolder;
	private Collection<ResponseLivenessMetric> metrics;
	
	public GradientAscentLivenessEvaluator(MessageDataset<Id, Recipient, Message, ThreadType> dataset,
			Collection<ResponseLivenessMetric> metrics) {
		super(dataset);
		this.gradientAscentFolder = new File(livenessFolder, "gradient ascent");
		this.metrics = metrics;
	}
	
	private File getPredictionsFolder() {
		return new File(gradientAscentFolder, "computed liveness");
	}
	
	private List<Double> getPredictedLiveness(Integer test) throws IOException {
		File predictionsFile = new File(getPredictionsFolder(), test + ".csv");
		List<Double> predictions = new ArrayList<Double>();
		List<String> lines = FileUtils.readLines(predictionsFile);
		for (String line : lines) {
			String[] splitLine = line.split(",");
			predictions.add(Double.parseDouble(splitLine[splitLine.length-1]));
		}
		return predictions;
	}

	@Override
	public List<MetricResult> evaluate(Integer testId) {
		try {
			Map<Integer, List<Double>> testingTimes = dataset.getResponesTimesTestingTimes();
			List<Double> trueTimes = testingTimes.get(testId);
			List<Double> predictedLiveness = getPredictedLiveness(testId);
			for (ResponseLivenessMetric metric : metrics) {
				metric.addTestResult(trueTimes, predictedLiveness);
			}
			List<MetricResult> results = new ArrayList<>();
			for (ResponseLivenessMetric metric : metrics) {
				results.add(metric.evaluate());
			}
			return results;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String getType() {
		return "gradient ascent";
	}
}
