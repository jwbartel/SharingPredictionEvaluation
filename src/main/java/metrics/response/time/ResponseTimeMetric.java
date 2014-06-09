package metrics.response.time;

import java.util.List;

import metrics.Metric;
import metrics.MetricResult;
import testbed.dataset.actions.messages.stackoverflow.evaluation.ResponseTimeEvaluator.ResponseTimeRange;

public interface ResponseTimeMetric extends Metric {

	public void addTestResult(List<Double> trueTimes, List<ResponseTimeRange> responseTimePredictions);

	public MetricResult evaluate();
}
