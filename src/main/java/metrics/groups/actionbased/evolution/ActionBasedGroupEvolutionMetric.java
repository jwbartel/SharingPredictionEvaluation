package metrics.groups.actionbased.evolution;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import metrics.Metric;
import metrics.MetricResult;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import recommendation.groups.evolution.recommendations.RecommendedEvolution;
import data.representation.actionbased.CollaborativeAction;

public interface ActionBasedGroupEvolutionMetric<Collaborator, Action extends CollaborativeAction<Collaborator>>
		extends Metric {

	public MetricResult evaluate(
			SimpleGraph<Collaborator, DefaultEdge> oldGraph,
			SimpleGraph<Collaborator, DefaultEdge> newGraph,
			Collection<RecommendedEvolution<Collaborator>> recommendations,
			Collection<Set<Collaborator>> ideals,
			Collection<Action> testActions,
			Map<Set<Collaborator>, Set<Collaborator>> recommendationsToIdeals,
			Map<Set<Collaborator>, Action> recommendationsToTestActions,
			Map<Action, Set<Collaborator>> testActionsToRecommendations);
}
