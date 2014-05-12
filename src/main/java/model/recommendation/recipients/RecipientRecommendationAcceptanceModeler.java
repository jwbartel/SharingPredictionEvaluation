package model.recommendation.recipients;

import general.actionbased.messages.SingleMessage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.TreeSet;

import metrics.MetricResult;
import metrics.recipients.RecipientAddressingEvents;
import recipients.RecipientRecommendation;
import recipients.RecipientRecommender;
import recipients.SingleRecipientRecommendation;
import recipients.groupbased.hierarchical.HierarchicalRecommendation;

public abstract class RecipientRecommendationAcceptanceModeler<RecipientType extends Comparable<RecipientType>, MessageType extends SingleMessage<RecipientType>> {

	public abstract Collection<MetricResult> modelRecommendationAcceptance();

	protected static class ReplayedMessage<V> implements SingleMessage<V> {

		Collection<V> creators;
		Date startDate;
		Date lastActiveDate;
		Collection<V> collaborators;
		boolean wasSent;

		public ReplayedMessage(SingleMessage<V> message) {
			this.creators = message.getCreators();
			this.startDate = message.getStartDate();
			this.lastActiveDate = message.getLastActiveDate();
			this.collaborators = new ArrayList<V>();
			this.wasSent = message.wasSent();

		}

		@Override
		public Collection<V> getCreators() {
			return creators;
		}

		@Override
		public Date getStartDate() {
			return startDate;
		}

		@Override
		public Date getLastActiveDate() {
			return lastActiveDate;
		}

		@Override
		public Collection<V> getCollaborators() {
			return collaborators;
		}

		@Override
		public boolean wasSent() {
			return wasSent;
		}

		public void addCollaborator(V collaborator) {
			this.collaborators.add(collaborator);
		}
	}

	protected ReplayedMessage<RecipientType> createReplayMessage(
			MessageType message, Collection<RecipientType> seed) {
		ReplayedMessage<RecipientType> replayMessage = new ReplayedMessage<>(
				message);
		for (RecipientType seedMember : seed) {
			replayMessage.addCollaborator(seedMember);
		}
		return replayMessage;
	}

	private Collection<RecipientType> determineSeed(
			ArrayList<RecipientType> collaborators) {
		Collection<RecipientType> seed = new TreeSet<>();
		while (seed.size() < 2 && collaborators.size() > 0) {
			RecipientType seedMember = collaborators.get(0);
			seed.add(seedMember);
			while (collaborators.remove(seedMember)) {
			}
		}
		return seed;
	}
	
	private void addEventsBasedOnRecommendationListSize(
			Collection<RecipientRecommendation<RecipientType>> recommendations,
			ArrayList<RecipientAddressingEvents> events) {
		
		if (recommendations.size() > 0) {
			events.add(RecipientAddressingEvents.Scan);
		} else {
			events.add(RecipientAddressingEvents.EmptyListGenerated);
		}
	}
	
	private RecipientAddressingEvents processSingleRecipientRecommendation(
			SingleRecipientRecommendation<RecipientType> recommendation,
			ReplayedMessage<RecipientType> replayMessage,
			ArrayList<RecipientType> remainingCollaborators,
			RecipientAddressingEvents lastActiveUserEvent,
			ArrayList<RecipientAddressingEvents> events) {

		RecipientType recommendedRecipient = recommendation.getRecipient();
		if (remainingCollaborators.contains(recommendedRecipient)) {

			events.add(RecipientAddressingEvents.ListWithCorrectEntriesGenerated);

			// Select the recipient and add it to the replay
			while (remainingCollaborators.remove(recommendedRecipient)) {
			}
			replayMessage.addCollaborator(recommendedRecipient);
			events.add(RecipientAddressingEvents.SelectSingleRecipient);

			// Determine if the use switched from clicking to typing
			if (lastActiveUserEvent == RecipientAddressingEvents.TypeSingleRecipient
					|| lastActiveUserEvent == null) {
				events.add(RecipientAddressingEvents.SwitchBetweenClickAndType);
			}
			return RecipientAddressingEvents.SelectSingleRecipient;
		}
		return null;
	}
	
	private RecipientAddressingEvents modelSelectionFromNonEmptyRecommendationList(
			Collection<RecipientRecommendation<RecipientType>> recommendations,
			ReplayedMessage<RecipientType> replayMessage,
			ArrayList<RecipientType> remainingCollaborators,
			RecipientAddressingEvents lastActiveUserEvent,
			ArrayList<RecipientAddressingEvents> events) {
		
		RecipientAddressingEvents retVal = null;
		for (RecipientRecommendation<RecipientType> recommendation : recommendations) {
			if (recommendation instanceof SingleRecipientRecommendation) {
				
				RecipientAddressingEvents newLastActiveUserEvent = processSingleRecipientRecommendation(
						(SingleRecipientRecommendation<RecipientType>) recommendation,
						replayMessage, remainingCollaborators,
						lastActiveUserEvent, events);
				if (newLastActiveUserEvent != null) {
					retVal = newLastActiveUserEvent;
					break;
				}
			}
		}
		return retVal;
		
	}
	
	private RecipientAddressingEvents modelManualEntry(
			ReplayedMessage<RecipientType> replayMessage,
			ArrayList<RecipientType> remainingCollaborators,
			RecipientAddressingEvents lastActiveUserEvent,
			ArrayList<RecipientAddressingEvents> events) {

		RecipientType manuallyEnteredIndividual = remainingCollaborators.get(0);
		while (remainingCollaborators.remove(manuallyEnteredIndividual)) {
		}
		replayMessage.addCollaborator(manuallyEnteredIndividual);
		events.add(RecipientAddressingEvents.TypeSingleRecipient);
		if (lastActiveUserEvent == RecipientAddressingEvents.SelectSingleRecipient
				|| lastActiveUserEvent == RecipientAddressingEvents.SelectMultipleRecipients) {
			events.add(RecipientAddressingEvents.SwitchBetweenClickAndType);
		}
		return RecipientAddressingEvents.TypeSingleRecipient;
	}

	protected ArrayList<RecipientAddressingEvents> modelSelection(
			MessageType message,
			RecipientRecommender<RecipientType> recommender, int listSize) {

		ArrayList<RecipientAddressingEvents> events = new ArrayList<>();

		ArrayList<RecipientType> remainingCollaborators = new ArrayList<>(
				message.getCollaborators());
		Collection<RecipientType> seed = determineSeed(remainingCollaborators);

		if (seed.size() < 2 || remainingCollaborators.size() == 0) {
			events.add(RecipientAddressingEvents.SeedTooSmallForListGeneration);
			events.add(RecipientAddressingEvents.AddressingCompleted);
			return events;
		}

		ReplayedMessage<RecipientType> replayMessage = createReplayMessage(
				message, seed);
		RecipientAddressingEvents lastActiveUserEvent = null;
		while (remainingCollaborators.size() > 0) {
			
			Collection<RecipientRecommendation<RecipientType>> recommendations = recommender
					.recommendRecipients(replayMessage, listSize);

			addEventsBasedOnRecommendationListSize(recommendations, events);

			RecipientAddressingEvents newLastActiveUserEvent = modelSelectionFromNonEmptyRecommendationList(
					recommendations, replayMessage, remainingCollaborators,
					lastActiveUserEvent, events);
			
			if (newLastActiveUserEvent != null) {
				// If we selected one of the recommendations
				lastActiveUserEvent = newLastActiveUserEvent;
				continue;
			}

			if (recommendations.size() > 0) {
				events.add(RecipientAddressingEvents.ListWithNoCorrectEntriesGenerated);
			}
			lastActiveUserEvent = modelManualEntry(replayMessage, remainingCollaborators, newLastActiveUserEvent, events);

		}

		events.add(RecipientAddressingEvents.AddressingCompleted);
		return events;

	}
}
