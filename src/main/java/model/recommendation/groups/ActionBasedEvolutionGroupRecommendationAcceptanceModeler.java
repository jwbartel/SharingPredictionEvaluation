package model.recommendation.groups;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import metrics.MetricResult;
import metrics.groups.actionbased.evolution.ActionBasedGroupEvolutionMetric;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import recommendation.groups.evolution.recommendations.RecommendedEvolution;
import recommendation.groups.evolution.recommendations.RecommendedGroupChangeEvolution;
import recommendation.groups.evolution.recommendations.RecommendedGroupCreationEvolution;
import data.representation.actionbased.CollaborativeAction;

public class ActionBasedEvolutionGroupRecommendationAcceptanceModeler<Collaborator, Action extends CollaborativeAction<Collaborator>> implements
		GroupRecommendationAcceptanceModeler {

	UndirectedGraph<Collaborator, DefaultEdge> oldGraph;
	UndirectedGraph<Collaborator, DefaultEdge> newGraph;
	Collection<RecommendedEvolution<Collaborator>> recommendations;
	Collection<Action> testActions;
	Collection<ActionBasedGroupEvolutionMetric<Collaborator, Action>> metrics;

	public ActionBasedEvolutionGroupRecommendationAcceptanceModeler(
			UndirectedGraph<Collaborator, DefaultEdge> oldGraph,
			UndirectedGraph<Collaborator, DefaultEdge> newGraph,
			Collection<RecommendedEvolution<Collaborator>> recommendations,
			Collection<Action> testActions,
			Collection<ActionBasedGroupEvolutionMetric<Collaborator, Action>> metrics) {
		this.oldGraph = oldGraph;
		this.newGraph = newGraph;
		this.recommendations = recommendations;
		this.testActions = testActions;
		this.metrics = metrics;
	}

	public Double distance(Set<Collaborator> recommendation,
			Set<Collaborator> intendedGroup) {
		Set<Collaborator> adds = new HashSet<Collaborator>(
				intendedGroup);
		adds.removeAll(recommendation);
		if (adds.size() == intendedGroup.size()) {
			return null;
		}

		Set<Collaborator> deletes = new HashSet<Collaborator>(
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
	
	protected Action matchRecommendationToAction(
			Set<Collaborator> recommendation,
			Collection<Action> futureActions) {

		// Find best action to match with recommendation
		double minDistanceToAction = Double.MAX_VALUE;
		Action bestAction = null;

		for (Action testAction : futureActions) {
			Double distance = distance(recommendation,
					new HashSet<>(testAction.getCollaborators()));
			if (distance != null && distance < minDistanceToAction) {
				minDistanceToAction = distance;
				bestAction = testAction;
			}
		}
		return bestAction;
	}
	
	protected RecommendedEvolution<Collaborator> matchActionToRecommendation(
			Action action,
			RecommendedEvolution<Collaborator> bestRecommendation,
			RecommendedEvolution<Collaborator> recommendation) {
		
		double minDistanceToRecommendation = Double.MAX_VALUE;
		if (bestRecommendation != null) {
			minDistanceToRecommendation = distance(getRecommendedGroup(bestRecommendation), new HashSet<>(
				action.getCollaborators()));
		}
		Double distance = distance(getRecommendedGroup(recommendation), new HashSet<>(
				action.getCollaborators()));
		if (distance != null && distance < minDistanceToRecommendation) {
			minDistanceToRecommendation = distance;
			bestRecommendation = recommendation;
		}
		return bestRecommendation;
	}
	
	private Set<Collaborator> getRecommendedGroup(RecommendedEvolution<Collaborator> recommendation) {
		if (recommendation instanceof RecommendedGroupCreationEvolution) {
			return recommendation.getRecommenderEngineResult();
		} else if (recommendation instanceof RecommendedGroupChangeEvolution){
			return ((RecommendedGroupChangeEvolution<Collaborator>) recommendation).getMerging();
		}
		return null;
	}

	@Override
	public Collection<MetricResult> modelRecommendationAcceptance() {

		Collection<RecommendedEvolution<Collaborator>> unusedRecommendations = new HashSet<RecommendedEvolution<Collaborator>>(recommendations);
		Map<Action, RecommendedEvolution<Collaborator>> testActionsToRecommendations = new HashMap<>();
		Map<RecommendedEvolution<Collaborator>, Action> recommendationsToTestAction = new HashMap<>();
		
		
		for (RecommendedEvolution<Collaborator> recommendation : recommendations) {

			// Find best action to match with recommendation
			Action bestAction = matchRecommendationToAction(getRecommendedGroup(recommendation), testActions);
			if (bestAction != null) {
				recommendationsToTestAction.put(recommendation, bestAction);
			}
		}

		for (Action testAction : testActions) {

			RecommendedEvolution<Collaborator> bestRecommendation = null;

			for (RecommendedEvolution<Collaborator> recommendation : recommendations) {
				bestRecommendation = matchActionToRecommendation(testAction,
						bestRecommendation, recommendation);
			}

			if (bestRecommendation != null) {
				testActionsToRecommendations
						.put(testAction, bestRecommendation);
			}
		}

		Collection<MetricResult> results = new ArrayList<MetricResult>();
		for (ActionBasedGroupEvolutionMetric<Collaborator, Action> metric : metrics) {
			results.add(metric.evaluate(oldGraph, newGraph, unusedRecommendations, testActions, recommendationsToTestAction, testActionsToRecommendations));
		}

		return results;
	}

}
