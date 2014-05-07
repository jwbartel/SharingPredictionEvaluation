package metrics.groups.evolution;

import groups.evolution.recommendations.RecommendedEvolution;
import groups.evolution.recommendations.RecommendedGroupChangeEvolution;
import groups.evolution.recommendations.RecommendedGroupCreationEvolution;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import metrics.DoubleResult;
import metrics.MetricResult;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class RelativeCreationAdditions<V> extends GroupEvolutionMetric<V> {

	@Override
	public String getHeader() {
		return "avg-creation additions,stdev-creation additions";
	}

	@Override
	public MetricResult evaluate(Map<Set<V>, Collection<Set<V>>> oldToNewIdealGroups,
			Collection<Set<V>> newlyCreatedIdealGroups,
			Map<RecommendedGroupChangeEvolution<V>, Set<V>> groupChangeToIdeal,
			Map<RecommendedGroupCreationEvolution<V>, Set<V>> groupCreationToIdeal,
			Collection<RecommendedEvolution<V>> unusedRecommendations,
			Collection<Set<V>> unusedIdeals) {

		DescriptiveStatistics stats = new DescriptiveStatistics();
		for (Entry<RecommendedGroupCreationEvolution<V>, Set<V>> entry : groupCreationToIdeal
				.entrySet()) {
			stats.addValue(requiredRelativeAdditions(entry.getKey().getRecommenderEngineResult(),
					entry.getValue()));
		}

		return new DoubleResult(((double) unusedIdeals.size()) / newlyCreatedIdealGroups.size());
	}

}
