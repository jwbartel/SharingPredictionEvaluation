package testbed;

import general.actionbased.messages.SingleMessage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.TreeSet;

import metrics.recipients.RecipientAddressingEvents;
import recipients.RecipientRecommender;
import recipients.ScoredRecipientRecommendation;

public abstract class RecipientRecommendationTestBed {

	protected static class ReplayedMessage<V> implements SingleMessage<V> {

		Collection<V> creators;
		Date startDate;
		Date lastActiveDate;
		Collection<V> collaborators;

		public ReplayedMessage(SingleMessage<V> message) {
			this.creators = message.getCreators();
			this.startDate = message.getStartDate();
			this.lastActiveDate = message.getLastActiveDate();
			this.collaborators = new ArrayList<V>();

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

		public void addCollaborator(V collaborator) {
			this.collaborators.add(collaborator);
		}
	}
	
	protected static <V extends Comparable<V>, MessageType extends SingleMessage<V>>
			ReplayedMessage<V> createReplayMessage(MessageType message) {
		return new ReplayedMessage<>(message);
	}

	protected static <V extends Comparable<V>, MessageType extends SingleMessage<V>>
			ArrayList<RecipientAddressingEvents> modelSelection(MessageType message,
					RecipientRecommender<V> recommender, int listSize) {

		ArrayList<RecipientAddressingEvents> events = new ArrayList<>();

		Collection<V> seed = new TreeSet<>();
		ArrayList<V> collaborators = new ArrayList<>(message.getCollaborators());
		while (seed.size() < 2 && collaborators.size() > 0) {
			V seedMember = collaborators.get(0);
			seed.add(seedMember);
			while (collaborators.remove(seedMember)) {}
		}

		ReplayedMessage<V> replayMessage = createReplayMessage(message);

		if (seed.size() < 2) {
			events.add(RecipientAddressingEvents.SeedTooSmallForListGeneration);
		} else {
			for (V seedMember : seed) {
				replayMessage.addCollaborator(seedMember);
			}
			RecipientAddressingEvents lastActiveUserEvent = null;
			while (collaborators.size() > 0) {
				Collection<ScoredRecipientRecommendation<V>> recommendations = recommender
						.recommendRecipients(replayMessage, listSize);

				if (recommendations.size() > 0) {
					events.add(RecipientAddressingEvents.Scan);
				} else {
					events.add(RecipientAddressingEvents.EmptyListGenerated);
				}

				boolean recommendationSelected = false;
				for (ScoredRecipientRecommendation<V> recommendation : recommendations) {
					if (collaborators.contains(recommendation.getRecipient())) {
						
						events.add(RecipientAddressingEvents.ListWithCorrectEntriesGenerated);
						
						// Select the recipient and add it to the replay
						while (collaborators.remove(recommendation
								.getRecipient())) {}
						replayMessage.addCollaborator(recommendation.getRecipient());
						events.add(RecipientAddressingEvents.SelectSingleRecipient);
						
						// Determine if the use switched from clicking to typing
						if (lastActiveUserEvent == RecipientAddressingEvents.TypeSingleRecipient) {
							events.add(RecipientAddressingEvents.SwitchBetweenClickAndType);
						}
						lastActiveUserEvent = RecipientAddressingEvents.SelectSingleRecipient;
					}
					recommendationSelected = true;
				}
				if (recommendationSelected) {
					continue;
				}
				
				if(recommendations.size() > 0) {
					events.add(RecipientAddressingEvents.ListWithNoCorrectEntriesGenerated);
				}
				V manuallyEnteredIndividual = collaborators.get(0);
				while (collaborators.remove(manuallyEnteredIndividual)) {}
				replayMessage.addCollaborator(manuallyEnteredIndividual);
				events.add(RecipientAddressingEvents.TypeSingleRecipient);

			}
		}

		events.add(RecipientAddressingEvents.AddressingCompleted);
		return events;

	}
}
