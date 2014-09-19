package metrics.groups.actionbased.evolution;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import metrics.DoubleResult;
import metrics.MetricResult;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import recommendation.groups.evolution.recommendations.RecommendedEvolution;
import data.representation.actionbased.CollaborativeAction;

public class EdgeGrowthRateMetric<Collaborator, Action extends CollaborativeAction<Collaborator>> implements
		ActionBasedGroupEvolutionMetric<Collaborator, Action> {
	
	@Override
	public MetricResult evaluate(
			UndirectedGraph<Collaborator, DefaultEdge> oldGraph,
			UndirectedGraph<Collaborator, DefaultEdge> newGraph,
			Collection<RecommendedEvolution<Collaborator>> recommendations,
			Collection<Action> testActions,
			Map<RecommendedEvolution<Collaborator>, Action> recommendationsToTestActions,
			Map<Action, Set<Collaborator>> testActionsToGroups) {
		
		int numNewEdges = 0;
		for (DefaultEdge edge : newGraph.edgeSet()) {
			Collaborator src = newGraph.getEdgeSource(edge);
			Collaborator tgt = newGraph.getEdgeTarget(edge);
			if (!oldGraph.containsEdge(src, tgt)) {
				numNewEdges++;
			}
		}
		
		return new DoubleResult(((double) numNewEdges)/((double) oldGraph.edgeSet().size()));
	}

	@Override
	public String getHeader() {
		return "edge growth rate";
	}

}
