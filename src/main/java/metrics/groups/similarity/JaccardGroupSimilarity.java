package metrics.groups.similarity;

import java.util.HashSet;
import java.util.Set;

public class JaccardGroupSimilarity<V> implements GroupSimilarityMetric<V>{

	@Override
	public double similarity(Set<V> group1, Set<V> group2) {
		Set<V> intersect = new HashSet<V>(group1);
		intersect.retainAll(group2);
		
		Set<V> union = new HashSet<V>(group1);
		union.addAll(group2);
		return ((double) intersect.size())/union.size();
	}

}
