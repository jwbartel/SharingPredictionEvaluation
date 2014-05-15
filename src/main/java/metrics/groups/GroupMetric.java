package metrics.groups;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import metrics.Metric;
import metrics.MetricResult;

public abstract class GroupMetric<V> implements Metric {

	public static <V> double relativeRequiredDeletions(Set<V> actualGroup,
			Set<V> recommendation) {

		Set<V> deletes = new HashSet<V>(recommendation);
		deletes.removeAll(actualGroup);

		return ((double) deletes.size()) / recommendation.size();
	}

	public static <V> double relativeRequiredAdditions(Set<V> actualGroup,
			Set<V> recommendation) {
		Set<V> adds = new HashSet<V>(actualGroup);
		adds.removeAll(recommendation);

		return ((double) adds.size()) / actualGroup.size();
	}

	public abstract MetricResult evaluate(Map<Set<V>, Set<V>> recommendationsToIdeals,
			Collection<Set<V>> unusedRecommendations, Collection<Set<V>> unusedIdeals);
}
