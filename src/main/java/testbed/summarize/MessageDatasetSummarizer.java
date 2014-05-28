package testbed.summarize;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import testbed.dataset.actions.messages.MessageDataSet;
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
			Collection<String> columnsToRankBy = new ArrayList<>();
			columnsToRankBy.add("precision");
			columnsToRankBy.add("recall");
			if (resultsFile.getName().startsWith("summarized - ")) {
				return new BestColumnsSummarizer(resultsFile, "half_life", columnsToRankBy, 1);
			} else {
				return new BestColumnsSummarizer(resultsFile, "half_life", columnsToRankBy);
			}
		}
		if (resultsFile.getName().endsWith(dataset.getActionBasedSeedlessGroupsMetricsFile().getName())) {
			if (resultsFile.getName().startsWith("summarized - ")) {
				Collection<String> columnsToRankBy = new ArrayList<>();
				columnsToRankBy.add("deletions required for test action");
				columnsToRankBy.add("additions required for test action");
				return new BestColumnsSummarizer(resultsFile, "threshold", columnsToRankBy, 1);
			} else {
				Collection<String> columnsToRankBy = new ArrayList<>();
				columnsToRankBy.add("avg-deletions required for test action");
				columnsToRankBy.add("avg-additions required for test action");
				return new BestColumnsSummarizer(resultsFile, "threshold", columnsToRankBy);
			}
		}
		return null;
	}

	@Override
	public void summarize() throws IOException {
		summarizeMetricResults(dataset.getRecipientRecommendationMetricsFile(), dataset.getAccountIds());
		summarizeMetricResults(dataset.getHierarchicalRecipientRecommendationMetricsFile(), dataset.getAccountIds());
		summarizeMetricResults(dataset.getActionBasedSeedlessGroupsMetricsFile(), dataset.getAccountIds());
		
	}

}
