package testbed.datasetspecific;

import java.io.IOException;
import java.util.Collection;

import testbed.GroupEvolutionTestbed;
import testbed.dataset.group.GroupDataSet;

public abstract class GraphsSpecificTestbed<Collaborator extends Comparable<Collaborator>>
		implements DatasetSpecificTestbed {

	public abstract Collection<GroupDataSet<Collaborator>> getGraphsDatasets();

	@Override
	public void runTestbed() throws Exception {
		Collection<GroupDataSet<Collaborator>> datasets = getGraphsDatasets();
		runGraphTests(datasets);
	}

	public void runGraphTests(Collection<GroupDataSet<Collaborator>> datasets)
			throws IOException {
		runGroupEvolutionTest(getGraphsDatasets());
	}

	public void runGroupEvolutionTest(
			Collection<GroupDataSet<Collaborator>> datasets) throws IOException {
		GroupEvolutionTestbed<Collaborator> testbed = new GroupEvolutionTestbed<>(
				datasets);
		testbed.runTestbed();
	}

}
