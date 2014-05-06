package metrics.groups.similarity;

import java.util.HashSet;
import java.util.Set;

public class AddsAndDeletesGroupSimilarity<V> implements GroupSimilarityMetric<V>{

	@Override
	public double similarity(Set<V> group1, Set<V> group2) {
		Set<V> adds = new HashSet<V>(group2);
		adds.removeAll(group1);
		
		Set<V> deletes = new HashSet<V>(group1);
		deletes.removeAll(group2);
		
		return ((double) adds.size() + deletes.size())/Math.min(group1.size(), group2.size());
	}

}
