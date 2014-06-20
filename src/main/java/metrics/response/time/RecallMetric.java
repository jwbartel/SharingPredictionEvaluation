package metrics.response.time;

import java.util.List;

import prediction.response.time.ResponseTimeRange;

public class RecallMetric extends ResponseTimeMetric {

	public static ResponseTimeMetricFactory factory() {
		return new ResponseTimeMetricFactory() {

			@Override
			public ResponseTimeMetric create() {
				return new RecallMetric();
			}
		};
	}

	@Override
	public String getTitle() {
		return "Recall";
	}

	@Override
	public void addTestResult(List<Double> trueTimes,
			List<ResponseTimeRange> responseTimePredictions) {
		int totalTimes = trueTimes.size();
		int totalPredictedTimes = 0;

		for (int i = 0; i < trueTimes.size(); i++) {
			ResponseTimeRange prediction = responseTimePredictions.get(i);
			if (prediction.minResponseTime != null || prediction.maxResponseTime != null) {
				totalPredictedTimes++;
			}
		}

		stats.addValue(((double) totalPredictedTimes) / totalTimes);

	}

}
