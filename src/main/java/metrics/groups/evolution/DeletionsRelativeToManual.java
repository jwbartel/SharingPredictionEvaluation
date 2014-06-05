package metrics.groups.evolution;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import metrics.DoubleResult;
import metrics.MetricResult;
import recommendation.groups.evolution.recommendations.RecommendedEvolution;
import recommendation.groups.evolution.recommendations.RecommendedGroupChangeEvolution;
import recommendation.groups.evolution.recommendations.RecommendedGroupCreationEvolution;

public class DeletionsRelativeToManual<V> extends GroupEvolutionMetric<V> {

	@Override
	public String getHeader() {
		return "deletions relative to manual";
	}

	@Override
	public MetricResult evaluate(Set<Integer> newMembers, Map<Set<V>, Collection<Set<V>>> oldToNewIdealGroups,
			Collection<Set<V>> newlyCreatedIdealGroups,
			Map<RecommendedGroupChangeEvolution<V>, Set<V>> groupChangeToIdeal,
			Map<RecommendedGroupCreationEvolution<V>, Set<V>> groupCreationToIdeal,
			Collection<RecommendedEvolution<V>> unusedRecommendations,
			Collection<Set<V>> unusedIdeals) {

		int deletions = 0;
		for (Entry<RecommendedGroupChangeEvolution<V>, Set<V>> entry : groupChangeToIdeal
				.entrySet()) {
			deletions += requiredDeletions(entry.getKey().getMerging(), entry.getValue());
		}
		for (Entry<RecommendedGroupCreationEvolution<V>, Set<V>> entry : groupCreationToIdeal
				.entrySet()) {
			deletions += requiredDeletions(entry.getKey().getRecommenderEngineResult(),
					entry.getValue());
		}
		
		int manualDeletions = 0;
		for (Set<V> oldIdeal : oldToNewIdealGroups.keySet()) {
			for (Set<V> evolvedIdeal : oldToNewIdealGroups.get(oldIdeal)) {
				manualDeletions = (int) requiredDeletions(oldIdeal, evolvedIdeal);
			}
		}
		for (Set<V> newlyCreatedIdeal : newlyCreatedIdealGroups) {
			manualDeletions += newlyCreatedIdeal.size();
		}

		return new DoubleResult(((double) deletions)/manualDeletions);
	}

}
