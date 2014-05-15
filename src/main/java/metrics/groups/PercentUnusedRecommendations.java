package metrics.groups;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import metrics.DoubleResult;
import metrics.MetricResult;

public class PercentUnusedRecommendations<V> extends GroupMetric<V> {

	@Override
	public String getHeader() {
		return "percent unused recommendations";
	}
	
	@Override
	public MetricResult evaluate(Map<Set<V>, Set<V>> recommendationsToIdeals,
			Collection<Set<V>> unusedRecommendations,
			Collection<Set<V>> unusedIdeals) {
		
		Collection<Set<V>> allRecommendations = new HashSet<Set<V>>(unusedRecommendations);
		allRecommendations.addAll(recommendationsToIdeals.keySet());
		
		return new DoubleResult(((double) unusedRecommendations.size())/allRecommendations.size());
	}

}
