package metrics.groups.evolution;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import metrics.DoubleResult;
import metrics.MetricResult;
import metrics.StatisticsResult;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import recommendation.groups.evolution.recommendations.RecommendedEvolution;
import recommendation.groups.evolution.recommendations.RecommendedGroupChangeEvolution;
import recommendation.groups.evolution.recommendations.RecommendedGroupCreationEvolution;

public class RelativeDeletions<V> extends GroupEvolutionMetric<V> {

	@Override
	public String getHeader() {
		return "avg-deletions,stdev-change deletions";
	}

	@Override
	public MetricResult evaluate(Map<Set<V>, Collection<Set<V>>> oldToNewIdealGroups,
			Collection<Set<V>> newlyCreatedIdealGroups,
			Map<RecommendedGroupChangeEvolution<V>, Set<V>> groupChangeToIdeal,
			Map<RecommendedGroupCreationEvolution<V>, Set<V>> groupCreationToIdeal,
			Collection<RecommendedEvolution<V>> unusedRecommendations,
			Collection<Set<V>> unusedIdeals) {

		DescriptiveStatistics stats = new DescriptiveStatistics();
		for (Entry<RecommendedGroupChangeEvolution<V>, Set<V>> entry : groupChangeToIdeal
				.entrySet()) {
			stats.addValue(requiredRelativeDeletions(entry.getKey().getMerging(), entry.getValue()));
		}
		for (Entry<RecommendedGroupCreationEvolution<V>, Set<V>> entry : groupCreationToIdeal
				.entrySet()) {
			stats.addValue(requiredRelativeDeletions(entry.getKey().getRecommenderEngineResult(),
					entry.getValue()));
		}

		return new StatisticsResult(stats);
	}

}
