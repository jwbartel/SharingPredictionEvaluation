package metrics.groups.actionbased.evolution;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import metrics.MetricResult;
import metrics.groups.actionbased.ActionBasedGroupMetric;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

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

	@Override
	public MetricResult evaluate(
			SimpleGraph<Collaborator, DefaultEdge> oldGraph,
			SimpleGraph<Collaborator, DefaultEdge> newGraph,
			Collection<RecommendedEvolution<Collaborator>> recommendations,
			Collection<Set<Collaborator>> ideals,
			Collection<Action> testActions,
			Map<Set<Collaborator>, Set<Collaborator>> recommendationsToIdeals,
			Map<Set<Collaborator>, Action> recommendationsToTestActions,
			Map<Action, Set<Collaborator>> testActionsToRecommendations) {

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

		return actionBasedMetric.evaluate(finalRecommendedGroups, ideals, testActions,
				recommendationsToIdeals, recommendationsToTestActions,
				testActionsToRecommendations);
	}

	@Override
	public String getHeader() {
		return actionBasedMetric.getHeader();
	}

}
