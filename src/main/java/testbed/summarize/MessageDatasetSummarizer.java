package testbed.summarize;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import testbed.dataset.actions.messages.MessageDataSet;
import testbed.summarize.SortableColumn.Order;
import data.representation.actionbased.messages.MessageThread;
import data.representation.actionbased.messages.SingleMessage;

public class MessageDatasetSummarizer<IdType, RecipientType, MessageType extends SingleMessage<RecipientType>, ThreadType extends MessageThread<RecipientType, MessageType>> extends
		DatasetSummarizer<IdType> {

	MessageDataSet<IdType, RecipientType, MessageType, ThreadType> dataset;
	
	private MessageDatasetSummarizer (MessageDataSet<IdType, RecipientType, MessageType, ThreadType> dataset) {
		this.dataset = dataset;
	}
	
	
	public static <IdType, RecipientType, MessageType extends SingleMessage<RecipientType>, ThreadType extends MessageThread<RecipientType, MessageType>>
			MessageDatasetSummarizer<IdType, RecipientType, MessageType, ThreadType>
			create(MessageDataSet<IdType, RecipientType, MessageType, ThreadType> dataset){
		return new MessageDatasetSummarizer<>(dataset);
	}
	
	@Override
	public GroupedRowSummarizer getGroupRowSummarizer(File resultsFile) {
		return new GroupedRowSummarizer(resultsFile, "account", "account");
	}

	@Override
	public BestColumnsSummarizer getBestColumnsSummarizer(File resultsFile) {
		if (resultsFile.getName().endsWith(dataset.getRecipientRecommendationMetricsFile().getName()) || 
				resultsFile.getName().endsWith(dataset.getHierarchicalRecipientRecommendationMetricsFile().getName())) {
			Collection<SortableColumn> columnsToRankBy = new ArrayList<>();
			columnsToRankBy.add(new SortableColumn("precision", Order.Descending));
			columnsToRankBy.add(new SortableColumn("recall", Order.Descending));
			if (resultsFile.getName().startsWith("summarized - ")) {
				return new BestColumnsSummarizer(resultsFile, "group scorer", columnsToRankBy, 1);
			} else {
				return new BestColumnsSummarizer(resultsFile, "group scorer", columnsToRankBy);
			}
		}
		if (resultsFile.getName().endsWith(dataset.getActionBasedSeedlessGroupsMetricsFile().getName())) {
			if (resultsFile.getName().startsWith("summarized - ")) {
				Collection<SortableColumn> columnsToRankBy = new ArrayList<>();
				columnsToRankBy.add(new SortableColumn("deletions required for test action", Order.Ascending));
				columnsToRankBy.add(new SortableColumn("additions required for test action", Order.Ascending));
				return new BestColumnsSummarizer(resultsFile, "graph builder", columnsToRankBy, 1);
			} else {
				Collection<SortableColumn> columnsToRankBy = new ArrayList<>();
				columnsToRankBy.add(new SortableColumn("avg-deletions required for test action", Order.Ascending));
				columnsToRankBy.add(new SortableColumn("avg-additions required for test action", Order.Ascending));
				return new BestColumnsSummarizer(resultsFile, "graph builder", columnsToRankBy);
			}
		}
		return null;
	}

	@Override
	public void summarize() throws IOException {
		if (dataset.getRecipientRecommendationMetricsFile().exists()) {
			summarizeMetricResults(dataset.getRecipientRecommendationMetricsFile(), dataset.getAccountIds());
		}
		if (dataset.getHierarchicalRecipientRecommendationMetricsFile().exists()) {
			summarizeMetricResults(dataset.getHierarchicalRecipientRecommendationMetricsFile(), dataset.getAccountIds());
		}
		if (dataset.getActionBasedSeedlessGroupsMetricsFile().exists()) {
			summarizeMetricResults(dataset.getActionBasedSeedlessGroupsMetricsFile(), dataset.getAccountIds());
		}
		
	}

}
