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

public class PercentMissedEvolvedIdeals<V> extends GroupEvolutionMetric<V> {

	@Override
	public String getHeader() {
		return "percent missed evolutions";
	}

	@Override
	public MetricResult evaluate(Set<V> newMembers, Map<Set<V>, Collection<Set<V>>> oldToNewIdealGroups,
			Collection<Set<V>> newlyCreatedIdealGroups,
			Map<RecommendedGroupChangeEvolution<V>, Set<V>> groupChangeToIdeal,
			Map<RecommendedGroupCreationEvolution<V>, Set<V>> groupCreationToIdeal,
			Collection<RecommendedEvolution<V>> unusedRecommendations,
			Collection<Set<V>> unusedIdeals) {

		Collection<Set<V>> intendedEvolvedIdeals = new HashSet<>();
		for (Set<V> oldIdeal : oldToNewIdealGroups.keySet()) {
			Collection<Set<V>> mappedNewIdeals = oldToNewIdealGroups.get(oldIdeal);
			for (Set<V> newIdeal : mappedNewIdeals) {
				if (!oldIdeal.equals(newIdeal)) {
					intendedEvolvedIdeals.add(newIdeal);
				}
			}
		}
		
		if (intendedEvolvedIdeals.size() == 0) {
			return new DoubleResult(0.0);
		}

		Collection<Set<V>> unevolvedIdeals = new HashSet<>(intendedEvolvedIdeals);
		unevolvedIdeals.retainAll(unusedIdeals);

		return new DoubleResult(((double) unevolvedIdeals.size()) / intendedEvolvedIdeals.size());
	}

}
