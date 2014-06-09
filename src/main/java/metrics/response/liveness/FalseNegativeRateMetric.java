package metrics.response.liveness;

import java.util.List;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import metrics.MetricResult;
import metrics.StatisticsResult;

public class FalseNegativeRateMetric implements ResponseLivenessMetric {

	DescriptiveStatistics stats = new DescriptiveStatistics();
	
	@Override
	public String getHeader() {
		return "mean-accuracy,stdev-accuracy";
	}

	@Override
	public void addTestResult(List<Double> trueTimes, List<Double> livenessPredictions) {
		int falsePositives = 0;
		int trueNegatives = 0;
		for (int i=0; i<trueTimes.size(); i++) {
			Double trueTime = trueTimes.get(i);
			Double livenessPrediction = livenessPredictions.get(i);
			
			if (livenessPrediction >= 0.5 ) {
				if (trueTime == Double.POSITIVE_INFINITY) {
					falsePositives++;
				}
			} else {
				if (trueTime == Double.POSITIVE_INFINITY) {
					trueNegatives++;
				}
			}
		}
		stats.addValue(((double) falsePositives)/(falsePositives + trueNegatives));

	}

	@Override
	public MetricResult evaluate() {
		return new StatisticsResult(stats);
	}	

}
