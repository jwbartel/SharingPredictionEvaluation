package testbed.dataset.actions;

import java.io.File;
import java.util.Collection;

import recommendation.general.actionbased.CollaborativeAction;
import recommendation.general.actionbased.CollaborativeActionThread;
import testbed.dataset.DataSet;

public abstract class ActionsDataSet<IdType, CollaboratorType, ActionType extends CollaborativeAction<CollaboratorType>, ThreadType extends CollaborativeActionThread<CollaboratorType, ActionType>>
		extends DataSet<IdType> {

	public ActionsDataSet(String name, IdType[] accountIds, File rootFolder,
			Class<IdType> genericClass) {
		super(name, accountIds, rootFolder, genericClass);
	}
	
	public abstract Collection<ActionType> getAllMessages(IdType account);
	public abstract Collection<ActionType> getTrainMessages(IdType account, double percentTrain);
	public abstract Collection<ActionType> getTestMessages(IdType account, double percentTrain);
	
	public abstract Collection<ThreadType> getAllThreads(IdType account);
	public abstract Collection<ThreadType> getTrainThreads(IdType account, double percentTrain);
	public abstract Collection<ThreadType> getTestThreads(IdType account, double percentTest);

}
