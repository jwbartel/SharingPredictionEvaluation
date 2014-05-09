package testbed.dataset.group;

import java.io.File;
import java.util.Collection;
import java.util.Set;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;

public class MixedInitiativeDataSet extends GroupDataSet<Integer>{

	@Override
	public File getGraphFile(Integer account) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UndirectedGraph<Integer, DefaultEdge> getGraph(Integer account) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public File getIdealGroupsFile(Integer account) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Set<Integer>> getIdealGroups(Integer account) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public File getSubstepsFolder(Integer account) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Set<Integer>> getMaximalCliques(Integer account) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public File getPredictedGroupsFolder(String predictionType, Integer account) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void writeGroupPredictions(String predictionType, Integer account,
			Collection<Set<Integer>> predictions) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public File getSeedlessMetricsFile() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public File getNewMembersFile(Integer account, double growthRate, int test) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Integer> getNewMembers(Integer account, double growthRate,
			int test) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public File getEvolutionMetricsFile() {
		// TODO Auto-generated method stub
		return null;
	}

}
