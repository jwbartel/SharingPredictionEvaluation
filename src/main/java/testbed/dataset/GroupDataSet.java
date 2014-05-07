package testbed.dataset;

import java.io.File;
import java.util.Collection;
import java.util.Set;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;

public abstract class GroupDataSet extends DataSet {

	public GroupDataSet(String name, int[] accountIds, File rootFolder) {
		super(name, accountIds, rootFolder);
	}

	public abstract File getGraphFile(int account);

	public abstract UndirectedGraph<Integer, DefaultEdge> getGraph(int account);

	public abstract File getIdealGroupsFile(int account);

	public abstract Collection<Set<Integer>> getIdealGroups(int account);

	public abstract File getSubstepsFolder(int account);

	public abstract Collection<Set<Integer>> getMaximalCliques(int account);

	public abstract File getPredictedGroupsFolder(String predictionType, int account);
	
	public abstract void writeGroupPredictions(String predictionType, int account, Collection<Set<Integer>> predictions);

	public abstract File getSeedlessMetricsFile();

}