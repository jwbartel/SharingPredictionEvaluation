package metrics.groups;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import metrics.Metric;
import metrics.MetricResult;

public interface GroupMetric<V> extends Metric {

	public MetricResult evaluate(Map<Set<V>, Set<V>> recommendationsToIdeals,
			Collection<Set<V>> unusedRecommendations, Collection<Set<V>> unusedIdeals);
}
