package testbed.dataset.group;

import java.io.File;

import bus.tools.io.SnapIOFunctions;

public class SnapGroupDataSet extends GroupDataSet<Integer> {
	
	public static final Integer[] DEFAULT_ACCOUNT_SET = { 0, 348, 414, 686,
			698, 1684, 3437, 3980 };

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
	public File getIdealGroupsFile(Integer account) {
		return new File(getPureDataFolder(), account+".circles");
	}

	@Override
	public File getSubstepsFolder(Integer account) {
		return new File(getSubstepsFolder(), ""+account);
	}
	
	@Override
	public File getMaximalCliquesFile(Integer account) {
		return new File(getSubstepsFolder(account), "maximal_cliques");
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
	public File getEvolutionMetricsFile() {
		return new File(getMetricsFolder(), "evolution results.csv");
	}
	
	

}
