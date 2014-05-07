package metrics.groups.distance;

import java.util.Set;

public interface GroupDistanceMetric<V> {

	public Double distance(Set<V> group1, Set<V> group2);
}
