package metrics.response.time;

import java.util.List;

import prediction.response.time.ResponseTimeRange;

public class PercentInfiniteMaximumMetric extends ResponseTimeMetric {

	public static ResponseTimeMetricFactory factory() {
		return new ResponseTimeMetricFactory() {

			@Override
			public ResponseTimeMetric create() {
				return new PercentInfiniteMaximumMetric();
			}
		};
	}

	@Override
	public String getTitle() {
		return "Percent infinite maximum";
	}

	@Override
	public void addTestResult(List<Double> trueTimes,
			List<ResponseTimeRange> responseTimePredictions) {
		int totalPredicted = 0;
		int totalInfiniteMax = 0;

		for (int i = 0; i < trueTimes.size(); i++) {
			ResponseTimeRange prediction = responseTimePredictions.get(i);
			if (prediction.maxResponseTime != null) {
				totalPredicted++;
				if (prediction.maxResponseTime == Double.POSITIVE_INFINITY) {
					totalInfiniteMax++;
				}
			}
		}
		
		stats.addValue(((double) totalInfiniteMax)/totalPredicted);

	}

}
