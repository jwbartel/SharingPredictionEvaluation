package model.prediction.liveness;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import prediction.response.liveness.message.MessageLivenessPredictor;
import metrics.MetricResult;
import metrics.response.liveness.ResponseLivenessMetric;
import data.representation.actionbased.messages.MessageThread;
import data.representation.actionbased.messages.SingleMessage;

public class LivenessPredictionEvaluationModeler<RecipientType extends Comparable<RecipientType>, MessageType extends SingleMessage<RecipientType>, ThreadType extends MessageThread<RecipientType, MessageType>> {
	
	private Collection<ThreadType> testThreads;
	private MessageLivenessPredictor<RecipientType, MessageType, ThreadType> predictor;
	private Collection<ResponseLivenessMetric> metrics;
	
	public LivenessPredictionEvaluationModeler(Collection<ThreadType> testThreads,
			MessageLivenessPredictor<RecipientType, MessageType, ThreadType> predictor,
			Collection<ResponseLivenessMetric> metrics) {

		this.testThreads = testThreads;
		this.predictor = predictor;
		this.metrics = metrics;
	}
	
	public Collection<MetricResult> modelPredictionEvaluation() throws Exception {
	
		List<Double> trueResponseTimes = new ArrayList<>(testThreads.size());
		List<Double> predictions = new ArrayList<>(testThreads.size());
		
		for(ThreadType thread : testThreads) {
			trueResponseTimes.add(thread.getTimeToResponse());
			Boolean prediction = predictor.predictLiveness(thread);
			if (prediction == null) {
				predictions.add(null);
			} else if (prediction == true) {
				predictions.add(1.0);
			} else {
				predictions.add(0.0);
			}
		}
		
		Collection<MetricResult> results = new ArrayList<>();
		for (ResponseLivenessMetric metric : metrics) {
			metric.addTestResult(trueResponseTimes, predictions);
			results.add(metric.evaluate());
		}
		return results;
	
	}
}
