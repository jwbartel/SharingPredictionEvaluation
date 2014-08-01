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

public class PercentUnchangedIdeals<V> extends GroupEvolutionMetric<V> {

	@Override
	public String getHeader() {
		return "percent unchanged ideals";
	}

	@Override
	public MetricResult evaluate(Set<V> newMembers, Map<Set<V>, Collection<Set<V>>> oldToNewIdealGroups,
			Collection<Set<V>> newlyCreatedIdealGroups,
			Map<RecommendedGroupChangeEvolution<V>, Set<V>> groupChangeToIdeal,
			Map<RecommendedGroupCreationEvolution<V>, Set<V>> groupCreationToIdeal,
			Collection<RecommendedEvolution<V>> unusedRecommendations,
			Collection<Set<V>> unusedIdeals) {

		Collection<Set<V>> ideals = new HashSet<>(newlyCreatedIdealGroups);
		Collection<Set<V>> unchangedIdeals = new HashSet<>();
		for (Set<V> oldIdeal : oldToNewIdealGroups.keySet()) {
			Collection<Set<V>> mappedNewIdeals = oldToNewIdealGroups.get(oldIdeal);
			ideals.addAll(mappedNewIdeals);
			for (Set<V> newIdeal : mappedNewIdeals) {
				if (oldIdeal.equals(newIdeal)) {
					unchangedIdeals.add(newIdeal);
				}
			}
		}
		ideals.addAll(newlyCreatedIdealGroups);

		return new DoubleResult(((double) unchangedIdeals.size()) / ideals.size());
	}

}
