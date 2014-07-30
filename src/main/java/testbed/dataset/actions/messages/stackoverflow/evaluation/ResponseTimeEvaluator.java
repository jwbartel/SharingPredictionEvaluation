package testbed.dataset.actions.messages.stackoverflow.evaluation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import metrics.MetricResult;
import metrics.response.time.ResponseTimeMetric;
import prediction.response.time.ResponseTimeRange;
import testbed.dataset.actions.messages.MessageDataset;
import testbed.dataset.actions.messages.stackoverflow.StackOverflowDataset;
import data.representation.actionbased.messages.MessageThread;
import data.representation.actionbased.messages.SingleMessage;

public abstract class ResponseTimeEvaluator<Id, Recipient, Message extends SingleMessage<Recipient>, ThreadType extends MessageThread<Recipient, Message>>
		implements Evaluator {
	
	public static interface ResponseTimeEvaluatorFactory<Id, Recipient, Message extends SingleMessage<Recipient>, ThreadType extends MessageThread<Recipient, Message>> {

		public ResponseTimeEvaluator<Id, Recipient, Message, ThreadType> create(
				MessageDataset<Id, Recipient, Message, ThreadType> dataset,
				Collection<ResponseTimeMetric> metrics);
	}
	
	protected MessageDataset<Id, Recipient, Message, ThreadType> dataset;
	protected Collection<ResponseTimeMetric> metrics;
	protected File resultsFolder;
	protected File timeFolder;

	public ResponseTimeEvaluator(MessageDataset<Id, Recipient, Message, ThreadType> dataset,
			Collection<ResponseTimeMetric> metrics) {
		this.dataset = dataset;
		this.resultsFolder = dataset.getResponseTimesResultsFolder();
		this.timeFolder = new File(resultsFolder, "time");
		this.metrics = metrics;
	}
	
	protected abstract List<ResponseTimeRange> getPredictedResponseTimes(Integer test) throws IOException;
	
	@Override
	public Collection<Integer> getTestIds() throws IOException {
		return dataset.getResponesTimesTestingTimes().keySet();
	}
	
	@Override
	public List<MetricResult> evaluate(Integer testId) {
		try {
			Map<Integer, List<Double>> testingTimes = dataset.getResponesTimesTestingTimes();
			List<Double> trueTimes = testingTimes.get(testId);
			List<ResponseTimeRange> predictedResponseTimes = getPredictedResponseTimes(testId);
			for (ResponseTimeMetric metric : metrics) {
				metric.addTestResult(trueTimes, predictedResponseTimes);
			}
			List<MetricResult> results = new ArrayList<>();
			for (ResponseTimeMetric metric : metrics) {
				results.add(metric.evaluate());
			}
			return results;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
