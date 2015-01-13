package model.recommendation.recipients;

import java.io.File;
import java.util.Collection;

import metrics.permessage.PerMessageMetric;
import metrics.recipients.RecipientMetric;
import recommendation.recipients.groupbased.hierarchical.HierarchicalRecipientRecommender;
import data.representation.actionbased.messages.stackoverflow.StackOverflowMessage;

public class StackOverflowHierarchicalRecipientRecommendationAcceptanceModeler<RecipientType extends Comparable<RecipientType>, MessageType extends StackOverflowMessage<RecipientType>>
		extends
		NewsgroupHierarchicalRecipientRecommendationAcceptanceModeler<RecipientType, MessageType> {
	
	public StackOverflowHierarchicalRecipientRecommendationAcceptanceModeler(int listSize,
			HierarchicalRecipientRecommender<RecipientType, MessageType> recommender,
			Collection<MessageType> trainingMessages,
			Collection<MessageType> testMessages,
			Collection<RecipientMetric<RecipientType, MessageType>> metrics,
			Collection<PerMessageMetric<RecipientType, MessageType>> perMessageMetrics,
			File outputFolder) {
		super(listSize, recommender, trainingMessages, testMessages, metrics, perMessageMetrics, outputFolder);
	}
}
