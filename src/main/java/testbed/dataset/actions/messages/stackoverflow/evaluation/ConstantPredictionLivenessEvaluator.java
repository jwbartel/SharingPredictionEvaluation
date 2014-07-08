package testbed.dataset.actions.messages.stackoverflow.evaluation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import metrics.MetricResult;
import metrics.response.liveness.ResponseLivenessMetric;
import testbed.dataset.actions.messages.stackoverflow.StackOverflowDataset;
import data.representation.actionbased.messages.stackoverflow.StackOverflowMessage;
import data.representation.actionbased.messages.stackoverflow.StackOverflowThread;

public class ConstantPredictionLivenessEvaluator<Recipient, Message extends StackOverflowMessage<Recipient>, ThreadType extends StackOverflowThread<Recipient, Message>>
		extends LivenessEvaluator<Recipient, Message, ThreadType> {
	
	public static <Recipient, Message extends StackOverflowMessage<Recipient>, ThreadType extends StackOverflowThread<Recipient, Message>>
			LivenessEvaluatorFactory<Recipient, Message, ThreadType> factory(
					Class<Recipient> recipientClass,
					Class<Message> messageClass,
					Class<ThreadType> threadClass,
					final String label, final double prediction) {
		return new LivenessEvaluatorFactory<Recipient, Message, ThreadType>() {

			@Override
			public LivenessEvaluator<Recipient, Message, ThreadType> create(
					StackOverflowDataset<Recipient, Message, ThreadType> dataset,
					Collection<ResponseLivenessMetric> metrics) {
				return new ConstantPredictionLivenessEvaluator<>(label, prediction, dataset,
						metrics);
			}
		};
	}

	private String label;
	private Double prediction;
	private Collection<ResponseLivenessMetric> metrics;
	
	public ConstantPredictionLivenessEvaluator(String label, double prediction, StackOverflowDataset<Recipient, Message, ThreadType> dataset,
			Collection<ResponseLivenessMetric> metrics) {
		super(dataset);
		this.label = label;
		this.prediction = prediction;
		this.metrics = metrics;
	}
	
	private List<Double> getPredictedLiveness(List<Double> testingTimes) throws IOException {
		List<Double> predictions = new ArrayList<Double>();
		for (int i=0; i<testingTimes.size(); i++) {
			predictions.add(prediction);
		}
		return predictions;
	}

	@Override
	public List<MetricResult> evaluate(Integer testId) {
		try {
			Map<Integer, List<Double>> testingTimes = dataset.getResponesTimesTestingTimes();
			
			List<Double> trueTimes = testingTimes.get(testId);
			List<Double> predictedLiveness = getPredictedLiveness(trueTimes);
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
		return label;
	}
}
