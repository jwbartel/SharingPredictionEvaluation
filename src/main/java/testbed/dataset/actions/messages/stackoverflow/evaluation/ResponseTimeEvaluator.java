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
import testbed.dataset.actions.messages.stackoverflow.StackOverflowDataset;
import data.representation.actionbased.messages.stackoverflow.StackOverflowMessage;
import data.representation.actionbased.messages.stackoverflow.StackOverflowThread;

public abstract class ResponseTimeEvaluator<Recipient, Message extends StackOverflowMessage<Recipient>, ThreadType extends StackOverflowThread<Recipient, Message>>
		implements Evaluator {
	
	public static interface ResponseTimeEvaluatorFactory<Recipient, Message extends StackOverflowMessage<Recipient>, ThreadType extends StackOverflowThread<Recipient, Message>> {

		public ResponseTimeEvaluator<Recipient, Message, ThreadType> create(
				StackOverflowDataset<Recipient, Message, ThreadType> dataset,
				Collection<ResponseTimeMetric> metrics);
	}
	
	protected StackOverflowDataset<Recipient, Message, ThreadType> dataset;
	protected Collection<ResponseTimeMetric> metrics;
	protected File resultsFolder;
	protected File timeFolder;

	public ResponseTimeEvaluator(StackOverflowDataset<Recipient, Message, ThreadType> dataset,
			Collection<ResponseTimeMetric> metrics) {
		this.dataset = dataset;
		this.resultsFolder = dataset.getResponseTimesResultsFolder();
		this.timeFolder = new File(resultsFolder, "time");
		this.metrics = metrics;
	}
	
	protected abstract List<ResponseTimeRange> getPredictedResponseTimes(Integer test) throws IOException;
	
	@Override
	public List<MetricResult> evaluate() {
		try {
			Map<Integer, List<Double>> testingTimes = dataset.getResponesTimesTestingTimes();
			for (Integer test : testingTimes.keySet()) {
				List<Double> trueTimes = testingTimes.get(test);
				List<ResponseTimeRange> predictedResponseTimes = getPredictedResponseTimes(test);
				for (ResponseTimeMetric metric : metrics) {
					metric.addTestResult(trueTimes, predictedResponseTimes);
				}
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
