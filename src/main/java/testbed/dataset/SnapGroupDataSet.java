package testbed.dataset;

import groups.evolution.snap.SnapIOFunctions;

import java.io.File;
import java.util.Collection;
import java.util.Set;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;

public class SnapGroupDataSet extends GroupDataSet {

	public SnapGroupDataSet(String name, int[] accountIds, File rootFolder) {
		super(name, accountIds, rootFolder);
		ioHelp = new SnapIOFunctions<Integer>(Integer.class);
		ioHelp.setStoreSubSteps(false);
	}
	
	File getPureDataFolder() {
		return new File(getRootFolder(), "pure_data");
	}
	
	File getSubstepsFolder() {
		return new File(getRootFolder(), "substeps");
	}
	
	public File getGraphFile(int account) {
		return new File(getPureDataFolder(), account+".edges");
	}

	public UndirectedGraph<Integer, DefaultEdge> getGraph(int account) {
		return ioHelp.createUIDGraph(getGraphFile(account).getAbsolutePath());
	}

	public File getIdealGroupsFile(int account) {
		return new File(getPureDataFolder(), account+".circles");
	}

	public Collection<Set<Integer>> getIdealGroups(int account) {
		return ioHelp.loadIdealGroups(getIdealGroupsFile(account).getAbsolutePath());
	}

	public File getSubstepsFolder(int account) {
		return new File(getSubstepsFolder(), ""+account);
	}

	public Collection<Set<Integer>> getMaximalCliques(int account) {
		File maximalCliquesFile = new File(getSubstepsFolder(account), "maximal_cliques");
		return ioHelp.loadCliqueIDs(maximalCliquesFile.getAbsolutePath());
	}

}
