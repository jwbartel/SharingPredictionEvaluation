package metrics.groups.evolution;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import metrics.MetricResult;
import metrics.StatisticsResult;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import recommendation.groups.evolution.recommendations.RecommendedEvolution;
import recommendation.groups.evolution.recommendations.RecommendedGroupChangeEvolution;
import recommendation.groups.evolution.recommendations.RecommendedGroupCreationEvolution;

public class MissedIdealSizes<V> extends GroupEvolutionMetric<V> {
	
	@Override
	public String getHeader() {
		return "avg-missed ideal size,stdev-missed ideal size";
	}

	@Override
	public MetricResult evaluate(Map<Set<V>, Collection<Set<V>>> oldToNewIdealGroups,
			Collection<Set<V>> newlyCreatedIdealGroups,
			Map<RecommendedGroupChangeEvolution<V>, Set<V>> groupChangeToIdeal,
			Map<RecommendedGroupCreationEvolution<V>, Set<V>> groupCreationToIdeal,
			Collection<RecommendedEvolution<V>> unusedRecommendations,
			Collection<Set<V>> unusedIdeals) {

		DescriptiveStatistics stats = new DescriptiveStatistics();
		for (Set<V> unusedIdeal : unusedIdeals) {
			stats.addValue(unusedIdeal.size());
		}
		
		return new StatisticsResult(stats);
	}

}
