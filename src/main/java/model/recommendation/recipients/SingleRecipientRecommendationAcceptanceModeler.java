package model.recommendation.recipients;

import general.actionbased.messages.SingleMessage;

import java.util.ArrayList;
import java.util.Collection;

import metrics.MetricResult;
import metrics.recipients.RecipientAddressingEvents;
import metrics.recipients.RecipientMetric;
import recipients.RecipientRecommender;

public class SingleRecipientRecommendationAcceptanceModeler<RecipientType extends Comparable<RecipientType>, MessageType extends SingleMessage<RecipientType>>
	extends RecipientRecommendationAcceptanceModeler<RecipientType,MessageType> {

	private final int listSize;
	private final RecipientRecommender<RecipientType> recommender;
	private final Collection<MessageType> trainingMessages;
	private final Collection<MessageType> testMessages;
	private final Collection<RecipientMetric<RecipientType, MessageType>> metrics;
	
	public SingleRecipientRecommendationAcceptanceModeler(int listSize,
			RecipientRecommender<RecipientType> recommender,
			Collection<MessageType> trainingMessages,
			Collection<MessageType> testMessages,
			Collection<RecipientMetric<RecipientType, MessageType>> metrics) {
		this.listSize = listSize;
		this.recommender = recommender;
		this.trainingMessages = trainingMessages;
		this.testMessages = testMessages;
		this.metrics = metrics;
	}
	
	@Override
	public Collection<MetricResult> modelRecommendationAcceptance() {
		
		for (MessageType trainingMessage : trainingMessages) {
			recommender.addPastAction(trainingMessage);
		}

		for (MessageType testMessage : testMessages) {
			Collection<RecipientAddressingEvents> events = modelSelection(
					testMessage, recommender, listSize);
			for (RecipientMetric<RecipientType, MessageType> metric : metrics) {
				metric.addMessageResult(testMessage, events);
			}
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
