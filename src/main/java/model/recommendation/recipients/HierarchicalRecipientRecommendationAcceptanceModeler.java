package model.recommendation.recipients;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import data.representation.actionbased.messages.SingleMessage;
import metrics.MetricResult;
import metrics.recipients.RecipientAddressingEvent;
import metrics.recipients.RecipientMetric;
import recommendation.recipients.RecipientRecommendation;
import recommendation.recipients.RecipientRecommender;
import recommendation.recipients.SingleRecipientRecommendation;
import recommendation.recipients.groupbased.hierarchical.HierarchicalGroupRecommendation;
import recommendation.recipients.groupbased.hierarchical.HierarchicalIndividualRecommendation;
import recommendation.recipients.groupbased.hierarchical.HierarchicalRecipientRecommender;
import recommendation.recipients.groupbased.hierarchical.HierarchicalRecommendation;

public class HierarchicalRecipientRecommendationAcceptanceModeler<RecipientType extends Comparable<RecipientType>, MessageType extends SingleMessage<RecipientType>>
		extends
		RecipientRecommendationAcceptanceModeler<RecipientType, MessageType> {

	protected final int listSize;
	protected final RecipientRecommender<RecipientType> recommender;
	protected final Collection<MessageType> trainingMessages;
	protected final Collection<MessageType> testMessages;
	protected final Collection<RecipientMetric<RecipientType, MessageType>> metrics;
	
	public HierarchicalRecipientRecommendationAcceptanceModeler(int listSize,
			HierarchicalRecipientRecommender<RecipientType> recommender,
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

	private int intersectionSize(Collection<RecipientType> remainingCollaborators,
			Collection<RecipientType> recommendation) {
		Set<RecipientType> intersection = new TreeSet<>(remainingCollaborators);
		intersection.retainAll(recommendation);
		return intersection.size();
	}
	
	private HierarchicalRecommendation<RecipientType> getBestRecommendation(
			HierarchicalRecommendation<RecipientType> recommendation,
			ArrayList<RecipientType> remainingCollaborators,
			HierarchicalRecommendation<RecipientType> bestRecommendation) {
		
		if (bestRecommendation == null
				|| recommendation.getSize() > bestRecommendation.getSize()) {

			Set<RecipientType> recommendationMembers = recommendation
					.getMembers();
			
			if (intersectionSize(remainingCollaborators, recommendationMembers) > 0) {
				if (remainingCollaborators.containsAll(recommendationMembers)) {
					return recommendation;
				} else if (recommendation instanceof HierarchicalGroupRecommendation) {
					for (HierarchicalRecommendation<RecipientType> subRecommendation : ((HierarchicalGroupRecommendation<RecipientType>) recommendation)
							.getValues()) {

						bestRecommendation = getBestRecommendation(
								subRecommendation, remainingCollaborators,
								bestRecommendation);
					}

				}
			}

		}
		return bestRecommendation;
	}
	
	protected RecipientAddressingEvent processBestHierarchicalRecommendation(
			HierarchicalRecommendation<RecipientType> recommendation,
			ReplayedMessage<RecipientType> replayMessage,
			ArrayList<RecipientType> remainingCollaborators,
			RecipientAddressingEvent lastActiveUserEvent,
			ArrayList<RecipientAddressingEvent> events) {

		if (recommendation == null) {
			return null;
		}
		
		Collection<RecipientType> recommendedRecipients = recommendation
				.getMembers();
		if (recommendation.getSize() > 0
				&& remainingCollaborators.containsAll(recommendedRecipients)) {

			events.add(RecipientAddressingEvent.ListWithCorrectEntriesGenerated);
			
			while(remainingCollaborators.removeAll(recommendedRecipients)) {}
			for (RecipientType recommendedRecipient : recommendedRecipients) {
				replayMessage.addCollaborator(recommendedRecipient);
			}
			
			RecipientAddressingEvent userActiveAction;
			if (recommendedRecipients.size() == 1) {
				userActiveAction = RecipientAddressingEvent.SelectSingleRecipient;
			} else {
				userActiveAction = RecipientAddressingEvent
						.SelectMultipleRecipients(recommendedRecipients.size());
			}
			events.add(userActiveAction);
			
			// Determine if the use switched from clicking to typing
			if (lastActiveUserEvent == RecipientAddressingEvent.TypeSingleRecipient
					|| lastActiveUserEvent == null) {
				events.add(RecipientAddressingEvent.SwitchBetweenClickAndType);
			}
			return userActiveAction;
		}

		return null;
	}
	

	protected RecipientAddressingEvent modelSelectionFromNonEmptyRecommendationList(
			Collection<RecipientRecommendation<RecipientType>> recommendations,
			ReplayedMessage<RecipientType> replayMessage,
			ArrayList<RecipientType> remainingCollaborators,
			RecipientAddressingEvent lastActiveUserEvent,
			ArrayList<RecipientAddressingEvent> events) {
		
		HierarchicalRecommendation<RecipientType> bestRecommendation = null;
		for (RecipientRecommendation<RecipientType> recommendation : recommendations) {

			if (recommendation instanceof HierarchicalRecommendation) {
				bestRecommendation = getBestRecommendation(
						(HierarchicalRecommendation<RecipientType>) recommendation,
						remainingCollaborators, bestRecommendation);
			} else if (recommendation instanceof SingleRecipientRecommendation) {
				HierarchicalRecommendation<RecipientType> tempRecommendation = new HierarchicalIndividualRecommendation<RecipientType>(
						((SingleRecipientRecommendation<RecipientType>) recommendation)
								.getRecipient(), null);
				bestRecommendation = getBestRecommendation(tempRecommendation,
						remainingCollaborators, bestRecommendation);
			}
		}
		
		return processBestHierarchicalRecommendation(bestRecommendation,
				replayMessage, remainingCollaborators, lastActiveUserEvent,
				events);
		
	}
}
