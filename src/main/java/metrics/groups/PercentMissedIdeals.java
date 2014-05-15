package metrics.groups;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import metrics.DoubleResult;
import metrics.MetricResult;

public class PercentMissedIdeals<V> extends GroupMetric<V> {

	@Override
	public String getHeader() {
		return "percent missed ideals";
	}
	
	@Override
	public MetricResult evaluate(Map<Set<V>, Set<V>> recommendationsToIdeals,
			Collection<Set<V>> unusedRecommendations,
			Collection<Set<V>> unusedIdeals) {
		
		Collection<Set<V>> allIdeals = new HashSet<Set<V>>(unusedIdeals);
		allIdeals.addAll(recommendationsToIdeals.values());
		
		return new DoubleResult(((double) unusedIdeals.size())/allIdeals.size());
	}

}
