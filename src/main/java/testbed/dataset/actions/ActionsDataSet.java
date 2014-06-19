package testbed.dataset.actions;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import testbed.dataset.DataSet;
import data.representation.actionbased.CollaborativeAction;
import data.representation.actionbased.CollaborativeActionThread;

public abstract class ActionsDataSet<IdType, CollaboratorType, ActionType extends CollaborativeAction<CollaboratorType>, ThreadType extends CollaborativeActionThread<CollaboratorType, ActionType>>
		extends DataSet<IdType> {


	public static class ThreadFold<CollaboratorType, ActionType extends CollaborativeAction<CollaboratorType>, ThreadType extends CollaborativeActionThread<CollaboratorType, ActionType>> {
		public final Collection<ThreadType> trainThreads;
		public final Collection<ThreadType> testThreads;

		public ThreadFold(Collection<ThreadType> trainThreads,
				Collection<ThreadType> testThreads) {
			this.trainThreads = trainThreads;
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
		for (int foldId = 0; foldId < folds.size(); foldId++) {

			Collection<ThreadType> trainThreads = new ArrayList<>();
			Collection<ThreadType> testThreads = new ArrayList<>(
					folds.get(foldId));

			for (int i = 0; i < folds.size(); i++) {
				if (i != foldId) {
					trainThreads.addAll(folds.get(i));
				}
			}
			
			retVal.put(foldId, new ThreadFold<>(trainThreads, testThreads));
		}
		return retVal;
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
