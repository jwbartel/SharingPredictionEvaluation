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
import data.representation.actionbased.CollaborativeAction;

public class VertexGrowthRateMetric<Collaborator, Action extends CollaborativeAction<Collaborator>> implements
		ActionBasedGroupEvolutionMetric<Collaborator, Action> {

	@Override
	public MetricResult evaluate(
			UndirectedGraph<Collaborator, DefaultEdge> oldGraph,
			UndirectedGraph<Collaborator, DefaultEdge> newGraph,
			Collection<RecommendedEvolution<Collaborator>> recommendations,
			Collection<Action> testActions,
			Map<RecommendedEvolution<Collaborator>, Action> recommendationsToTestActions,
			Map<Action, Set<Collaborator>> testActionsToGroups) {
		
		Set<Collaborator> newVertices = new HashSet<>(newGraph.vertexSet());
		newVertices.removeAll(oldGraph.vertexSet());
		
		return new DoubleResult(((double) newVertices.size())/((double) oldGraph.vertexSet().size()));
	}

	@Override
	public String getHeader() {
		return "vertex growth rate";
	}

}
