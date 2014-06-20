package model.prediction.responsetime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import prediction.response.liveness.message.MessageLivenessPredictor;
import prediction.response.time.ResponseTimeRange;
import prediction.response.time.message.MessageResponseTimePredictor;
import metrics.MetricResult;
import metrics.response.liveness.ResponseLivenessMetric;
import metrics.response.time.ResponseTimeMetric;
import data.representation.actionbased.messages.MessageThread;
import data.representation.actionbased.messages.SingleMessage;

public class ResponseTimePredictionEvaluationModeler<RecipientType extends Comparable<RecipientType>, MessageType extends SingleMessage<RecipientType>, ThreadType extends MessageThread<RecipientType, MessageType>> {
	
	private Collection<ThreadType> testThreads;
	private MessageResponseTimePredictor<RecipientType, MessageType, ThreadType> predictor;
	private Collection<ResponseTimeMetric> metrics;
	
	public ResponseTimePredictionEvaluationModeler(Collection<ThreadType> testThreads,
			MessageResponseTimePredictor<RecipientType, MessageType, ThreadType> predictor,
			Collection<ResponseTimeMetric> metrics) {

		this.testThreads = testThreads;
		this.predictor = predictor;
		this.metrics = metrics;
	}
	
	public Collection<MetricResult> modelPredictionEvaluation() throws Exception {
	
		List<Double> trueResponseTimes = new ArrayList<>(testThreads.size());
		List<ResponseTimeRange> predictions = new ArrayList<>(testThreads.size());
		
		for(ThreadType thread : testThreads) {
			trueResponseTimes.add(thread.getTimeToResponse());
			ResponseTimeRange prediction = predictor.predictResponseTime(thread);
			predictions.add(prediction);
		}
		
		Collection<MetricResult> results = new ArrayList<>();
		for (ResponseTimeMetric metric : metrics) {
			metric.addTestResult(trueResponseTimes, predictions);
			results.add(metric.evaluate());
		}
		return results;
	
	}
}
