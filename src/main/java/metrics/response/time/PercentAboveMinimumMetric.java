package metrics.response.time;

import java.util.List;

import prediction.response.time.ResponseTimeRange;

public class PercentAboveMinimumMetric extends ResponseTimeMetric {

	public static ResponseTimeMetricFactory factory() {
		return new ResponseTimeMetricFactory() {

			@Override
			public ResponseTimeMetric create() {
				return new PercentAboveMinimumMetric();
			}
		};
	}

	@Override
	public String getTitle() {
		return "Percent above minimum";
	}

	@Override
	public void addTestResult(List<Double> trueTimes,
			List<ResponseTimeRange> responseTimePredictions) {
		int totalPredicted = 0;
		int totalAboveMin = 0;

		for (int i = 0; i < trueTimes.size(); i++) {
			Double trueTime = trueTimes.get(i);
			ResponseTimeRange prediction = responseTimePredictions.get(i);
			if (prediction.minResponseTime != null) {
				totalPredicted++;
				if (prediction.minResponseTime <= trueTime) {
					totalAboveMin++;
				}
			}
		}
		
		stats.addValue(((double) totalAboveMin)/totalPredicted);

	}

}
