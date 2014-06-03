package testbed.summarize;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import testbed.dataset.group.GroupDataSet;
import testbed.summarize.SortableColumn.Order;

public class GroupDatasetSummarizer<AccountType> extends DatasetSummarizer<AccountType> {

	GroupDataSet<AccountType> dataset;

	private GroupDatasetSummarizer(GroupDataSet<AccountType> dataset) {
		this.dataset = dataset;
	}

	public static <AccountType> GroupDatasetSummarizer<AccountType> create(
			GroupDataSet<AccountType> dataset) {
		return new GroupDatasetSummarizer<>(dataset);
	}

	@Override
	public void summarize() throws IOException {
		if (dataset.getSeedlessMetricsFile().exists()) {
			summarizeMetricResults(dataset.getSeedlessMetricsFile(), dataset.getAccountIds());
		}
		if (dataset.getEvolutionMetricsFile().exists()) {
			summarizeMetricResults(dataset.getEvolutionMetricsFile(), dataset.getAccountIds());
		}
	}

	@Override
	public GroupedRowSummarizer getGroupRowSummarizer(File resultsFile) {
		return new GroupedRowSummarizer(resultsFile, "account", "account");
	}

	private GroupedRowSummarizer getCrossTestSummarizer(File resultsFile) {
		return new GroupedRowSummarizer(resultsFile, "test", "account");
	}
	
	@Override
	public void summarizeMetricResults(File resultsFile, AccountType[] accounts) throws IOException {
		if(resultsFile.getName().equals(dataset.getEvolutionMetricsFile().getName())) {
			File crossTestSummarizedFile = new File(resultsFile.getParent(), "summarized by test - " + resultsFile.getName());
			getCrossTestSummarizer(resultsFile).summarize(crossTestSummarizedFile);
			
			File summarizedFile = new File(crossTestSummarizedFile.getParent(), "summarized - "+crossTestSummarizedFile.getName());
			new GroupedRowSummarizer(crossTestSummarizedFile, "account", "account", 1).summarize(summarizedFile);
		} else {
			super.summarizeMetricResults(resultsFile, accounts);
		}
	}
	
	@Override
	public BestColumnsSummarizer getBestColumnsSummarizer(File resultsFile) {
		if (resultsFile.getName().endsWith(dataset.getSeedlessMetricsFile().getName())) {
			Collection<SortableColumn> columnsToRankBy = new ArrayList<>();
			columnsToRankBy.add(new SortableColumn("reqired adds", Order.Ascending));
			columnsToRankBy.add(new SortableColumn("required deletes", Order.Ascending));
			if (resultsFile.getName().startsWith("summarized - ")) {
				return new BestColumnsSummarizer(resultsFile, "type", columnsToRankBy, 1);
			} else {
				return new BestColumnsSummarizer(resultsFile, "type", columnsToRankBy);
			}
		}
		return null;
	}
}
