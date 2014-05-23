package model.recommendation.recipients;

import java.util.ArrayList;
import java.util.Collection;

import metrics.MetricResult;
import metrics.recipients.RecipientAddressingEvent;
import metrics.recipients.RecipientMetric;
import recommendation.recipients.groupbased.hierarchical.HierarchicalRecipientRecommender;
import data.representation.actionbased.messages.SingleMessage;

public class NewsgroupHierarchicalRecipientRecommendationAcceptanceModeler<RecipientType extends Comparable<RecipientType>, MessageType extends SingleMessage<RecipientType>>
		extends
		HierarchicalRecipientRecommendationAcceptanceModeler<RecipientType, MessageType> {
	
	public NewsgroupHierarchicalRecipientRecommendationAcceptanceModeler(int listSize,
			HierarchicalRecipientRecommender<RecipientType> recommender,
			Collection<MessageType> trainingMessages,
			Collection<MessageType> testMessages,
			Collection<RecipientMetric<RecipientType, MessageType>> metrics) {
		super(listSize, recommender, trainingMessages, testMessages, metrics);
	}
	
	@Override
	public Collection<MetricResult> modelRecommendationAcceptance() {
		for (MessageType trainingMessage : trainingMessages) {
			recommender.addPastAction(trainingMessage);
		}

		for (MessageType testMessage : testMessages) {
			Collection<RecipientAddressingEvent> events = modelSelection(
					testMessage, recommender, listSize);
			for (RecipientMetric<RecipientType, MessageType> metric : metrics) {
				metric.addMessageResult(testMessage, events, seedSize);
			}
			recommender.addPastAction(testMessage);
		}

		Collection<MetricResult> results = new ArrayList<>();
		for (RecipientMetric<RecipientType, MessageType> metric : metrics) {
			MetricResult result = metric.evaluate(trainingMessages,
					testMessages);
			results.add(result);
		}
		
		return results;
	}
}
