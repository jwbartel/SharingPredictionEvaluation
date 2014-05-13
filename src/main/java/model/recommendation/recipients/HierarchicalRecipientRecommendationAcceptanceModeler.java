package model.recommendation.recipients;

import general.actionbased.messages.SingleMessage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import bus.data.structures.ComparableSet;
import metrics.MetricResult;
import metrics.recipients.RecipientAddressingEvent;
import model.recommendation.recipients.RecipientRecommendationAcceptanceModeler.ReplayedMessage;
import recipients.RecipientRecommendation;
import recipients.SingleRecipientRecommendation;
import recipients.groupbased.hierarchical.HierarchicalGroupRecommendation;
import recipients.groupbased.hierarchical.HierarchicalIndividualRecommendation;
import recipients.groupbased.hierarchical.HierarchicalRecommendation;

public class HierarchicalRecipientRecommendationAcceptanceModeler<RecipientType extends Comparable<RecipientType>, MessageType extends SingleMessage<RecipientType>>
		extends
		RecipientRecommendationAcceptanceModeler<RecipientType, MessageType> {

	@Override
	public Collection<MetricResult> modelRecommendationAcceptance() {
		// TODO Auto-generated method stub
		return null;
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
		
		RecipientAddressingEvent retVal = null;
		
		HierarchicalRecommendation<RecipientType> bestRecommendation = null;
		for (RecipientRecommendation<RecipientType> recommendation : recommendations) {
			if (recommendation instanceof SingleRecipientRecommendation) {

				if (recommendation instanceof HierarchicalRecommendation) {
					bestRecommendation = getBestRecommendation(
							(HierarchicalRecommendation<RecipientType>) recommendation,
							remainingCollaborators, bestRecommendation);
				} else if (recommendation instanceof SingleRecipientRecommendation) {
					HierarchicalRecommendation<RecipientType> tempRecommendation = new HierarchicalIndividualRecommendation<RecipientType>(
							((SingleRecipientRecommendation<RecipientType>) recommendation)
									.getRecipient(), null);
					bestRecommendation = getBestRecommendation(
							tempRecommendation, remainingCollaborators,
							bestRecommendation);
				}
			}
		}
		
		processBestHierarchicalRecommendation(bestRecommendation,
				replayMessage, remainingCollaborators, lastActiveUserEvent,
				events);
		return retVal;
		
	}
}
