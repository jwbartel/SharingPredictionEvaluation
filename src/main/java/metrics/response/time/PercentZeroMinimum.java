package metrics.response.time;

import java.util.List;

import testbed.dataset.actions.messages.stackoverflow.evaluation.ResponseTimeEvaluator.ResponseTimeRange;

public class PercentZeroMinimum extends ResponseTimeMetric {

	public static ResponseTimeMetricFactory factory() {
		return new ResponseTimeMetricFactory() {

			@Override
			public ResponseTimeMetric create() {
				return new PercentZeroMinimum();
			}
		};
	}

	@Override
	public String getTitle() {
		return "Zero minimum";
	}

	@Override
	public void addTestResult(List<Double> trueTimes,
			List<ResponseTimeRange> responseTimePredictions) {
		int totalPredicted = 0;
		int totalZero = 0;

		for (int i = 0; i < trueTimes.size(); i++) {
			ResponseTimeRange prediction = responseTimePredictions.get(i);
			if (prediction.minResponseTime != null) {
				totalPredicted++;
				if (prediction.minResponseTime == 0) {
					totalZero++;
				}
			}
		}
		
		stats.addValue(((double) totalZero)/totalPredicted);

	}

}
