package testbed.dataset;

import java.io.File;

import recommendation.groups.seedless.hybrid.IOFunctions;

public abstract class DataSet<Id> {
	protected IOFunctions<Id> ioHelp;
	
	protected final String name;
	protected final Id[] accountIds;
	protected final File rootFolder;
	
	public DataSet(String name, Id[] accountIds, File rootFolder) {
		this.name = name;
		this.accountIds = accountIds;
		this.rootFolder = rootFolder;
	}
	
	public Id[] getAccountIds() {
		return accountIds;
	}
	
	public File getRootFolder() {
		return rootFolder;
	}
	
	public String getName() {
		return name;
	}
}
