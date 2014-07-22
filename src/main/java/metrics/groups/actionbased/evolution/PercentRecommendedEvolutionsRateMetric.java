package metrics.groups.actionbased.evolution;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import metrics.DoubleResult;
import metrics.MetricResult;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import recommendation.groups.evolution.recommendations.RecommendedEvolution;
import recommendation.groups.evolution.recommendations.RecommendedGroupChangeEvolution;
import data.representation.actionbased.CollaborativeAction;

public class PercentRecommendedEvolutionsRateMetric<Collaborator, Action extends CollaborativeAction<Collaborator>> implements
		ActionBasedGroupEvolutionMetric<Collaborator, Action> {

	@Override
	public MetricResult evaluate(
			UndirectedGraph<Collaborator, DefaultEdge> oldGraph,
			UndirectedGraph<Collaborator, DefaultEdge> newGraph,
			Collection<RecommendedEvolution<Collaborator>> recommendations,
			Collection<Action> testActions,
			Map<RecommendedEvolution<Collaborator>, Action> recommendationsToTestActions,
			Map<Action, RecommendedEvolution<Collaborator>> testActionsToRecommendations) {
		
		int numEvolutions = 0;
		for (RecommendedEvolution<Collaborator> recommendation : recommendations) {
			if (recommendation instanceof RecommendedGroupChangeEvolution) {
				Set<Collaborator> evolution = ((RecommendedGroupChangeEvolution<Collaborator>) recommendation).getMerging();
				Set<Collaborator> oldGroup = recommendation.getRecommenderEngineResult();
				if (!evolution.containsAll(oldGroup) || !oldGroup.containsAll(evolution)) {
					numEvolutions++;
				}
			}
		}
		
		return new DoubleResult(((double) numEvolutions)/((double) recommendations.size()));
	}

	@Override
	public String getHeader() {
		return "percent of recommendations that are evolutions";
	}

}
