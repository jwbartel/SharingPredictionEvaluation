package metrics.groups.actionbased.evolution;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import metrics.DoubleResult;
import metrics.MetricResult;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import recommendation.groups.evolution.recommendations.RecommendedEvolution;
import recommendation.groups.evolution.recommendations.RecommendedGroupCreationEvolution;
import data.representation.actionbased.CollaborativeAction;

public class PercentMatchedRecommendedCreationRateMetric<Collaborator, Action extends CollaborativeAction<Collaborator>> implements
		ActionBasedGroupEvolutionMetric<Collaborator, Action> {

	@Override
	public MetricResult evaluate(
			UndirectedGraph<Collaborator, DefaultEdge> oldGraph,
			UndirectedGraph<Collaborator, DefaultEdge> newGraph,
			Collection<RecommendedEvolution<Collaborator>> recommendations,
			Collection<Action> testActions,
			Map<RecommendedEvolution<Collaborator>, Action> recommendationsToTestActions,
			Map<Action, RecommendedEvolution<Collaborator>> testActionsToRecommendations) {

		Set<RecommendedEvolution<Collaborator>> creationRecommendations = new HashSet<>();
		for (RecommendedEvolution<Collaborator> recommendation : recommendations) {
			if (recommendation instanceof RecommendedGroupCreationEvolution) {
				creationRecommendations.add(recommendation);
			}
		}

		Set<RecommendedEvolution<Collaborator>> matchedCreations = new HashSet<>(creationRecommendations);
		matchedCreations.retainAll(recommendationsToTestActions.keySet());
		
		return new DoubleResult(((double) matchedCreations.size())/((double) creationRecommendations.size()));
	}

	@Override
	public String getHeader() {
		return "percent of recommendations that are unchanged";
	}

}
