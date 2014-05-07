package model.recommendation.groups;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import metrics.MetricResult;
import metrics.groups.GroupMetric;
import metrics.groups.distance.GroupDistanceMetric;

public class SeedlessGroupRecommenationAcceptanceModeler<V> implements
		GroupRecommendationAcceptanceModeler {

	GroupDistanceMetric<V> distanceMetric;

	Collection<Set<V>> recommendations;
	Collection<Set<V>> idealGroups;
	Collection<GroupMetric<V>> metrics;

	public SeedlessGroupRecommenationAcceptanceModeler(GroupDistanceMetric<V> distanceMetric,
			Collection<Set<V>> recommendations, Collection<Set<V>> idealGroups,
			Collection<GroupMetric<V>> metrics) {
		this.distanceMetric = distanceMetric;
		this.recommendations = recommendations;
		this.idealGroups = idealGroups;
		this.metrics = metrics;
	}

	@Override
	public Collection<MetricResult> modelRecommendationAcceptance() {

		Map<Set<V>, Set<V>> recommendationsToIdeals = new HashMap<>();
		Collection<Set<V>> unusedIdeals = new HashSet<>(idealGroups);
		Collection<Set<V>> unusedRecommendations = new HashSet<Set<V>>(recommendations);

		for (Set<V> recommendation : recommendations) {

			double minDistance = Double.MAX_VALUE;
			Set<V> bestIdeal = null;

			for (Set<V> ideal : idealGroups) {
				if (unusedIdeals.contains(ideal)) {
					Double distance = distanceMetric.distance(recommendation, ideal);
					if (distance != null && distance < minDistance) {
						minDistance = distance;
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
