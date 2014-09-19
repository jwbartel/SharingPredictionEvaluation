package testbed.summarize;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import testbed.dataset.actions.messages.MessageDataset;
import testbed.summarize.SortableColumn.Order;
import data.representation.actionbased.messages.MessageThread;
import data.representation.actionbased.messages.SingleMessage;

public class MessageDatasetSummarizer<IdType, RecipientType, MessageType extends SingleMessage<RecipientType>, ThreadType extends MessageThread<RecipientType, MessageType>> extends
		DatasetSummarizer<IdType> {

	MessageDataset<IdType, RecipientType, MessageType, ThreadType> dataset;
	
	private MessageDatasetSummarizer (MessageDataset<IdType, RecipientType, MessageType, ThreadType> dataset) {
		this.dataset = dataset;
	}
	
	
	public static <IdType, RecipientType, MessageType extends SingleMessage<RecipientType>, ThreadType extends MessageThread<RecipientType, MessageType>>
			MessageDatasetSummarizer<IdType, RecipientType, MessageType, ThreadType>
			create(MessageDataset<IdType, RecipientType, MessageType, ThreadType> dataset){
		return new MessageDatasetSummarizer<>(dataset);
	}
	
	@Override
	public GroupedRowSummarizer getGroupRowSummarizer(File resultsFile) {
		if (resultsFile.getName().endsWith(dataset.getResponseTimeMetricsFile().getName())) {
			return new GroupedRowSummarizer(resultsFile, "fold", "account");
		}
		return new GroupedRowSummarizer(resultsFile, "account", "account");
	}
	
	public GroupedRowSummarizer getGroupRowByAccountSummarizer(File resultsFile) {
		return new GroupedRowSummarizer(resultsFile, "account", "account", 1);
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
				columnsToRankBy.add(new SortableColumn("test actions matched to recommendations", Order.Descending));
				return new BestColumnsSummarizer(resultsFile, "graph builder", columnsToRankBy, 1);
			} else {
				Collection<SortableColumn> columnsToRankBy = new ArrayList<>();
				columnsToRankBy.add(new SortableColumn("avg-deletions required for test action", Order.Ascending));
				columnsToRankBy.add(new SortableColumn("avg-additions required for test action", Order.Ascending));
				columnsToRankBy.add(new SortableColumn("test actions matched to recommendations", Order.Descending));
				return new BestColumnsSummarizer(resultsFile, "graph builder", columnsToRankBy);
			}
		}
		if (resultsFile.getName().endsWith(dataset.getResponseTimeMetricsFile().getName())) {
			Collection<SortableColumn> columnsToRankBy = new ArrayList<>();
			columnsToRankBy.add(new SortableColumn("deletions required for test action", Order.Ascending));
			columnsToRankBy.add(new SortableColumn("additions required for test action", Order.Ascending));
		}
		return null;
	}
	
	@Override
	public void summarizeMetricResults(File resultsFile, IdType[] accounts) throws IOException {
		if (resultsFile.getName().endsWith(dataset.getEvolutionMetricsFile().getName())) {

			File vertexGrowthRateFile = new File(resultsFile.getParentFile(),
					"vertex growth rate - " + resultsFile.getName());
			File summarizedVertexGrowthRateFile = new File(vertexGrowthRateFile.getParent(), "summarized - " + vertexGrowthRateFile.getName());
			getGroupRowSummarizer(vertexGrowthRateFile).summarize(summarizedVertexGrowthRateFile);
			
			File edgeGrowthRateFile = new File(resultsFile.getParentFile(),
					"edge growth rate - " + resultsFile.getName());
			File summarizedEdgeGrowthRateFile = new File(edgeGrowthRateFile.getParent(), "summarized - " + edgeGrowthRateFile.getName());
			getGroupRowSummarizer(edgeGrowthRateFile).summarize(summarizedEdgeGrowthRateFile);
			
			
		} else if (resultsFile.getName().endsWith(dataset.getResponseTimeMetricsFile().getName())) {
			if (accounts.length > 1) {
				File summarizedByFold = new File(resultsFile.getParent(), "summarized by fold - " + resultsFile.getName());
				getGroupRowSummarizer(resultsFile).summarize(summarizedByFold);
				

				File summarizedFile = new File(resultsFile.getParent(), "summarized - " + resultsFile.getName());
				getGroupRowByAccountSummarizer(summarizedByFold).summarize(summarizedFile);
				
			} else {
				File summarizedFile = new File(resultsFile.getParent(), "summarized - " + resultsFile.getName());
				getGroupRowSummarizer(resultsFile).summarize(summarizedFile);
			}
		} else {
			super.summarizeMetricResults(resultsFile, accounts);
		}
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
		if (dataset.getBurstyGroupsMetricsFile().exists()) {
			summarizeMetricResults(dataset.getBurstyGroupsMetricsFile(), dataset.getAccountIds());
		}
		if (dataset.getEvolutionMetricsFile().exists()) {
			summarizeMetricResults(dataset.getEvolutionMetricsFile(), dataset.getAccountIds());
		}
		if (dataset.getResponseTimeMetricsFile().exists()) {
			summarizeMetricResults(dataset.getResponseTimeMetricsFile(), dataset.getAccountIds());
		}
		
	}

}
