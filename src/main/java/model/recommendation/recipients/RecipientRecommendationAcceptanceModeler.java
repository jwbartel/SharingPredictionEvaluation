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
	
	protected ReplayedMessage<RecipientType> createReplayMessage(MessageType message) {
		return new ReplayedMessage<>(message);
	}

	protected ArrayList<RecipientAddressingEvents> modelSelection(MessageType message,
					RecipientRecommender<RecipientType> recommender, int listSize) {

		ArrayList<RecipientAddressingEvents> events = new ArrayList<>();

		Collection<RecipientType> seed = new TreeSet<>();
		ArrayList<RecipientType> collaborators = new ArrayList<>(message.getCollaborators());
		while (seed.size() < 2 && collaborators.size() > 0) {
			RecipientType seedMember = collaborators.get(0);
			seed.add(seedMember);
			while (collaborators.remove(seedMember)) {}
		}

		ReplayedMessage<RecipientType> replayMessage = createReplayMessage(message);

		if (seed.size() < 2 || collaborators.size() == 0) {
			events.add(RecipientAddressingEvents.SeedTooSmallForListGeneration);
		} else {
			for (RecipientType seedMember : seed) {
				replayMessage.addCollaborator(seedMember);
			}
			RecipientAddressingEvents lastActiveUserEvent = null;
			while (collaborators.size() > 0) {
				Collection<RecipientRecommendation<RecipientType>> recommendations =
						recommender.recommendRecipients(replayMessage, listSize);

				if (recommendations.size() > 0) {
					events.add(RecipientAddressingEvents.Scan);
				} else {
					events.add(RecipientAddressingEvents.EmptyListGenerated);
				}

				boolean recommendationSelected = false;
				for (RecipientRecommendation<RecipientType> recommendation : recommendations) {
					if (recommendation instanceof SingleRecipientRecommendation) {
						RecipientType recommendedRecipient = 
								((SingleRecipientRecommendation<RecipientType>) recommendation).getRecipient();
						if (collaborators.contains(recommendedRecipient)) {

							events.add(RecipientAddressingEvents.ListWithCorrectEntriesGenerated);

							// Select the recipient and add it to the replay
							while (collaborators.remove(recommendedRecipient)) {}
							replayMessage.addCollaborator(recommendedRecipient);
							events.add(RecipientAddressingEvents.SelectSingleRecipient);

							// Determine if the use switched from clicking to typing
							if (lastActiveUserEvent == RecipientAddressingEvents.TypeSingleRecipient
									|| lastActiveUserEvent == null) {
								events.add(RecipientAddressingEvents.SwitchBetweenClickAndType);
							}
							lastActiveUserEvent = RecipientAddressingEvents.SelectSingleRecipient;
							recommendationSelected = true;
						}
					}
				}
				if (recommendationSelected) {
					continue;
				}
				
				if(recommendations.size() > 0) {
					events.add(RecipientAddressingEvents.ListWithNoCorrectEntriesGenerated);
				}
				RecipientType manuallyEnteredIndividual = collaborators.get(0);
				while (collaborators.remove(manuallyEnteredIndividual)) {}
				replayMessage.addCollaborator(manuallyEnteredIndividual);
				events.add(RecipientAddressingEvents.TypeSingleRecipient);
				if (lastActiveUserEvent == RecipientAddressingEvents.SelectSingleRecipient
						|| lastActiveUserEvent == RecipientAddressingEvents.SelectMultipleRecipients) {
					events.add(RecipientAddressingEvents.SwitchBetweenClickAndType);
				}
				lastActiveUserEvent = RecipientAddressingEvents.TypeSingleRecipient;

			}
		}

		events.add(RecipientAddressingEvents.AddressingCompleted);
		return events;

	}
}
