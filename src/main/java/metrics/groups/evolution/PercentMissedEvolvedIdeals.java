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

public class PercentMissedEvolvedIdeals<V> extends GroupEvolutionMetric<V> {

	@Override
	public String getHeader() {
		return "percent missed evolutions";
	}

	@Override
	public MetricResult evaluate(Map<Set<V>, Collection<Set<V>>> oldToNewIdealGroups,
			Collection<Set<V>> newlyCreatedIdealGroups,
			Map<RecommendedGroupChangeEvolution<V>, Set<V>> groupChangeToIdeal,
			Map<RecommendedGroupCreationEvolution<V>, Set<V>> groupCreationToIdeal,
			Collection<RecommendedEvolution<V>> unusedRecommendations,
			Collection<Set<V>> unusedIdeals) {

		Collection<Set<V>> intendedEvolvedIdeals = new HashSet<>();
		for (Collection<Set<V>> mappedNewIdeals : oldToNewIdealGroups.values()) {
			intendedEvolvedIdeals.addAll(mappedNewIdeals);
		}

		Collection<Set<V>> unevolvedIdeals = new HashSet<>(intendedEvolvedIdeals);
		unevolvedIdeals.removeAll(unusedIdeals);

		return new DoubleResult(((double) unevolvedIdeals.size()) / intendedEvolvedIdeals.size());
	}

}
