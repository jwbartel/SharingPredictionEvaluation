package model.recommendation.groups;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import metrics.MetricResult;
import metrics.groups.actionbased.ActionBasedGroupMetric;
import data.representation.actionbased.CollaborativeAction;
import data.representation.actionbased.CollaborativeActionThread;

public class ActionBasedSeedlessGroupRecommendationAcceptanceModeler<CollaboratorType, ActionType extends CollaborativeAction<CollaboratorType>>
		implements GroupRecommendationAcceptanceModeler {

	Collection<Set<CollaboratorType>> recommendations;
	Collection<Set<CollaboratorType>> idealGroups;
	Collection<ActionType> testActions;
	Collection<ActionBasedGroupMetric<CollaboratorType, ActionType>> metrics;

	public ActionBasedSeedlessGroupRecommendationAcceptanceModeler(
			Collection<Set<CollaboratorType>> recommendations,
			Collection<Set<CollaboratorType>> idealGroups,
			Collection<ActionType> testActions,
			Collection<ActionBasedGroupMetric<CollaboratorType, ActionType>> metrics) {

		this.recommendations = recommendations;
		this.idealGroups = recommendations;
		this.testActions = testActions;
		this.metrics = metrics;
	}

	protected Set<CollaboratorType> matchRecommendationToIdeal(
			Set<CollaboratorType> recommendation,
			Collection<Set<CollaboratorType>> unusedIdeals) {

		// Find best ideal to match with recommendation
		double minDistanceToIdeal = Double.MAX_VALUE;
		Set<CollaboratorType> bestIdeal = null;

		for (Set<CollaboratorType> ideal : idealGroups) {
			if (unusedIdeals.contains(ideal)) {
				Double distance = distance(recommendation, ideal);
				if (distance != null && distance < minDistanceToIdeal) {
					minDistanceToIdeal = distance;
					bestIdeal = ideal;
				}
			}
		}
		return bestIdeal;
	}
	
	protected ActionType matchRecommendationToAction(
			Set<CollaboratorType> recommendation,
			Collection<ActionType> futureActions) {

		// Find best action to match with recommendation
		double minDistanceToAction = Double.MAX_VALUE;
		ActionType bestAction = null;

		for (ActionType testAction : futureActions) {
			Double distance = distance(recommendation,
					new HashSet<>(testAction.getCollaborators()));
			if (distance != null && distance < minDistanceToAction) {
				minDistanceToAction = distance;
				bestAction = testAction;
			}
		}
		return bestAction;
	}
	
	protected Set<CollaboratorType> matchActionToRecommendation(
			ActionType action,
			Set<CollaboratorType> bestRecommendation,
			Set<CollaboratorType> recommendation) {
		
		double minDistanceToRecommendation = Double.MAX_VALUE;
		if (bestRecommendation != null) {
			minDistanceToRecommendation = distance(bestRecommendation, new HashSet<>(
				action.getCollaborators()));
		}
		Double distance = distance(recommendation, new HashSet<>(
				action.getCollaborators()));
		if (distance != null && distance < minDistanceToRecommendation) {
			minDistanceToRecommendation = distance;
			bestRecommendation = recommendation;
		}
		return bestRecommendation;
	}

	@Override
	public Collection<MetricResult> modelRecommendationAcceptance() {

		Map<Set<CollaboratorType>, Set<CollaboratorType>> recommendationsToIdeals = new HashMap<>();
		Map<ActionType, Set<CollaboratorType>> testActionsToRecommendations = new HashMap<>();
		Map<Set<CollaboratorType>, ActionType> recommendationsToTestAction = new HashMap<>();
		Collection<Set<CollaboratorType>> unusedIdeals = new HashSet<>(
				idealGroups);

		for (Set<CollaboratorType> recommendation : recommendations) {

			// Find best ideal to match with recommendation
			Set<CollaboratorType> bestIdeal = matchRecommendationToIdeal(
					recommendation, unusedIdeals);
			if (bestIdeal != null) {
				recommendationsToIdeals.put(recommendation, bestIdeal);
				unusedIdeals.remove(bestIdeal);
			}

			// Find best action to match with recommendation
			ActionType bestAction = matchRecommendationToAction(recommendation, testActions);
			if (bestAction != null) {
				recommendationsToTestAction.put(recommendation, bestAction);
			}
		}

		for (ActionType testAction : testActions) {

			Set<CollaboratorType> bestRecommendation = null;

			for (Set<CollaboratorType> recommendation : recommendations) {
				bestRecommendation = matchActionToRecommendation(testAction,
						bestRecommendation, recommendation);
			}

			if (bestRecommendation != null) {
				testActionsToRecommendations
						.put(testAction, bestRecommendation);
			}
		}

		Collection<MetricResult> results = new ArrayList<MetricResult>();
		for (ActionBasedGroupMetric<CollaboratorType, ActionType> metric : metrics) {
			results.add(metric.evaluate(recommendations, idealGroups,
					testActions, recommendationsToIdeals,
					recommendationsToTestAction, testActionsToRecommendations));
		}

		return results;

	}

	public Double distance(Set<CollaboratorType> recommendation,
			Set<CollaboratorType> intendedGroup) {
		Set<CollaboratorType> adds = new HashSet<CollaboratorType>(
				intendedGroup);
		adds.removeAll(recommendation);
		if (adds.size() == intendedGroup.size()) {
			return null;
		}

		Set<CollaboratorType> deletes = new HashSet<CollaboratorType>(
				recommendation);
		deletes.removeAll(intendedGroup);
		if (deletes.size() == recommendation.size()) {
			return null;
		}

		double distance = ((double) adds.size() + deletes.size());
		if (distance >= intendedGroup.size()) {
			return null;
		}
		return distance;
	}

}
