package metrics.groups.distance;

import java.util.HashSet;
import java.util.Set;

public class AddsAndDeletesGroupDistance<V> implements GroupDistanceMetric<V>{

	@Override
	public Double distance(Set<V> group1, Set<V> group2) {
		Set<V> adds = new HashSet<V>(group2);
		adds.removeAll(group1);
		
		Set<V> deletes = new HashSet<V>(group1);
		deletes.removeAll(group2);
		
		double distance = ((double) adds.size() + deletes.size());
		if (distance >= group2.size()) {
			return null;
		}
		return distance/Math.min(group1.size(), group2.size());
	}

	@Override
	public String getDistanceName() {
		return "Adds and Deletes";
	}

}
