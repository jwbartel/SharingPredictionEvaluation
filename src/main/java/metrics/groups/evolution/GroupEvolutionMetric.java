package metrics.groups.evolution;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import recommendation.groups.evolution.recommendations.RecommendedEvolution;
import recommendation.groups.evolution.recommendations.RecommendedGroupChangeEvolution;
import recommendation.groups.evolution.recommendations.RecommendedGroupCreationEvolution;
import metrics.Metric;
import metrics.MetricResult;

public abstract class GroupEvolutionMetric<V> implements Metric {
	
	protected double requiredAdditions(Set<V> recommendation, Set<V> ideal) {
		Set<V> additions = new HashSet<>(ideal);
		additions.removeAll(recommendation);
		return additions.size();
	}
	
	protected double requiredRelativeAdditions(Set<V> recommendation, Set<V> ideal) {
		
		return ((double) requiredAdditions(recommendation, ideal)) / ideal.size();
	}
	
	protected double requiredDeletions(Set<V> recommendation, Set<V> ideal) {
		Set<V> deletions = new HashSet<>(recommendation);
		deletions.removeAll(ideal);
		return deletions.size();
	}
	
	protected double requiredRelativeDeletions(Set<V> recommendation, Set<V> ideal) {
		
		return ((double) requiredDeletions(recommendation, ideal)) / recommendation.size();
	}

	public abstract MetricResult evaluate(
			Set<V> newMembers,
			Map<Set<V>, Collection<Set<V>>> oldToNewIdealGroups,
			Collection<Set<V>> newlyCreatedIdealGroups,
			Map<RecommendedGroupChangeEvolution<V>, Set<V>> groupChangeToIdeal,
			Map<RecommendedGroupCreationEvolution<V>, Set<V>> groupCreationToIdeal,
			Collection<RecommendedEvolution<V>> unusedRecommendations, Collection<Set<V>> unusedIdeals);
}
