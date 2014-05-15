package metrics.groups;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import metrics.MetricResult;
import metrics.StatisticsResult;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class RelativeRequiredDeletes<V> extends GroupMetric<V> {

	@Override
	public String getHeader() {
		return "avg-required deletes,stdev-required deletes";
	}
	
	@Override
	public MetricResult evaluate(Map<Set<V>, Set<V>> recommendationsToIdeals,
			Collection<Set<V>> unusedRecommendations,
			Collection<Set<V>> unusedIdeals) {
		
		DescriptiveStatistics stats = new DescriptiveStatistics();
		
		for(Entry<Set<V>, Set<V>> entry : recommendationsToIdeals.entrySet()) {
			Set<V> recommendation = entry.getKey();
			Set<V> ideal = entry.getValue();
			
			stats.addValue(relativeRequiredDeletions(ideal, recommendation));
		}
		
		return new StatisticsResult(stats);
	}

}
