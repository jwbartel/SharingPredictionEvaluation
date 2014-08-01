package testbed.previousresults;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;

import metrics.MetricResult;
import metrics.response.liveness.ResponseLivenessMetric;
import testbed.dataset.actions.messages.MessageDataset;
import data.representation.actionbased.messages.MessageThread;
import data.representation.actionbased.messages.SingleMessage;

public class RandomPredictionLivenessEvaluator<Id, Recipient, Message extends SingleMessage<Recipient>, ThreadType extends MessageThread<Recipient, Message>>
		extends LivenessEvaluator<Id, Recipient, Message, ThreadType> {
	
	public static <Id, Recipient, Message extends SingleMessage<Recipient>, ThreadType extends MessageThread<Recipient, Message>>
			LivenessEvaluatorFactory<Id, Recipient, Message, ThreadType> factory(
					Class<Id> idClass,
					Class<Recipient> recipientClass,
					Class<Message> messageClass,
					Class<ThreadType> threadClass,
					final String label, final double prediction) {
		return new LivenessEvaluatorFactory<Id, Recipient, Message, ThreadType>() {

			@Override
			public LivenessEvaluator<Id, Recipient, Message, ThreadType> create(
					MessageDataset<Id, Recipient, Message, ThreadType> dataset,
					Collection<ResponseLivenessMetric> metrics) {
				return new RandomPredictionLivenessEvaluator<>(label, prediction, dataset,
						metrics);
			}
		};
	}

	private String label;
	private Double percentLive;
	private Collection<ResponseLivenessMetric> metrics;
	
	public RandomPredictionLivenessEvaluator(String label, double percentLive, MessageDataset<Id, Recipient, Message, ThreadType> dataset,
			Collection<ResponseLivenessMetric> metrics) {
		super(dataset);
		this.label = label;
		this.percentLive = percentLive;
		this.metrics = metrics;
	}
	
	private List<Double> getPredictedLiveness(List<Double> testingTimes) throws IOException {
		Random rand = new Random();
		List<Double> predictions = new ArrayList<Double>();
		for (int i=0; i<testingTimes.size(); i++) {
			double randVal = rand.nextDouble();
			double prediction = (randVal < percentLive)? 1.0 : 0.0;
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
