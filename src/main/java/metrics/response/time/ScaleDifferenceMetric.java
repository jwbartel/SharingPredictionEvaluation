package metrics.response.time;

import java.util.List;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import testbed.dataset.actions.messages.stackoverflow.evaluation.ResponseTimeEvaluator.ResponseTimeRange;

public class ScaleDifferenceMetric extends MinOrMaxResponseTimeMetric {

	public static ResponseTimeMetricFactory factory(final MinOrMaxType type) {
		return new ResponseTimeMetricFactory() {
			
			@Override
			public ResponseTimeMetric create() {
				return new ScaleDifferenceMetric(type);
			}
		};
	}
	
	public ScaleDifferenceMetric(MinOrMaxType type) {
		super(type);
	}

	@Override
	public String getTitle() {
		return "Scale Difference "+type.toString();
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
				
				Double minVal = Math.min(prediction, trueTime);
				Double maxVal = Math.max(prediction, trueTime);
				
				if (minVal != 0) {
					Double scaledError = maxVal/minVal;
					resultStats.addValue(scaledError);
				}
			}
		}
		
		stats.addValue(resultStats.getMean());
	}

}
