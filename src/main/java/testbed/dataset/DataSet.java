package testbed.dataset;

import java.io.File;

import recommendation.groups.seedless.hybrid.IOFunctions;

public abstract class DataSet<V> {
	protected IOFunctions<V> ioHelp;
	
	protected final String name;
	protected final V[] accountIds;
	protected final File rootFolder;
	
	public DataSet(String name, V[] accountIds, File rootFolder, Class<V> genericClass) {
		this.name = name;
		this.accountIds = accountIds;
		this.rootFolder = rootFolder;
		this.ioHelp = new IOFunctions<V>(genericClass);
	}
	
	public V[] getAccountIds() {
		return accountIds;
	}
	
	public File getRootFolder() {
		return rootFolder;
	}
	
	public String getName() {
		return name;
	}
}
