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

public class NumIdeals<V> extends GroupEvolutionMetric<V> {

	@Override
	public String getHeader() {
		return "number of ideals";
	}

	@Override
	public MetricResult evaluate(Set<Integer> newMembers, Map<Set<V>, Collection<Set<V>>> oldToNewIdealGroups,
			Collection<Set<V>> newlyCreatedIdealGroups,
			Map<RecommendedGroupChangeEvolution<V>, Set<V>> groupChangeToIdeal,
			Map<RecommendedGroupCreationEvolution<V>, Set<V>> groupCreationToIdeal,
			Collection<RecommendedEvolution<V>> unusedRecommendations,
			Collection<Set<V>> unusedIdeals) {

		Collection<Set<V>> ideals = new HashSet<>(newlyCreatedIdealGroups);
		for (Collection<Set<V>> mappedNewIdeals : oldToNewIdealGroups.values()) {
			ideals.addAll(mappedNewIdeals);
		}
		ideals.addAll(newlyCreatedIdealGroups);

		return new DoubleResult(ideals.size());
	}

}
