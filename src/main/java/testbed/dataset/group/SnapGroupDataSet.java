package testbed.dataset.group;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import model.tools.evolution.MembershipChangeFinder;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import bus.tools.io.CollectionIOAssist;
import bus.tools.io.IntegerValueParser;
import bus.tools.io.SnapIOFunctions;

public class SnapGroupDataSet extends GroupDataSet<Integer> {

	public SnapGroupDataSet(String name, Integer[] accountIds, File rootFolder) {
		super(name, accountIds, rootFolder, Integer.class);
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
	public File getGraphFile(Integer account) {
		return new File(getPureDataFolder(), account+".edges");
	}

	@Override
	public UndirectedGraph<Integer, DefaultEdge> getGraph(Integer account) {
		return ioHelp.createUIDGraph(getGraphFile(account).getAbsolutePath());
	}

	@Override
	public File getIdealGroupsFile(Integer account) {
		return new File(getPureDataFolder(), account+".circles");
	}

	@Override
	public Collection<Set<Integer>> getIdealGroups(Integer account) {
		return ioHelp.loadIdealGroups(getIdealGroupsFile(account).getAbsolutePath());
	}

	@Override
	public File getSubstepsFolder(Integer account) {
		return new File(getSubstepsFolder(), ""+account);
	}

	@Override
	public Collection<Set<Integer>> getMaximalCliques(Integer account) {
		File maximalCliquesFile = new File(getSubstepsFolder(account), "maximal_cliques");
		return ioHelp.loadCliqueIDs(maximalCliquesFile.getAbsolutePath());
	}
	
	private File getPredictedGroupsFolder() {
		return new File(getRootFolder(), "Predicted groups");
	}
	
	@Override
	public File getPredictedGroupsFolder(String predictionType, Integer account) {
		File folder = new File(getPredictedGroupsFolder(), predictionType);
		if (!folder.exists()) {
			folder.mkdirs();
		}
		return new File(folder, account+".predictions");
	}

	@Override
	public void writeGroupPredictions(String predictionType, Integer account,
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
	
	private File getNewMembersFolder() {
		return new File(getRootFolder(), "New Membership");
	}
	
	@Override
	public File getNewMembersFile(Integer account, double growthRate, int test) {
		File growthRateFolder = new File(getNewMembersFolder(), ""+growthRate);
		return new File(growthRateFolder, "participant"+account+"_test"+test+".txt");
	}

	@Override
	public Set<Integer> getNewMembers(Integer account, double growthRate, int test) {
		try {
			File newMembersFile = getNewMembersFile(account, growthRate, test);
			if (!newMembersFile.exists()) {
				MembershipChangeFinder<Integer> changeFinder = new MembershipChangeFinder<>();
				Set<Integer> newMembers = changeFinder
						.getPseudoRandomNewIndividuals(getGraph(account)
								.vertexSet(), growthRate);
				CollectionIOAssist.writeCollection(newMembersFile, newMembers);
			}
			Set<Integer> newMembers = new HashSet<>(
					CollectionIOAssist.readCollection(newMembersFile,
							new IntegerValueParser()));
			return newMembers;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@Override
	public File getEvolutionMetricsFile() {
		return new File(getMetricsFolder(), "evolution results.csv");
	}
	
	

}
