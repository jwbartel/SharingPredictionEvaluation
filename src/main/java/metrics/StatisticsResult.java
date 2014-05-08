package metrics;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class StatisticsResult implements MetricResult {

	private final DescriptiveStatistics statistics;

	public StatisticsResult(DescriptiveStatistics stats) {
		this.statistics = stats;
	}

	public DescriptiveStatistics getStatistics() {
		return statistics;
	}

	public String toString() {
		double mean = (Double.isNaN(statistics.getMean())) ? 0 : statistics
				.getMean();
		double stdev = (Double.isNaN(statistics.getStandardDeviation())) ? 0
				: statistics.getStandardDeviation();

		return "" + mean + "," + stdev;
	}
}
