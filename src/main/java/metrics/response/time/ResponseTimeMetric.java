package metrics.response.time;

import java.util.List;

import metrics.Metric;
import metrics.MetricResult;
import metrics.StatisticsResult;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import prediction.response.time.ResponseTimeRange;

public abstract class ResponseTimeMetric implements Metric {

	protected DescriptiveStatistics stats = new DescriptiveStatistics();


	public abstract String getTitle();
	
	@Override
	public String getHeader() {
		return "avg-"+getTitle()+",stdev-"+getTitle();
	}
	
	
	public abstract void addTestResult(List<Double> trueTimes, List<ResponseTimeRange> responseTimePredictions);

	public MetricResult evaluate() {
		return new StatisticsResult(stats);
	}
}
