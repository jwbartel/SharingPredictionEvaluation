package metrics.groups.actionbased.evolution;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import metrics.DoubleResult;
import metrics.MetricResult;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import recommendation.groups.evolution.recommendations.RecommendedEvolution;
import recommendation.groups.evolution.recommendations.RecommendedGroupCreationEvolution;
import data.representation.actionbased.CollaborativeAction;

public class PercentRecommendedCreationRateMetric<Collaborator, Action extends CollaborativeAction<Collaborator>> implements
		ActionBasedGroupEvolutionMetric<Collaborator, Action> {

	@Override
	public MetricResult evaluate(
			UndirectedGraph<Collaborator, DefaultEdge> oldGraph,
			UndirectedGraph<Collaborator, DefaultEdge> newGraph,
			Collection<RecommendedEvolution<Collaborator>> recommendations,
			Collection<Action> testActions,
			Map<RecommendedEvolution<Collaborator>, Action> recommendationsToTestActions,
			Map<Action, Set<Collaborator>> testActionsToGroups) {
		
		int numCreated = 0;
		for (RecommendedEvolution<Collaborator> recommendation : recommendations) {
			if (recommendation instanceof RecommendedGroupCreationEvolution) {
				numCreated++;
			}
		}
		
		return new DoubleResult(((double) numCreated)/((double) recommendations.size()));
	}

	@Override
	public String getHeader() {
		return "percent of recommendations that are unchanged";
	}

}
