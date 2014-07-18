package testbed.dataset.actions;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import recommendation.groups.seedless.hybrid.IOFunctions;
import testbed.dataset.DataSet;
import data.representation.actionbased.CollaborativeAction;
import data.representation.actionbased.CollaborativeActionThread;

public abstract class ActionsDataSet<IdType, CollaboratorType, ActionType extends CollaborativeAction<CollaboratorType>, ThreadType extends CollaborativeActionThread<CollaboratorType, ActionType>>
		extends DataSet<IdType> {


	public static class ThreadFold<CollaboratorType, ActionType extends CollaborativeAction<CollaboratorType>, ThreadType extends CollaborativeActionThread<CollaboratorType, ActionType>> {
		public final Collection<ThreadType> trainThreads;
		public final Collection<ThreadType> validationThreads;
		public final Collection<ThreadType> testThreads;

		public ThreadFold(Collection<ThreadType> trainThreads,
				Collection<ThreadType> validationThreads,
				Collection<ThreadType> testThreads) {
			this.trainThreads = trainThreads;
			this.validationThreads = validationThreads;
			this.testThreads = testThreads;
		}
	}

	public ActionsDataSet(String name, IdType[] accountIds, File rootFolder,
			Class<IdType> genericClass) {
		super(name, accountIds, rootFolder, genericClass);
	}
	
	public Map<Integer, ThreadFold<CollaboratorType, ActionType, ThreadType>> getThreadFolds(
			IdType account, int numFolds) {

		List<ThreadType> threads = new ArrayList<>(getAllThreads(account));
		List<Collection<ThreadType>> folds = new ArrayList<>(numFolds);
		
		int foldSize = threads.size() / numFolds;
		int remainder = threads.size() % foldSize;
		
		int pos = 0;
		int foldNum = 0;
		while (pos < threads.size() && foldNum < numFolds) {
			
			Collection<ThreadType> fold = new ArrayList<>();
			for (int i=0; i< foldSize; i++) {
				fold.add(threads.get(pos));
				pos++;
			}
			if (remainder > 0) {
				fold.add(threads.get(pos));
				pos++;
				remainder--;
			}
			folds.add(fold);
			foldNum++;
		}

		Map<Integer, ThreadFold<CollaboratorType, ActionType, ThreadType>> retVal =
				new TreeMap<>();
		int validationPosition = folds.size() - 2;
		int testPostion = folds.size()-1;
		for (int foldId=0; foldId<folds.size(); foldId++) {

			Collection<ThreadType> trainThreads = new ArrayList<>();
			Collection<ThreadType> validationThreads = new ArrayList<>(
					folds.get(validationPosition));
			Collection<ThreadType> testThreads = new ArrayList<>(
					folds.get(testPostion));
			

			for (int j = 0; j < folds.size(); j++) {
				if (j != validationPosition && j != testPostion) {
					trainThreads.addAll(folds.get(j));
				}
			}
			
			validationPosition = (validationPosition + 1) % folds.size();
			testPostion = (testPostion + 1) % folds.size();
			

			retVal.put(foldId, new ThreadFold<>(trainThreads, validationThreads, testThreads));
		}
		return retVal;
	}
	
	private File getGroupsFolder(IdType account) {
		File folder = new File(getRootFolder(), "groups");
		if (account != null) {
			return new File(folder, account.toString());
		} else {
			return folder;
		}
	}
	
	private File getArgumentlessGraphBasedFolder(IdType account,
			String graphBuilderType) {
		File groupsFolder = getGroupsFolder(account);
		groupsFolder = new File(groupsFolder, graphBuilderType);
		return groupsFolder;
	}
	
	public File getArgumentlessGraphBasedGroupsFile(IdType account,
			String graphBuilderType) {
		File groupsFolder = getArgumentlessGraphBasedFolder(account,
				graphBuilderType);
		return new File(groupsFolder, "groups.txt");
	}
	
	public File getArgumentlessGraphBasedGraphFile(IdType account,
			String graphBuilderType) {
		File groupsFolder = getArgumentlessGraphBasedFolder(account,
				graphBuilderType);
		return new File(groupsFolder, "graph.txt");
	}
	
	public File getTimeThresholdGraphBasedFolder(IdType account,
			String graphBuilderType, String timeThreshold) {
		File groupsFolder = getTimeThresholdGraphBasedFolder(account,
				graphBuilderType, timeThreshold);
		return groupsFolder;
	}

	public File getTimeThresholdGraphBasedGroupsFile(IdType account,
			String graphBuilderType, String timeThreshold) {
		File groupsFolder = getGroupsFolder(account);
		groupsFolder = new File(groupsFolder, graphBuilderType);
		groupsFolder = new File(groupsFolder, timeThreshold);
		return new File(groupsFolder, "groups.txt");
	}

	public File getTimeThresholdGraphBasedGraphFile(IdType account,
			String graphBuilderType, String timeThreshold) {
		File groupsFolder = getGroupsFolder(account);
		groupsFolder = new File(groupsFolder, graphBuilderType);
		groupsFolder = new File(groupsFolder, timeThreshold);
		return new File(groupsFolder, "graph.txt");
	}


	public File getScoredEdgesGraphBasedFolder(IdType account,
			String graphBuilderType,
			String halfLife,
			double wOut,
			double scoreThreshold) {
		File groupsFolder = getGroupsFolder(account);
		groupsFolder = new File(groupsFolder, graphBuilderType);
		groupsFolder = new File(groupsFolder, "halfLife-" + halfLife);
		groupsFolder = new File(groupsFolder, "wOut-" + wOut);
		groupsFolder = new File(groupsFolder, "scoreThreshold-" + scoreThreshold);
		return groupsFolder;
	}

	public File getScoredEdgesGraphBasedGraphFile(IdType account,
			String graphBuilderType,
			String halfLife,
			double wOut,
			double scoreThreshold) {
		File groupsFolder = getScoredEdgesGraphBasedFolder(account,
				graphBuilderType, halfLife, wOut, scoreThreshold);
		return new File(groupsFolder, "graph.txt");
	}

	public File getScoredEdgesGraphBasedGroupsFile(IdType account,
			String graphBuilderType,
			String halfLife,
			double wOut,
			double scoreThreshold) {
		File groupsFolder = getScoredEdgesGraphBasedFolder(account,
				graphBuilderType, halfLife, wOut, scoreThreshold);
		return new File(groupsFolder, "groups.txt");
	}
	
	public Collection<Set<CollaboratorType>> loadGroups(File groupsFile,
			IOFunctions<CollaboratorType> ioHelp) {

		return ioHelp.loadCliqueIDs(groupsFile.getAbsolutePath());
	}
	
	public abstract Collection<ActionType> getAllMessages(IdType account);
	public abstract Collection<ActionType> getTrainMessages(IdType account, double percentTrain);
	public abstract Collection<ActionType> getTestMessages(IdType account, double percentTrain);
	
	public abstract Collection<ThreadType> getAllThreads(IdType account);
	public abstract Collection<ThreadType> getTrainThreads(IdType account, double percentTrain);
	public abstract Collection<ThreadType> getTestThreads(IdType account, double percentTest);
	
	public abstract File getActionBasedSeedlessGroupsMetricsFile();
	public abstract File getBurstyGroupsMetricsFile();

}
