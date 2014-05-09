package testbed.dataset.group;

import java.io.File;
import java.util.Collection;
import java.util.Set;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import testbed.dataset.DataSet;

public abstract class GroupDataSet extends DataSet<Integer> {

	public GroupDataSet(String name, Integer[] accountIds, File rootFolder) {
		super(name, accountIds, rootFolder, Integer.class);
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
	
	public abstract File getNewMembersFile(int account, double growthRate, int test);
	
	public abstract Set<Integer> getNewMembers(int account, double growthRate, int test);
	
	public abstract File getEvolutionMetricsFile();

}