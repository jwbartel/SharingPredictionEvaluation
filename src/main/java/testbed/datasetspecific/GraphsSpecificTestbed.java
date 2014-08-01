package testbed.datasetspecific;

import java.io.IOException;
import java.util.Collection;

import testbed.GroupEvolutionTestbed;
import testbed.SeedlessGroupRecommendationTestbed;
import testbed.dataset.group.GroupDataSet;

public abstract class GraphsSpecificTestbed<Id, Collaborator extends Comparable<Collaborator>>
		implements DatasetSpecificTestbed {

	public abstract Collection<GroupDataSet<Id, Collaborator>> getGraphsDatasets();

	@Override
	public void runTestbed() throws Exception {
		Collection<GroupDataSet<Id, Collaborator>> datasets = getGraphsDatasets();
		runGraphTests(datasets);
	}

	public void runGraphTests(Collection<GroupDataSet<Id, Collaborator>> datasets)
			throws IOException {
		runGroupEvolutionTest(getGraphsDatasets());
	}

	public void runSeedlessGroupRecommendationTest(
			Collection<GroupDataSet<Id, Collaborator>> datasets) throws IOException {
		SeedlessGroupRecommendationTestbed<Id, Collaborator> testbed = new SeedlessGroupRecommendationTestbed<>(
				datasets);
		testbed.runTestbed();
	}

	public void runGroupEvolutionTest(
			Collection<GroupDataSet<Id, Collaborator>> datasets) throws IOException {
		GroupEvolutionTestbed<Id, Collaborator> testbed = new GroupEvolutionTestbed<>(
				datasets);
		testbed.runTestbed();
	}

}
