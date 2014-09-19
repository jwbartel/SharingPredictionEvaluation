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
import recommendation.groups.evolution.recommendations.RecommendedGroupChangeEvolution;
import data.representation.actionbased.CollaborativeAction;

public class PercentMatchedRecommendedEvolutionsRateMetric<Collaborator, Action extends CollaborativeAction<Collaborator>> implements
		ActionBasedGroupEvolutionMetric<Collaborator, Action> {

	@Override
	public MetricResult evaluate(
			UndirectedGraph<Collaborator, DefaultEdge> oldGraph,
			UndirectedGraph<Collaborator, DefaultEdge> newGraph,
			Collection<RecommendedEvolution<Collaborator>> recommendations,
			Collection<Action> testActions,
			Map<RecommendedEvolution<Collaborator>, Action> recommendationsToTestActions,
			Map<Action, Set<Collaborator>> testActionsToGroups) {
		
		Set<RecommendedEvolution<Collaborator>> evolutionRecommendations = new HashSet<>();
		for (RecommendedEvolution<Collaborator> recommendation : recommendations) {
			if (recommendation instanceof RecommendedGroupChangeEvolution) {
				Set<Collaborator> evolution = ((RecommendedGroupChangeEvolution<Collaborator>) recommendation).getMerging();
				Set<Collaborator> oldGroup = recommendation.getRecommenderEngineResult();
				if (!evolution.containsAll(oldGroup) || !oldGroup.containsAll(evolution)) {
					evolutionRecommendations.add(recommendation);
				}
			}
		}
		
		Set<RecommendedEvolution<Collaborator>> matchedEvolutions = new HashSet<>(evolutionRecommendations);
		matchedEvolutions.retainAll(recommendationsToTestActions.keySet());
		
		return new DoubleResult(((double) matchedEvolutions.size())/((double) evolutionRecommendations.size()));
	}

	@Override
	public String getHeader() {
		return "percent matched evolution recommendations";
	}

}
