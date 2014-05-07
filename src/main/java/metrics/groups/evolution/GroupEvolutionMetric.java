package metrics.groups.evolution;

import groups.evolution.recommendations.RecommendedEvolution;
import groups.evolution.recommendations.RecommendedGroupChangeEvolution;
import groups.evolution.recommendations.RecommendedGroupCreationEvolution;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import metrics.Metric;
import metrics.MetricResult;

public abstract class GroupEvolutionMetric<V> implements Metric {
	
	protected double requiredRelativeAdditions(Set<V> recommendation, Set<V> ideal) {
		Set<V> additions = new HashSet<>(ideal);
		additions.removeAll(recommendation);
		
		return ((double) additions.size()) / ideal.size();
	}
	
	protected double requiredRelativeDeletions(Set<V> recommendation, Set<V> ideal) {
		Set<V> deletions = new HashSet<>(recommendation);
		deletions.removeAll(ideal);
		
		return ((double) deletions.size()) / recommendation.size();
	}

	public abstract MetricResult evaluate(Map<Set<V>, Collection<Set<V>>> oldToNewIdealGroups,
			Collection<Set<V>> newlyCreatedIdealGroups,
			Map<RecommendedGroupChangeEvolution<V>, Set<V>> groupChangeToIdeal,
			Map<RecommendedGroupCreationEvolution<V>, Set<V>> groupCreationToIdeal,
			Collection<RecommendedEvolution<V>> unusedRecommendations, Collection<Set<V>> unusedIdeals);
}
