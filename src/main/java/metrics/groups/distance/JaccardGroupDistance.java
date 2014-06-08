package metrics.groups.distance;

import java.util.HashSet;
import java.util.Set;

public class JaccardGroupDistance<V> implements GroupDistanceMetric<V>{

	@Override
	public Double distance(Set<V> group1, Set<V> group2) {
		Set<V> intersect = new HashSet<V>(group1);
		intersect.retainAll(group2);
		
		Set<V> union = new HashSet<V>(group1);
		union.addAll(group2);
		double distance = 1 - ((double) intersect.size())/union.size();
		if (distance == 1.0) {
			return null;
		}
		return distance;
	}

	@Override
	public String getDistanceName() {
		return "Jaccard";
	}

}
