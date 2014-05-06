package model.recommendation.groups;

import groups.seedless.SeedlessGroupRecommender;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import metrics.MetricResult;
import metrics.groups.GroupMetric;
import metrics.groups.similarity.GroupSimilarityMetric;

public class SeedlessGroupRecommenationAcceptanceModeler<V> implements
		GroupRecommendationAcceptanceModeler {

	GroupSimilarityMetric<V> similarityMetric;

	SeedlessGroupRecommender<V> recommender;
	Collection<Set<V>> idealGroups;
	Collection<GroupMetric<V>> metrics;

	public SeedlessGroupRecommenationAcceptanceModeler(GroupSimilarityMetric<V> similarityMetric,
			SeedlessGroupRecommender<V> recommender, Collection<Set<V>> idealGroups,
			Collection<GroupMetric<V>> metrics) {
		this.similarityMetric = similarityMetric;
		this.recommender = recommender;
		this.idealGroups = idealGroups;
		this.metrics = metrics;
	}

	@Override
	public Collection<MetricResult> modelRecommendationAcceptance() {

		Map<Set<V>, Set<V>> recommendationsToIdeals = new HashMap<>();
		Collection<Set<V>> unusedIdeals = new HashSet<>(idealGroups);

		Collection<Set<V>> recommendations = recommender.getRecommendations();
		Collection<Set<V>> unusedRecommendations = new HashSet<Set<V>>(recommendations);

		for (Set<V> recommendation : recommendations) {

			double maxSimilarity = 0.0;
			Set<V> bestIdeal = null;

			for (Set<V> ideal : idealGroups) {
				if (unusedIdeals.contains(ideal)) {
					double similarity = similarityMetric.similarity(recommendation, ideal);
					if (similarity > maxSimilarity) {
						maxSimilarity = similarity;
						bestIdeal = ideal;
					}
				}
			}

			if (bestIdeal != null) {
				recommendationsToIdeals.put(recommendation, bestIdeal);
				unusedRecommendations.remove(recommendation);
				unusedIdeals.remove(bestIdeal);
			}
		}

		Collection<MetricResult> results = new ArrayList<MetricResult>();
		for (GroupMetric<V> metric : metrics) {
			results.add(metric.evaluate(recommendationsToIdeals, unusedRecommendations,
					unusedIdeals));
		}

		return results;

	}

}
