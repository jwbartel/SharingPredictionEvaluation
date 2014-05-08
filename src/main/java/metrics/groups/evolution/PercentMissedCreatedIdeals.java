package metrics.groups.evolution;

import groups.evolution.recommendations.RecommendedEvolution;
import groups.evolution.recommendations.RecommendedGroupChangeEvolution;
import groups.evolution.recommendations.RecommendedGroupCreationEvolution;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import metrics.DoubleResult;
import metrics.MetricResult;

public class PercentMissedCreatedIdeals<V> extends GroupEvolutionMetric<V> {

	@Override
	public String getHeader() {
		return "percent missed creations";
	}

	@Override
	public MetricResult evaluate(Map<Set<V>, Collection<Set<V>>> oldToNewIdealGroups,
			Collection<Set<V>> newlyCreatedIdealGroups,
			Map<RecommendedGroupChangeEvolution<V>, Set<V>> groupChangeToIdeal,
			Map<RecommendedGroupCreationEvolution<V>, Set<V>> groupCreationToIdeal,
			Collection<RecommendedEvolution<V>> unusedRecommendations,
			Collection<Set<V>> unusedIdeals) {

		Collection<Set<V>> uncreatedIdeals = new HashSet<>(newlyCreatedIdealGroups);
		uncreatedIdeals.removeAll(groupCreationToIdeal.values());

		return new DoubleResult(((double) uncreatedIdeals.size()) / newlyCreatedIdealGroups.size());
	}

}
