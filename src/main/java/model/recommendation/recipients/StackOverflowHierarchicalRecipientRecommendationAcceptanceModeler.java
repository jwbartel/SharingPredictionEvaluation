package model.recommendation.recipients;

import java.util.Collection;

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
			Collection<RecipientMetric<RecipientType, MessageType>> metrics) {
		super(listSize, recommender, trainingMessages, testMessages, metrics);
	}
}
