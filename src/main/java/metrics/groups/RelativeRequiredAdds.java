package metrics.groups;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import metrics.MetricResult;
import metrics.StatisticsResult;

public class RelativeRequiredAdds<V> implements GroupMetric<V> {
	
	@Override
	public String getHeader() {
		return "avg-reqired adds,stdev-required adds";
	}
	
	@Override
	public MetricResult evaluate(Map<Set<V>, Set<V>> recommendationsToIdeals,
			Collection<Set<V>> unusedRecommendations,
			Collection<Set<V>> unusedIdeals) {
		
		DescriptiveStatistics stats = new DescriptiveStatistics();
		
		for(Entry<Set<V>, Set<V>> entry : recommendationsToIdeals.entrySet()) {
			Set<V> recommendation = entry.getKey();
			Set<V> ideal = entry.getValue();
			
			Set<V> adds = new HashSet<V>(ideal);
			adds.removeAll(recommendation);
			
			stats.addValue(((double) adds.size())/ideal.size());
		}
		
		return new StatisticsResult(stats);
	}

}
