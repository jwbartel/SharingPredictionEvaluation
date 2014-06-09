package metrics.response.liveness;

import java.util.List;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import metrics.MetricResult;
import metrics.StatisticsResult;

public class AccuracyMetric implements ResponseLivenessMetric {

	DescriptiveStatistics stats = new DescriptiveStatistics();
	
	@Override
	public String getHeader() {
		return "mean-accuracy,stdev-accuracy";
	}

	@Override
	public void addTestResult(List<Double> trueTimes, List<Double> livenessPredictions) {
		int totalNum = trueTimes.size();
		int numCorrect = 0;
		for (int i=0; i<totalNum; i++) {
			Double trueTime = trueTimes.get(i);
			Double livenessPrediction = livenessPredictions.get(i);
			
			if (livenessPrediction >= 0.5 ) {
				if (trueTime != Double.POSITIVE_INFINITY) {
					numCorrect++;
				}
			} else {
				if (trueTime == Double.POSITIVE_INFINITY) {
					numCorrect++;
				}
			}
		}
		stats.addValue(((double) numCorrect)/totalNum);

	}

	@Override
	public MetricResult evaluate() {
		return new StatisticsResult(stats);
	}

}
