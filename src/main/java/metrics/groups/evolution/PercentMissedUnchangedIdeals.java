package metrics.groups.evolution;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import recommendation.groups.evolution.recommendations.RecommendedEvolution;
import recommendation.groups.evolution.recommendations.RecommendedGroupChangeEvolution;
import recommendation.groups.evolution.recommendations.RecommendedGroupCreationEvolution;
import metrics.DoubleResult;
import metrics.MetricResult;

public class PercentMissedUnchangedIdeals<V> extends GroupEvolutionMetric<V> {

	@Override
	public String getHeader() {
		return "percent missed unchanged evolutions";
	}

	@Override
	public MetricResult evaluate(Set<Integer> newMembers, Map<Set<V>, Collection<Set<V>>> oldToNewIdealGroups,
			Collection<Set<V>> newlyCreatedIdealGroups,
			Map<RecommendedGroupChangeEvolution<V>, Set<V>> groupChangeToIdeal,
			Map<RecommendedGroupCreationEvolution<V>, Set<V>> groupCreationToIdeal,
			Collection<RecommendedEvolution<V>> unusedRecommendations,
			Collection<Set<V>> unusedIdeals) {

		Collection<Set<V>> intendedUnchangedIdeals = new HashSet<>();
		for (Set<V> oldIdeal : oldToNewIdealGroups.keySet()) {
			Collection<Set<V>> mappedNewIdeals = oldToNewIdealGroups.get(oldIdeal);
			for (Set<V> newIdeal : mappedNewIdeals) {
				if (oldIdeal.equals(newIdeal)) {
					intendedUnchangedIdeals.add(newIdeal);
				}
			}
		}

		Collection<Set<V>> missedUnchangedIdeals = new HashSet<>(intendedUnchangedIdeals);
		missedUnchangedIdeals.retainAll(unusedIdeals);

		return new DoubleResult(((double) missedUnchangedIdeals.size()) / intendedUnchangedIdeals.size());
	}

}
