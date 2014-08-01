package testbed.datasetspecific;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import testbed.dataset.group.GroupDataSet;
import testbed.dataset.group.MixedInitiativeDataSet;

public class FacebookMixedInitiativeTestbed extends
		GraphsSpecificTestbed<Integer> {

	@Override
	public Collection<GroupDataSet<Integer>> getGraphsDatasets() {
		Collection<GroupDataSet<Integer>> datasets = new ArrayList<>();
		Integer[] mixedInitiativeAccounts = MixedInitiativeDataSet.DEFAULT_ACCOUNT_SET;
		datasets.add(new MixedInitiativeDataSet("mixed_initiative",
				mixedInitiativeAccounts, new File("data/kelli")));
		return datasets;
	}

	public static void main(String[] args) throws Exception {
		FacebookMixedInitiativeTestbed testbed = new FacebookMixedInitiativeTestbed();
		testbed.runTestbed();
	}

}
