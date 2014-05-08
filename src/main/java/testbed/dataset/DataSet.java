package testbed.dataset;

import groups.seedless.hybrid.IOFunctions;

import java.io.File;

public class DataSet {
	protected IOFunctions<Integer> ioHelp;
	
	protected final String name;
	protected final int[] accountIds;
	protected final File rootFolder;
	
	public DataSet(String name, int[] accountIds, File rootFolder) {
		this.name = name;
		this.accountIds = accountIds;
		this.rootFolder = rootFolder;
		this.ioHelp = new IOFunctions<Integer>(Integer.class);
	}
	
	public int[] getAccountIds() {
		return accountIds;
	}
	
	public File getRootFolder() {
		return rootFolder;
	}
	
	public String getName() {
		return name;
	}
}
