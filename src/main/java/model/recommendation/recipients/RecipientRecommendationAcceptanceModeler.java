package model.recommendation.recipients;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.TreeSet;

import metrics.MetricResult;
import metrics.recipients.RecipientAddressingEvent;
import recommendation.recipients.RecipientRecommendation;
import recommendation.recipients.RecipientRecommender;
import recommendation.recipients.SingleRecipientRecommendation;
import data.representation.actionbased.CollaborativeAction;
import data.representation.actionbased.messages.SingleMessage;

public abstract class RecipientRecommendationAcceptanceModeler<RecipientType extends Comparable<RecipientType>, MessageType extends SingleMessage<RecipientType>> {

	protected int seedSize = 2;
	
	public abstract Collection<MetricResult> modelRecommendationAcceptance();

	protected static class ReplayedMessage<V> implements SingleMessage<V> {

		Collection<V> creators;
		Date startDate;
		Date lastActiveDate;
		Collection<V> collaborators;
		boolean wasSent;
		String title;

		public ReplayedMessage(SingleMessage<V> message) {
			this.creators = message.getCreators();
			this.startDate = message.getStartDate();
			this.lastActiveDate = message.getLastActiveDate();
			this.collaborators = new ArrayList<V>();
			this.wasSent = message.wasSent();
			this.title = message.getTitle();
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
		
		@Override
		public String getTitle() {
			return title;
		}

		public void addCollaborator(V collaborator) {
			this.collaborators.add(collaborator);
		}

		@Override
		public int compareTo(CollaborativeAction<V> action) {
			if (!getStartDate().equals(action.getStartDate())) {
				return getStartDate().compareTo(action.getStartDate());
			}
			return toString().compareTo(action.toString());
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

	protected Collection<RecipientType> determineSeed(
			ArrayList<RecipientType> collaborators) {
		Collection<RecipientType> seed = new TreeSet<>();
		while (seed.size() < seedSize && collaborators.size() > 0) {
			RecipientType seedMember = collaborators.get(0);
			seed.add(seedMember);
			while (collaborators.remove(seedMember)) {
			}
		}
		return seed;
	}
	
	private void addEventsBasedOnRecommendationListSize(
			Collection<RecipientRecommendation<RecipientType>> recommendations,
			ArrayList<RecipientAddressingEvent> events) {
		
		if (recommendations.size() > 0) {
			events.add(RecipientAddressingEvent.Scan);
		} else {
			events.add(RecipientAddressingEvent.EmptyListGenerated);
		}
	}
	
	protected RecipientAddressingEvent processSingleRecipientRecommendation(
			SingleRecipientRecommendation<RecipientType> recommendation,
			ReplayedMessage<RecipientType> replayMessage,
			ArrayList<RecipientType> remainingCollaborators,
			RecipientAddressingEvent lastActiveUserEvent,
			ArrayList<RecipientAddressingEvent> events) {

		RecipientType recommendedRecipient = recommendation.getRecipient();
		if (remainingCollaborators.contains(recommendedRecipient)) {

			events.add(RecipientAddressingEvent.ListWithCorrectEntriesGenerated);

			// Select the recipient and add it to the replay
			while (remainingCollaborators.remove(recommendedRecipient)) {
			}
			replayMessage.addCollaborator(recommendedRecipient);
			events.add(RecipientAddressingEvent.SelectSingleRecipient);

			// Determine if the use switched from clicking to typing
			if (lastActiveUserEvent == RecipientAddressingEvent.TypeSingleRecipient
					|| lastActiveUserEvent == null) {
				events.add(RecipientAddressingEvent.SwitchBetweenClickAndType);
			}
			return RecipientAddressingEvent.SelectSingleRecipient;
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
		for (RecipientRecommendation<RecipientType> recommendation : recommendations) {
			if (recommendation instanceof SingleRecipientRecommendation) {
				
				RecipientAddressingEvent newLastActiveUserEvent = processSingleRecipientRecommendation(
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
	
	private RecipientAddressingEvent modelManualEntry(
			ReplayedMessage<RecipientType> replayMessage,
			ArrayList<RecipientType> remainingCollaborators,
			RecipientAddressingEvent lastActiveUserEvent,
			ArrayList<RecipientAddressingEvent> events) {

		RecipientType manuallyEnteredIndividual = remainingCollaborators.get(0);
		while (remainingCollaborators.remove(manuallyEnteredIndividual)) {
		}
		replayMessage.addCollaborator(manuallyEnteredIndividual);
		events.add(RecipientAddressingEvent.TypeSingleRecipient);
		if (lastActiveUserEvent == RecipientAddressingEvent.SelectSingleRecipient
				|| lastActiveUserEvent instanceof RecipientAddressingEvent.SelectMultipleRecipientsEvent) {
			events.add(RecipientAddressingEvent.SwitchBetweenClickAndType);
		}
		return RecipientAddressingEvent.TypeSingleRecipient;
	}

	protected ArrayList<RecipientAddressingEvent> modelSelection(
			MessageType message,
			RecipientRecommender<RecipientType, MessageType> recommender, int listSize) {

		ArrayList<RecipientAddressingEvent> events = new ArrayList<>();

		ArrayList<RecipientType> remainingCollaborators = new ArrayList<>(
				message.getCollaborators());
		Collection<RecipientType> seed = determineSeed(remainingCollaborators);

		if (seed.size() < seedSize || remainingCollaborators.size() == 0) {
			events.add(RecipientAddressingEvent.SeedTooSmallForListGeneration);
			events.add(RecipientAddressingEvent.AddressingCompleted);
			return events;
		}

		ReplayedMessage<RecipientType> replayMessage = createReplayMessage(
				message, seed);
		RecipientAddressingEvent lastActiveUserEvent = null;
		while (remainingCollaborators.size() > 0) {
			
			Collection<RecipientRecommendation<RecipientType>> recommendations = recommender
					.recommendRecipients(replayMessage, listSize);

			addEventsBasedOnRecommendationListSize(recommendations, events);

			RecipientAddressingEvent newLastActiveUserEvent = modelSelectionFromNonEmptyRecommendationList(
					recommendations, replayMessage, remainingCollaborators,
					lastActiveUserEvent, events);
			
			if (newLastActiveUserEvent != null) {
				// If we selected one of the recommendations
				lastActiveUserEvent = newLastActiveUserEvent;
				seed = replayMessage.collaborators;
				continue;
			}

			if (recommendations.size() > 0) {
				events.add(RecipientAddressingEvent.ListWithNoCorrectEntriesGenerated);
			}
			lastActiveUserEvent = modelManualEntry(replayMessage, remainingCollaborators, lastActiveUserEvent, events);
			seed = replayMessage.collaborators;
		}

		events.add(RecipientAddressingEvent.AddressingCompleted);
		return events;

	}
}
