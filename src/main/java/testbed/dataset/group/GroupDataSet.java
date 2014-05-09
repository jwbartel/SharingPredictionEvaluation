package testbed.dataset.group;

import java.io.File;
import java.util.Collection;
import java.util.Set;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import testbed.dataset.DataSet;

public abstract class GroupDataSet<V> extends DataSet<V> {
	public GroupDataSet(String name, V[] accountIds, File rootFolder, Class<V> genericClass) {
		super(name, accountIds, rootFolder, genericClass);
	}

	public abstract File getGraphFile(V account);

	public abstract UndirectedGraph<Integer, DefaultEdge> getGraph(V account);

	public abstract File getIdealGroupsFile(V account);

	public abstract Collection<Set<Integer>> getIdealGroups(V account);

	public abstract File getSubstepsFolder(V account);

	public abstract Collection<Set<Integer>> getMaximalCliques(V account);

	public abstract File getPredictedGroupsFolder(String predictionType, V account);
	
	public abstract void writeGroupPredictions(String predictionType, V account, Collection<Set<Integer>> predictions);

	public abstract File getSeedlessMetricsFile();
	
	public abstract File getNewMembersFile(V account, double growthRate, int test);
	
	public abstract Set<Integer> getNewMembers(V account, double growthRate, int test);
	
	public abstract File getEvolutionMetricsFile();

}