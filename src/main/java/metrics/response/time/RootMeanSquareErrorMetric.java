package metrics.response.time;

import java.util.List;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import prediction.response.time.ResponseTimeRange;

public class RootMeanSquareErrorMetric extends MinOrMaxResponseTimeMetric {

	public static ResponseTimeMetricFactory factory(final MinOrMaxType type) {
		return new ResponseTimeMetricFactory() {
			
			@Override
			public ResponseTimeMetric create() {
				return new RootMeanSquareErrorMetric(type);
			}
		};
	}
	
	public RootMeanSquareErrorMetric(MinOrMaxType type) {
		super(type);
	}

	@Override
	public String getTitle() {
		return "Root Mean Square Error of "+type.toString();
	}

	@Override
	public void addTestResult(List<Double> trueTimes,
			List<ResponseTimeRange> responseTimePredictions) {
		
		int totalPredictions = 0;
		double meanSquareError = 0;
		DescriptiveStatistics resultStats = new DescriptiveStatistics();
		
		for (int i = 0; i < trueTimes.size(); i++) {
			Double trueTime = trueTimes.get(i);
			ResponseTimeRange predictedRange = responseTimePredictions.get(i);
			Double prediction = getPredictionValue(predictedRange);
			if (prediction != null && trueTime != null && trueTime != 0 && trueTime != Double.POSITIVE_INFINITY
					&& prediction != Double.POSITIVE_INFINITY) {
				
				totalPredictions++;
				meanSquareError += Math.pow(prediction-trueTime, 2.0);
			}
		}
		meanSquareError = meanSquareError/((double) totalPredictions);
		
		stats.addValue(Math.sqrt(meanSquareError));
	}

}
