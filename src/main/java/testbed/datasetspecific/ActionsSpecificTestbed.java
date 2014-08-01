package testbed.datasetspecific;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import testbed.ActionBasedBurstyGroupCreationTestbed;
import testbed.ActionBasedEvolutionTestbed;
import testbed.ActionBasedSeedlessGroupCreationTestbed;
import testbed.ConstantValues;
import testbed.dataset.actions.ActionsDataSet;
import data.representation.actionbased.CollaborativeAction;
import data.representation.actionbased.CollaborativeActionThread;

public abstract class ActionsSpecificTestbed<Id, Collaborator extends Comparable<Collaborator>, Action extends CollaborativeAction<Collaborator>, ActionThread extends CollaborativeActionThread<Collaborator, Action>>
		implements DatasetSpecificTestbed {

	Class<Id> idClass;
	Class<Collaborator> collaboratorClass;
	Class<Action> actionClass;
	Class<ActionThread> threadClass;

	public ActionsSpecificTestbed(Class<Id> idClass,
			Class<Collaborator> collaboratorClass, Class<Action> actionClass,
			Class<ActionThread> threadClass) {

		this.idClass = idClass;
		this.collaboratorClass = collaboratorClass;
		this.actionClass = actionClass;
		this.threadClass = threadClass;
	}

	public abstract Collection<ActionsDataSet<Id, Collaborator, Action, ActionThread>> getActionsDatasets();

	@Override
	public void runTestbed() throws Exception {
		Collection<ActionsDataSet<Id, Collaborator, Action, ActionThread>> datasets = getActionsDatasets();
		runActionTests(datasets);
	}

	public void runActionTests(
			Collection<ActionsDataSet<Id, Collaborator, Action, ActionThread>> datasets)
			throws Exception {

		runSeedlessGroupCreationTest(datasets);
		runBurstyGroupCreationTest(datasets);
		runGroupEvolutionTest(datasets);
	}

	public abstract Map<String, Collection<ConstantValues>> getGraphConstants();

	public void runSeedlessGroupCreationTest(
			Collection<ActionsDataSet<Id, Collaborator, Action, ActionThread>> datasets)
			throws IOException {
		ActionBasedSeedlessGroupCreationTestbed<Id, Collaborator, Action, ActionThread> testbed = new ActionBasedSeedlessGroupCreationTestbed<>(
				datasets, getGraphConstants(), collaboratorClass, actionClass);

		testbed.runTestbed();
	}

	public void runBurstyGroupCreationTest(
			Collection<ActionsDataSet<Id, Collaborator, Action, ActionThread>> datasets)
			throws IOException {
		ActionBasedBurstyGroupCreationTestbed<Id, Collaborator, Action, ActionThread> testbed = new ActionBasedBurstyGroupCreationTestbed<>(
				datasets, getGraphConstants(), collaboratorClass, actionClass);

		testbed.runTestbed();
	}

	public void runGroupEvolutionTest(
			Collection<ActionsDataSet<Id, Collaborator, Action, ActionThread>> datasets)
			throws IOException {
		ActionBasedEvolutionTestbed<Id, Collaborator, Action, ActionThread> testbed =
				new ActionBasedEvolutionTestbed<>(
						datasets, getGraphConstants(), collaboratorClass, actionClass);

		testbed.runTestbed();
	}
}
