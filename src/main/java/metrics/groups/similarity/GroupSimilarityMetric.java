package metrics.groups.similarity;

import java.util.Set;

public interface GroupSimilarityMetric<V> {

	public double similarity(Set<V> group1, Set<V> group2);
}
