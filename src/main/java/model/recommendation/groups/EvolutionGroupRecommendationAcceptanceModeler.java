package model.recommendation.groups;

import groups.evolution.RecommendedEvolution;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import metrics.MetricResult;
import metrics.groups.GroupMetric;
import metrics.groups.distance.GroupDistanceMetric;

public class EvolutionGroupRecommendationAcceptanceModeler<V> implements
		GroupRecommendationAcceptanceModeler {

	GroupDistanceMetric<V> distanceMetric;
	Collection<RecommendedEvolution<V>> recommendations;
	Map<Set<V>,Set<V>> oldToNewIdealGroups;
	Collection<Set<V>> newlyCreatedIdealGroups;
	Collection<GroupMetric<V>> metrics;

	public EvolutionGroupRecommendationAcceptanceModeler(GroupDistanceMetric<V> distanceMetric,
			Collection<RecommendedEvolution<V>> recommendations, Map<Set<V>,Set<V>> oldToNewIdealGroups,
			Collection<Set<V>> newlyCreatedIdealGroups,	Collection<GroupMetric<V>> metrics) {
		this.distanceMetric = distanceMetric;
		this.recommendations = recommendations;
		this.oldToNewIdealGroups = oldToNewIdealGroups;
		this.newlyCreatedIdealGroups = newlyCreatedIdealGroups;
		this.metrics = metrics;
	}

	@Override
	public Collection<MetricResult> modelRecommendationAcceptance() {

		//TODO : evaluate costs of changing existing groups
		//TODO : evaluate costs of creating new groups
		
		return null;

	}

}
