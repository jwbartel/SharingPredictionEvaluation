package metrics.response.time;

import java.util.ArrayList;
import java.util.List;

import prediction.response.time.ResponseTimeRange;
import prediction.response.time.ScoringMethods;

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
		
		List<Double> predictions = new ArrayList<Double>(trueTimes.size());
		
		for (int i = 0; i < responseTimePredictions.size(); i++) {
			ResponseTimeRange predictedRange = responseTimePredictions.get(i);
			Double prediction = getPredictionValue(predictedRange);
			predictions.add(prediction);
		}
		
		double rmse = ScoringMethods.rootMeanSquareError(trueTimes, predictions);
		stats.addValue(rmse);
	}

}
