package model.recommendation.groups;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import recommendation.groups.evolution.recommendations.RecommendedEvolution;
import recommendation.groups.evolution.recommendations.RecommendedGroupChangeEvolution;
import recommendation.groups.evolution.recommendations.RecommendedGroupCreationEvolution;
import metrics.MetricResult;
import metrics.groups.distance.GroupDistanceMetric;
import metrics.groups.evolution.GroupEvolutionMetric;

public class EvolutionGroupRecommendationAcceptanceModeler<V> implements
		GroupRecommendationAcceptanceModeler {

	GroupDistanceMetric<V> distanceMetric;
	Collection<RecommendedEvolution<V>> recommendations;
	Map<Set<V>,Collection<Set<V>>> oldToNewIdealGroups;
	Collection<Set<V>> newlyCreatedIdealGroups;
	Collection<GroupEvolutionMetric<V>> metrics;

	public EvolutionGroupRecommendationAcceptanceModeler(GroupDistanceMetric<V> distanceMetric,
			Collection<RecommendedEvolution<V>> recommendations,
			Map<Set<V>, Collection<Set<V>>> oldToNewIdealGroups,
			Collection<Set<V>> newlyCreatedIdealGroups,	Collection<GroupEvolutionMetric<V>> metrics) {
		this.distanceMetric = distanceMetric;
		this.recommendations = recommendations;
		this.oldToNewIdealGroups = oldToNewIdealGroups;
		this.newlyCreatedIdealGroups = newlyCreatedIdealGroups;
		this.metrics = metrics;
	}
	
	private Set<V> findClosestIdeal(Collection<Set<V>> candidateIdeals, Set<V> recommendedGroup) {
		double minDistance = Double.MAX_VALUE;
		Set<V> bestIdeal = null;
		
		for (Set<V> ideal : candidateIdeals) {
			Double distance = distanceMetric.distance(recommendedGroup, ideal);
			if (distance != null && distance < minDistance) {
				minDistance = distance;
				bestIdeal = ideal;
			}
		}
		
		return bestIdeal;
	}

	@Override
	public Collection<MetricResult> modelRecommendationAcceptance() {

		Collection<Set<V>> unusedIdealGroups = new HashSet<>(newlyCreatedIdealGroups);
		for (Collection<Set<V>> evolvedIdeals : oldToNewIdealGroups.values()) {
			unusedIdealGroups.addAll(evolvedIdeals);
		}

		Collection<RecommendedEvolution<V>> unusedRecommendations = new HashSet<RecommendedEvolution<V>>(recommendations);
		Map<RecommendedGroupChangeEvolution<V>, Set<V>> groupChangeToIdeal = new HashMap<>();
		Map<RecommendedGroupCreationEvolution<V>, Set<V>> groupCreationToIdeal = new HashMap<>();
		
		for(RecommendedEvolution<V> recommendation : recommendations) {
			if (recommendation instanceof RecommendedGroupCreationEvolution) {
				// If we are recommending the creation of a new group
				
				Set<V> recommendedGroup = recommendation.getRecommenderEngineResult();
				Set<V> closestIdeal = findClosestIdeal(unusedIdealGroups, recommendedGroup);
				
				if (closestIdeal != null) {
					groupCreationToIdeal.put((RecommendedGroupCreationEvolution<V>) recommendation,
							closestIdeal);
					unusedRecommendations.remove(recommendation);
					unusedIdealGroups.remove(closestIdeal);
				}
				
			} else if (recommendation instanceof RecommendedGroupChangeEvolution) {
				// If we are recommending the evolution of an existing group
				
				Collection<Set<V>> candidateIdeals = oldToNewIdealGroups
						.get(((RecommendedGroupChangeEvolution<V>) recommendation).getOldGroup());
				Set<V> recommendedGroup = ((RecommendedGroupChangeEvolution<V>) recommendation)
						.getMerging();
				
				Set<V> closestIdeal = findClosestIdeal(candidateIdeals, recommendedGroup);

				if (closestIdeal != null) {
					groupChangeToIdeal.put((RecommendedGroupChangeEvolution<V>) recommendation,
							closestIdeal);
					unusedRecommendations.remove(recommendation);
					unusedIdealGroups.remove(closestIdeal);
				}
			}
		}

		Collection<MetricResult> results = new ArrayList<MetricResult>();
		for (GroupEvolutionMetric<V> metric : metrics) {
			results.add(metric.evaluate(oldToNewIdealGroups, unusedIdealGroups,
					groupChangeToIdeal, groupCreationToIdeal,
					unusedRecommendations, unusedIdealGroups));
		}

		return results;
	}

}
