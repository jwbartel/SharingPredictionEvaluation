package metrics.response.time;

import java.util.List;

import testbed.dataset.actions.messages.stackoverflow.evaluation.ResponseTimeEvaluator.ResponseTimeRange;

public class PercentWithinRangeMetric extends ResponseTimeMetric {

	public static ResponseTimeMetricFactory factory() {
		return new ResponseTimeMetricFactory() {

			@Override
			public ResponseTimeMetric create() {
				return new PercentWithinRangeMetric();
			}
		};
	}

	@Override
	public String getTitle() {
		return "Percent within range";
	}

	@Override
	public void addTestResult(List<Double> trueTimes,
			List<ResponseTimeRange> responseTimePredictions) {
		int totalPredicted = 0;
		int totalWithinRange = 0;

		for (int i = 0; i < trueTimes.size(); i++) {
			Double trueTime = trueTimes.get(i);
			ResponseTimeRange prediction = responseTimePredictions.get(i);
			if (prediction.minResponseTime != null && prediction.maxResponseTime != null) {
				totalPredicted++;
				if (prediction.minResponseTime <= trueTime && prediction.maxResponseTime >= trueTime) {
					totalWithinRange++;
				}
			}
		}
		
		stats.addValue(((double) totalWithinRange)/totalPredicted);

	}

}
