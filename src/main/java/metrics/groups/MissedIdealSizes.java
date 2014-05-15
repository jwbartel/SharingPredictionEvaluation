package metrics.groups;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import metrics.MetricResult;
import metrics.StatisticsResult;

public class MissedIdealSizes<V> extends GroupMetric<V> {
	
	@Override
	public String getHeader() {
		return "avg-missed ideal size,stdev-missed ideal size";
	}

	@Override
	public MetricResult evaluate(Map<Set<V>, Set<V>> recommendationsToIdeals,
			Collection<Set<V>> unusedRecommendations,
			Collection<Set<V>> unusedIdeals) {
		
		DescriptiveStatistics stats = new DescriptiveStatistics();
		for (Set<V> unusedIdeal : unusedIdeals) {
			stats.addValue(unusedIdeal.size());
		}
		return new StatisticsResult(stats);
	}

}
