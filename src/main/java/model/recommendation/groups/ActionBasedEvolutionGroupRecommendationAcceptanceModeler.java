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
	Collection<Set<Collaborator>> oldGroups;
	Collection<RecommendedEvolution<Collaborator>> recommendations;
	Collection<Action> testActions;
	Collection<ActionBasedGroupEvolutionMetric<Collaborator, Action>> metrics;

	public ActionBasedEvolutionGroupRecommendationAcceptanceModeler(
			UndirectedGraph<Collaborator, DefaultEdge> oldGraph,
			UndirectedGraph<Collaborator, DefaultEdge> newGraph,
			Collection<Set<Collaborator>> oldGroups,
			Collection<RecommendedEvolution<Collaborator>> recommendations,
			Collection<Action> testActions,
			Collection<ActionBasedGroupEvolutionMetric<Collaborator, Action>> metrics) {
		this.oldGraph = oldGraph;
		this.newGraph = newGraph;
		this.oldGroups = oldGroups;
		this.recommendations = recommendations;
		this.testActions = testActions;
		this.metrics = metrics;
	}
	
	public double relativeDeletions(Set<Collaborator> recommendation,
			Set<Collaborator> intendedGroup) {
		
		Set<Collaborator> deletes = new HashSet<Collaborator>(
				recommendation);
		deletes.removeAll(intendedGroup);
		
		return ((double) deletes.size())/recommendation.size();
	}
	
	public double relativeAdditions(Set<Collaborator> recommendation,
			Set<Collaborator> intendedGroup) {
		
		Set<Collaborator> adds = new HashSet<Collaborator>(
				intendedGroup);
		adds.removeAll(recommendation);
		
		return ((double) adds.size())/intendedGroup.size();
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
//		if (distance >= intendedGroup.size()) {
//			return null;
//		}
		return distance;
	}
	
	protected Action matchGroupToAction(
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
	
	protected Set<Collaborator> matchActionToGroup(
			Action action,
			Set<Collaborator> bestGroup,
			Set<Collaborator> group) {
		
		double minAdds = Double.MAX_VALUE;
		double minDeletes = Double.MAX_VALUE;
		if (bestGroup != null) {
			minAdds = relativeAdditions(bestGroup, new HashSet<>(
				action.getCollaborators()));
			minDeletes = relativeDeletions(bestGroup, new HashSet<>(
					action.getCollaborators()));
		}
		double adds = relativeAdditions(group, new HashSet<>(
				action.getCollaborators()));
		double deletes = relativeDeletions(group, new HashSet<>(
				action.getCollaborators()));
		if ((adds < 1.0 && deletes < 1.0) &&
				(adds < minAdds) || (adds == minAdds && deletes < minDeletes)) {
			bestGroup = group;
		}
		return bestGroup;
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
		Map<Action, Set<Collaborator>> testActionsToGroups = new HashMap<>();
		Map<RecommendedEvolution<Collaborator>, Action> recommendationsToTestAction = new HashMap<>();
		
		
		for (RecommendedEvolution<Collaborator> recommendation : recommendations) {

			// Find best action to match with recommendation
			Action bestAction = matchGroupToAction(getRecommendedGroup(recommendation), testActions);
			if (bestAction != null) {
				recommendationsToTestAction.put(recommendation, bestAction);
			}
		}

		for (Action testAction : testActions) {
			
			Collection<Set<Collaborator>> allGroups = new ArrayList<>(oldGroups);
			for (RecommendedEvolution<Collaborator> usedRecommendation : recommendationsToTestAction.keySet()) {
				if(usedRecommendation instanceof RecommendedGroupChangeEvolution) {
					allGroups.remove(((RecommendedGroupChangeEvolution) usedRecommendation).getOldGroup());
				}
				Set<Collaborator> group = getRecommendedGroup(usedRecommendation);
				if (group != null) {
					allGroups.add(group);
				}
			}

			Set<Collaborator> bestGroup = null;

			for (Set<Collaborator> group : allGroups) {
				bestGroup = matchActionToGroup(testAction,
						bestGroup, group);
			}

			if (bestGroup != null) {
				testActionsToGroups
						.put(testAction, bestGroup);
			}
		}

		Collection<MetricResult> results = new ArrayList<MetricResult>();
		for (ActionBasedGroupEvolutionMetric<Collaborator, Action> metric : metrics) {
			results.add(metric.evaluate(oldGraph, newGraph, unusedRecommendations, testActions, recommendationsToTestAction, testActionsToGroups));
		}

		return results;
	}

}
