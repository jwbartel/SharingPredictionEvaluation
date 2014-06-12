package metrics.response.time;

import java.util.List;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import testbed.dataset.actions.messages.stackoverflow.evaluation.ResponseTimeEvaluator.ResponseTimeRange;

public class PercentWithinErrorThresholdMetric extends MinOrMaxResponseTimeMetric {

	private final String label;
	private final Double errorThreshold;

	public static ResponseTimeMetricFactory factory(final MinOrMaxType type,
			final String label,
			final Double errorThreshold) {
		return new ResponseTimeMetricFactory() {

			@Override
			public ResponseTimeMetric create() {
				return new PercentWithinErrorThresholdMetric(type, label, errorThreshold);
			}
		};
	}

	public PercentWithinErrorThresholdMetric(MinOrMaxType type, String label, Double errorThreshold) {
		super(type);
		this.label = label;
		this.errorThreshold = errorThreshold;
	}

	@Override
	public String getTitle() {
		return "Percent of " + type.toString() + " with "+label;
	}

	@Override
	public void addTestResult(List<Double> trueTimes,
			List<ResponseTimeRange> responseTimePredictions) {

		int numPredictions = 0;
		int numWithinBounds = 0;

		for (int i = 0; i < trueTimes.size(); i++) {
			Double trueTime = trueTimes.get(i);
			ResponseTimeRange predictedRange = responseTimePredictions.get(i);
			Double prediction = getPredictionValue(predictedRange);
			if (prediction != null && trueTime != null && trueTime != Double.POSITIVE_INFINITY
					&& prediction != Double.POSITIVE_INFINITY) {

				numPredictions++;
				Double error = Math.abs(prediction - trueTime);
				if (error <= errorThreshold) {
					numWithinBounds++;
				}
			}
		}

		stats.addValue(((double) numWithinBounds)/numPredictions);
	}

}
