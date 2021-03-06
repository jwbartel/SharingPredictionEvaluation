package testbed.datasetspecific;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import testbed.dataset.group.GroupDataSet;
import testbed.dataset.group.SnapGroupDataSet;

public class FacebookSnapTestbed extends GraphsSpecificTestbed<Integer, Integer> {

	@Override
	public Collection<GroupDataSet<Integer, Integer>> getGraphsDatasets() {
		Collection<GroupDataSet<Integer, Integer>> datasets = new ArrayList<>();
		Integer[] snapAccounts = { 0, 348, 414, 686, 698, 1684, 3437, 3980 };
		datasets.add(new SnapGroupDataSet("snap_facebook", snapAccounts,
				new File("data/Stanford_snap/facebook")));
		return datasets;
	}

	public static void main(String[] args) throws Exception {
//		FacebookSnapTestbed testbed = new FacebookSnapTestbed();
//		testbed.runTestbed();
		
		SnapGroupDataSet dataset = new SnapGroupDataSet("snap_facebook", SnapGroupDataSet.DEFAULT_ACCOUNT_SET,
				new File("data/Stanford_snap/facebook"));
		dataset.printStats();
	}
}
