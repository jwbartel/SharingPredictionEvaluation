package metrics.groups.actionbased.evolution;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import metrics.MetricResult;
import metrics.groups.actionbased.ActionBasedGroupMetric;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import recommendation.groups.evolution.recommendations.RecommendedEvolution;
import recommendation.groups.evolution.recommendations.RecommendedGroupChangeEvolution;
import recommendation.groups.evolution.recommendations.RecommendedGroupCreationEvolution;
import data.representation.actionbased.CollaborativeAction;

public class EvolutionRepurposedActionBasedGroupMetric<Collaborator, Action extends CollaborativeAction<Collaborator>>
		implements ActionBasedGroupEvolutionMetric<Collaborator, Action> {

	private final ActionBasedGroupMetric<Collaborator, Action> actionBasedMetric;

	public EvolutionRepurposedActionBasedGroupMetric(
			ActionBasedGroupMetric<Collaborator, Action> actionBasedMetric) {
		this.actionBasedMetric = actionBasedMetric;
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
	public MetricResult evaluate(
			UndirectedGraph<Collaborator, DefaultEdge> oldGraph,
			UndirectedGraph<Collaborator, DefaultEdge> newGraph,
			Collection<RecommendedEvolution<Collaborator>> recommendations,
			Collection<Action> testActions,
			Map<RecommendedEvolution<Collaborator>, Action> recommendationsToTestActions,
			Map<Action, RecommendedEvolution<Collaborator>> testActionsToRecommendations) {

		Collection<Set<Collaborator>> finalRecommendedGroups = new ArrayList<>();
		for (RecommendedEvolution<Collaborator> recommendation : recommendations) {
			if (recommendation instanceof RecommendedGroupCreationEvolution) {

				finalRecommendedGroups.add(recommendation
						.getRecommenderEngineResult());

			} else if (recommendation instanceof RecommendedGroupChangeEvolution) {

				Set<Collaborator> recommendedGroup = 
						((RecommendedGroupChangeEvolution<Collaborator>) recommendation)
						.getMerging();
				finalRecommendedGroups.add(recommendedGroup);
			}
		}
		
		Map<Set<Collaborator>, Action> recommendedGroupsToTestActions = new HashMap<>();
		for (Entry<RecommendedEvolution<Collaborator>, Action> entry : recommendationsToTestActions.entrySet()) {
			RecommendedEvolution<Collaborator> recommendation = entry.getKey();
			Set<Collaborator> recommendedGroup = getRecommendedGroup(recommendation);
			recommendedGroupsToTestActions.put(recommendedGroup, entry.getValue());
		}
		
		Map<Action, Set<Collaborator>> testActionsToRecommendedGroup = new HashMap<>();
		for (Entry<Action, RecommendedEvolution<Collaborator>> entry : testActionsToRecommendations.entrySet()) {
			RecommendedEvolution<Collaborator> recommendation = entry.getValue();
			Set<Collaborator> recommendedGroup = getRecommendedGroup(recommendation);
			testActionsToRecommendedGroup.put(entry.getKey(), recommendedGroup);
		}

		return actionBasedMetric.evaluate(finalRecommendedGroups, new ArrayList<Set<Collaborator>>(), testActions,
				new HashMap<Set<Collaborator>, Set<Collaborator>>(), recommendedGroupsToTestActions,
				testActionsToRecommendedGroup);
	}

	@Override
	public String getHeader() {
		return actionBasedMetric.getHeader();
	}

}
