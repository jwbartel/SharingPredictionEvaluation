package metrics.response.time;

import java.util.List;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import prediction.response.time.ResponseTimeRange;

public class RelativeErrorMetric extends MinOrMaxResponseTimeMetric {

	public static ResponseTimeMetricFactory factory(final MinOrMaxType type) {
		return new ResponseTimeMetricFactory() {
			
			@Override
			public ResponseTimeMetric create() {
				return new RelativeErrorMetric(type);
			}
		};
	}
	
	public RelativeErrorMetric(MinOrMaxType type) {
		super(type);
	}

	@Override
	public String getTitle() {
		return "Relative Error of "+type.toString();
	}

	@Override
	public void addTestResult(List<Double> trueTimes,
			List<ResponseTimeRange> responseTimePredictions) {
		
		DescriptiveStatistics resultStats = new DescriptiveStatistics();
		
		for (int i = 0; i < trueTimes.size(); i++) {
			Double trueTime = trueTimes.get(i);
			ResponseTimeRange predictedRange = responseTimePredictions.get(i);
			Double prediction = getPredictionValue(predictedRange);
			if (prediction != null && trueTime != null && trueTime != 0 && trueTime != Double.POSITIVE_INFINITY
					&& prediction != Double.POSITIVE_INFINITY) {
				
				Double relError = Math.abs(prediction - trueTime)/trueTime;
				resultStats.addValue(relError);
			}
		}
		
		stats.addValue(resultStats.getMean());
	}

}
