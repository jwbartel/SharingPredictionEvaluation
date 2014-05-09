package testbed.dataset.group;

import java.io.File;
import java.util.Collection;
import java.util.Set;

public class MixedInitiativeDataSet extends GroupDataSet<Integer> {

	public MixedInitiativeDataSet(String name, Integer[] accountIds,
			File rootFolder) {
		super(name, accountIds, rootFolder, Integer.class);
	}

	private File getGraphFolder() {
		return new File(getRootFolder(), "Friend Graphs");
	}
	
	private File getIdNamesFile(Integer account) {
		return new File(getGraphFolder(), account + "_People.txt");
	}

	@Override
	public File getGraphFile(Integer account) {
		return new File(getGraphFolder(), account + "_MutualFriends.txt");
	}

	private File getIdealGroupsFolder() {
		return new File(getRootFolder(), "ideal");
	}

	@Override
	public File getIdealGroupsFile(Integer account) {
		return new File(getIdealGroupsFolder(), account + "_ideal.txt");
	}
	
	@Override
	public Collection<Set<Integer>> getIdealGroups(Integer account) {
		ioHelp.fillNamesAndIDs(getIdNamesFile(account).getAbsolutePath());
		return super.getIdealGroups(account);
	}

	private File getSubstepsFolder() {
		return new File(getRootFolder(), "substeps");
	}
	
	@Override
	public File getSubstepsFolder(Integer account) {
		return new File(getSubstepsFolder(), ""+account);
	}
	
	private File getPredictedGroupsFolder() {
		return new File(getRootFolder(), "Predicted groups");
	}

	@Override
	public File getPredictedGroupsFile(String predictionType, Integer account) {
		File folder = new File(getPredictedGroupsFolder(), predictionType);
		if (!folder.exists()) {
			folder.mkdirs();
		}
		return new File(folder, account+".predictions");
	}
	
	private File getNewMembersFolder() {
		return new File(getRootFolder(), "New Membership");
	}

	@Override
	public File getNewMembersFile(Integer account, double growthRate, int test) {
		File growthRateFolder = new File(getNewMembersFolder(), ""+growthRate);
		return new File(growthRateFolder, "participant"+account+"_test"+test+".txt");
	}
	
	private File getMetricsFolder() {
		return new File(getRootFolder(), "metric statistics");
	}

	@Override
	public File getSeedlessMetricsFile() {
		return new File(getMetricsFolder(), "seedless results.csv");
	}
	
	@Override
	public File getEvolutionMetricsFile() {
		return new File(getMetricsFolder(), "evolution results.csv");
	}
	
	private File getMaximalCliquesFolder() {
		return new File(getRootFolder(), "MaximalCliques");
	}

	@Override
	public File getMaximalCliquesFile(Integer account) {
		return new File(getMaximalCliquesFolder(), account+"_MaximalCliques.txt");
	}

}
