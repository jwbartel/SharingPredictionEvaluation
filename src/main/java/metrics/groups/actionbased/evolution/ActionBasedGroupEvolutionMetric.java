package metrics.groups.actionbased.evolution;

import java.util.Collection;
import java.util.Map;

import metrics.Metric;
import metrics.MetricResult;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import recommendation.groups.evolution.recommendations.RecommendedEvolution;
import data.representation.actionbased.CollaborativeAction;

public interface ActionBasedGroupEvolutionMetric<Collaborator, Action extends CollaborativeAction<Collaborator>>
		extends Metric {

	public MetricResult evaluate(
			UndirectedGraph<Collaborator, DefaultEdge> oldGraph,
			UndirectedGraph<Collaborator, DefaultEdge> newGraph,
			Collection<RecommendedEvolution<Collaborator>> recommendations,
			Collection<Action> testActions,
			Map<RecommendedEvolution<Collaborator>, Action> recommendationsToTestActions,
			Map<Action, RecommendedEvolution<Collaborator>> testActionsToRecommendations);
}
