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
	
	@Override
	public File getGraphFile(int account) {
		return new File(getPureDataFolder(), account+".edges");
	}

	@Override
	public UndirectedGraph<Integer, DefaultEdge> getGraph(int account) {
		return ioHelp.createUIDGraph(getGraphFile(account).getAbsolutePath());
	}

	@Override
	public File getIdealGroupsFile(int account) {
		return new File(getPureDataFolder(), account+".circles");
	}

	@Override
	public Collection<Set<Integer>> getIdealGroups(int account) {
		return ioHelp.loadIdealGroups(getIdealGroupsFile(account).getAbsolutePath());
	}

	@Override
	public File getSubstepsFolder(int account) {
		return new File(getSubstepsFolder(), ""+account);
	}

	@Override
	public Collection<Set<Integer>> getMaximalCliques(int account) {
		File maximalCliquesFile = new File(getSubstepsFolder(account), "maximal_cliques");
		return ioHelp.loadCliqueIDs(maximalCliquesFile.getAbsolutePath());
	}
	
	private File getPredictedGroupsFolder() {
		return new File(getRootFolder(), "Predicted groups");
	}
	
	@Override
	public File getPredictedGroupsFolder(String predictionType, int account) {
		File folder = new File(getPredictedGroupsFolder(), predictionType);
		if (!folder.exists()) {
			folder.mkdirs();
		}
		return new File(folder, account+".predictions");
	}

	@Override
	public void writeGroupPredictions(String predictionType, int account,
			Collection<Set<Integer>> predictions) {
		
		File outFile = getPredictedGroupsFolder(predictionType, account);
		ioHelp.printCliqueIDsToFile(outFile.getAbsolutePath(), predictions);
		
	}
	
	private File getMetricsFolder() {
		return new File(getRootFolder(), "metric statistics");
	}
	
	@Override
	public File getSeedlessMetricsFile() {
		return new File(getMetricsFolder(), "seedless results.csv");
	}

}
