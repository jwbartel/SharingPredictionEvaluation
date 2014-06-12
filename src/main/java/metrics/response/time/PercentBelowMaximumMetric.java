package metrics.response.time;

import java.util.List;

import testbed.dataset.actions.messages.stackoverflow.evaluation.ResponseTimeEvaluator.ResponseTimeRange;

public class PercentBelowMaximumMetric extends ResponseTimeMetric {

	public static ResponseTimeMetricFactory factory() {
		return new ResponseTimeMetricFactory() {

			@Override
			public ResponseTimeMetric create() {
				return new PercentBelowMaximumMetric();
			}
		};
	}

	@Override
	public String getTitle() {
		return "Percent below maximum";
	}

	@Override
	public void addTestResult(List<Double> trueTimes,
			List<ResponseTimeRange> responseTimePredictions) {
		int totalPredicted = 0;
		int totalbelowMax = 0;

		for (int i = 0; i < trueTimes.size(); i++) {
			Double trueTime = trueTimes.get(i);
			ResponseTimeRange prediction = responseTimePredictions.get(i);
			if (prediction.maxResponseTime != null) {
				totalPredicted++;
				if (prediction.maxResponseTime >= trueTime) {
					totalbelowMax++;
				}
			}
		}
		
		stats.addValue(((double) totalbelowMax)/totalPredicted);

	}

}
