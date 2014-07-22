package metrics.groups.actionbased.evolution;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import metrics.DoubleResult;
import metrics.MetricResult;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import recommendation.groups.evolution.recommendations.RecommendedEvolution;
import data.representation.actionbased.CollaborativeAction;

public class VertexGrowthRateMetric<Collaborator, Action extends CollaborativeAction<Collaborator>> implements
		ActionBasedGroupEvolutionMetric<Collaborator, Action> {

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
		
		Set<Collaborator> newVertices = new HashSet<>(newGraph.vertexSet());
		newVertices.removeAll(oldGraph.vertexSet());
		
		return new DoubleResult(((double) newVertices.size())/((double) oldGraph.vertexSet().size()));
	}

	@Override
	public String getHeader() {
		return "vertex growth rate";
	}

}
