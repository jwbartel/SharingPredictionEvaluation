package metrics.groups.actionbased.evolution;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import metrics.DoubleResult;
import metrics.MetricResult;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import recommendation.groups.evolution.recommendations.RecommendedEvolution;
import data.representation.actionbased.CollaborativeAction;

public class EdgeGrowthRateMetric<Collaborator, Action extends CollaborativeAction<Collaborator>> implements
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
