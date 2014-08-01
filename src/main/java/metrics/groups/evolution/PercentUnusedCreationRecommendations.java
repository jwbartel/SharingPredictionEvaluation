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

public class PercentUnusedCreationRecommendations<V> extends GroupEvolutionMetric<V> {

	@Override
	public String getHeader() {
		return "percent unused creation recommendations";
	}

	@Override
	public MetricResult evaluate(Set<V> newMembers, Map<Set<V>, Collection<Set<V>>> oldToNewIdealGroups,
			Collection<Set<V>> newlyCreatedIdealGroups,
			Map<RecommendedGroupChangeEvolution<V>, Set<V>> groupChangeToIdeal,
			Map<RecommendedGroupCreationEvolution<V>, Set<V>> groupCreationToIdeal,
			Collection<RecommendedEvolution<V>> unusedRecommendations, Collection<Set<V>> unusedIdeals) {
		
		Collection<RecommendedGroupCreationEvolution<V>> unusedCreationRecommendations = new HashSet<>();
		for (RecommendedEvolution<V> recommendation : unusedRecommendations) {
			if (recommendation instanceof RecommendedGroupCreationEvolution) {
				unusedCreationRecommendations
						.add((RecommendedGroupCreationEvolution<V>) recommendation);
			}
		}
		
		
		return new DoubleResult(((double) unusedCreationRecommendations.size())
				/ (unusedCreationRecommendations.size() + groupCreationToIdeal.keySet().size()));
	}

}
