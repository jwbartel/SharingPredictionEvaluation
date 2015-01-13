package model.recommendation.recipients;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import data.representation.actionbased.messages.SingleMessage;
import metrics.MetricResult;
import metrics.permessage.PerMessageMetric;
import metrics.recipients.RecipientAddressingEvent;
import metrics.recipients.RecipientMetric;
import recommendation.recipients.RecipientRecommender;
import recommendation.recipients.groupbased.interactionrank.InteractionRankGroupBasedRecipientRecommender;

public class SingleRecipientRecommendationAcceptanceModeler<RecipientType extends Comparable<RecipientType>, MessageType extends SingleMessage<RecipientType>>
	extends RecipientRecommendationAcceptanceModeler<RecipientType,MessageType> {

	protected final int listSize;
	protected final RecipientRecommender<RecipientType, MessageType> recommender;
	protected final Collection<MessageType> trainingMessages;
	protected final Collection<MessageType> testMessages;
	protected final Collection<RecipientMetric<RecipientType, MessageType>> metrics;
	
	public SingleRecipientRecommendationAcceptanceModeler(int listSize,
			RecipientRecommender<RecipientType, MessageType> recommender,
			Collection<MessageType> trainingMessages,
			Collection<MessageType> testMessages,
			Collection<RecipientMetric<RecipientType, MessageType>> metrics,
			Collection<PerMessageMetric<RecipientType, MessageType>> perMessageMetrics,
			File outputFolder) {
		super(perMessageMetrics, outputFolder);
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
			if (testMessage.wasSent()) {
				Collection<RecipientAddressingEvent> events = modelSelection(
						testMessage, recommender, listSize);
				for (RecipientMetric<RecipientType, MessageType> metric : metrics) {
					metric.addMessageResult(testMessage, events, seedSize);
				}
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

	@Override
	protected String getGroupingType() {
		return "flat";
	}

	@Override
	protected String getPredictorType() {
		if (recommender instanceof InteractionRankGroupBasedRecipientRecommender) {
			return ((InteractionRankGroupBasedRecipientRecommender) recommender).getGroupScorer()
					.getName();
		}
		return recommender.getTypeOfRecommender(); 
	}
	
	@Override
	protected String getWeightsLabel() {
		if (recommender instanceof InteractionRankGroupBasedRecipientRecommender) {
			String halfLifeLabel = getHalfLifeName(((InteractionRankGroupBasedRecipientRecommender) recommender)
					.getGroupScorer().getHalfLife());
			double wOut = ((InteractionRankGroupBasedRecipientRecommender) recommender)
					.getGroupScorer().getWOut();
			return "half life - " + halfLifeLabel + "/ wout - " + wOut;
		}
		return null;
	}

}
