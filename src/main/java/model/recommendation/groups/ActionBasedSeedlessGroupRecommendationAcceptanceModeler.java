package model.recommendation.groups;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import metrics.MetricResult;
import metrics.groups.actionbased.ActionBasedGroupMetric;
import metrics.groups.distance.GroupDistanceMetric;
import recommendation.general.actionbased.CollaborativeAction;
import recommendation.general.actionbased.CollaborativeActionThread;

public class ActionBasedSeedlessGroupRecommendationAcceptanceModeler<IdType, CollaboratorType, ActionType extends CollaborativeAction<CollaboratorType>, ThreadType extends CollaborativeActionThread<CollaboratorType, ActionType>>
		implements GroupRecommendationAcceptanceModeler {


	GroupDistanceMetric<CollaboratorType> distanceMetric;
	Collection<Set<CollaboratorType>> recommendations;
	Collection<Set<CollaboratorType>> idealGroups;
	Collection<ActionType> testActions;
	Collection<ActionBasedGroupMetric<CollaboratorType,ActionType>> metrics;
	
	public ActionBasedSeedlessGroupRecommendationAcceptanceModeler(
			GroupDistanceMetric<CollaboratorType> distanceMetric,
			Collection<Set<CollaboratorType>> recommendations,
			Collection<Set<CollaboratorType>> idealGroups,
			Collection<ActionType> testActions,
			Collection<ActionBasedGroupMetric<CollaboratorType,ActionType>> metrics) {

		this.distanceMetric = distanceMetric;
		this.recommendations = recommendations;
		this.idealGroups = recommendations;
		this.testActions = testActions;
		this.metrics = metrics;
	}

	@Override
	public Collection<MetricResult> modelRecommendationAcceptance() {
		
		Map<Set<CollaboratorType>, Set<CollaboratorType>> recommendationsToIdeals = new HashMap<>();
		Map<ActionType, Set<CollaboratorType>> testActionsToRecommendations = new HashMap<>();
		Collection<Set<CollaboratorType>> unusedIdeals = new HashSet<>(idealGroups);
		Collection<Set<CollaboratorType>> unusedRecommendations = new HashSet<Set<CollaboratorType>>(recommendations);

		for (Set<CollaboratorType> recommendation : recommendations) {

			double minDistanceToIdeal = Double.MAX_VALUE;
			Set<CollaboratorType> bestIdeal = null;

			for (Set<CollaboratorType> ideal : idealGroups) {
				if (unusedIdeals.contains(ideal)) {
					Double distance = distanceMetric.distance(recommendation, ideal);
					if (distance != null && distance < minDistanceToIdeal) {
						minDistanceToIdeal = distance;
						bestIdeal = ideal;
					}
				}
			}

			if (bestIdeal != null) {
				recommendationsToIdeals.put(recommendation, bestIdeal);
				unusedRecommendations.remove(recommendation);
				unusedIdeals.remove(bestIdeal);
			}
		}
		
		
		for (ActionType testAction : testActions) {

			double minDistanceToRecommendation = Double.MAX_VALUE;
			Set<CollaboratorType> bestRecommendation = null;

			for (Set<CollaboratorType> recommendation : recommendations) {
				Double distance = distanceMetric.distance(
						new HashSet<>(testAction.getCollaborators()), recommendation);
				if (distance != null && distance < minDistanceToRecommendation) {
					minDistanceToRecommendation = distance;
					bestRecommendation = recommendation;
				}
			}

			if (bestRecommendation != null) {
				testActionsToRecommendations.put(testAction, bestRecommendation);
			}
		}
		
		Collection<MetricResult> results = new ArrayList<MetricResult>();
		for (ActionBasedGroupMetric<CollaboratorType, ActionType> metric : metrics) {
			results.add(metric.evaluate(recommendationsToIdeals, unusedRecommendations,
					unusedIdeals, testActions, testActionsToRecommendations));
		}

		return results;
		
	}

}
