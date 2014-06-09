package metrics.response.liveness;

import java.util.List;

import metrics.Metric;
import metrics.MetricResult;

public interface ResponseLivenessMetric extends Metric {

	public void addTestResult(List<Double> trueTimes, List<Double> livenessPredictions);

	public MetricResult evaluate();
}
