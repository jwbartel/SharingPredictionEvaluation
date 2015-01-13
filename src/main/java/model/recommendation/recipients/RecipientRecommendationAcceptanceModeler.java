package model.recommendation.recipients;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.TreeSet;

import metrics.MetricResult;
import metrics.permessage.PerMessageMetric;
import metrics.recipients.RecipientAddressingEvent;
import recommendation.recipients.RecipientRecommendation;
import recommendation.recipients.RecipientRecommender;
import recommendation.recipients.SingleRecipientRecommendation;
import data.representation.actionbased.CollaborativeAction;
import data.representation.actionbased.messages.SingleMessage;

public abstract class RecipientRecommendationAcceptanceModeler<RecipientType extends Comparable<RecipientType>, MessageType extends SingleMessage<RecipientType>> {

	protected int seedSize = 2;
	protected final Collection<PerMessageMetric<RecipientType, MessageType>> perMessageMetrics;
	protected final File outputFolder;
	
	public abstract Collection<MetricResult> modelRecommendationAcceptance();

	protected static class ReplayedMessage<V> implements SingleMessage<V> {

		Collection<V> creators;
		Date startDate;
		Date lastActiveDate;
		Collection<V> collaborators;
		boolean wasSent;
		String title;
		String id;

		public ReplayedMessage(SingleMessage<V> message) {
			this.creators = message.getCreators();
			this.startDate = message.getStartDate();
			this.lastActiveDate = message.getLastActiveDate();
			this.collaborators = new ArrayList<V>();
			this.wasSent = message.wasSent();
			this.title = message.getTitle();
			this.id = message.getId();
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

		@Override
		public String getId() {
			return id;
		}
	}
	
	public RecipientRecommendationAcceptanceModeler(
			Collection<PerMessageMetric<RecipientType, MessageType>> perMessageMetrics,
			File outputFolder) {
		this.perMessageMetrics = perMessageMetrics;
		this.outputFolder = outputFolder;
		
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
		
		try {
			printPerMessageMetrics(message, events, seedSize);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return events;

	}
	
	boolean outputFileWrittenBefore = false;
	int messageCount = 1;
	private void printPerMessageMetrics(MessageType message,
			Collection<RecipientAddressingEvent> events, int seedSize) throws IOException{
		File outputFile = getPerMessageOutputFile(); 
		BufferedWriter out;
		if (!outputFileWrittenBefore) {
			if (!outputFile.exists()) {
				outputFile.getParentFile().mkdirs();
			}
			out = new BufferedWriter(new FileWriter(outputFile, false));
			writeHeader(out);
			outputFileWrittenBefore = true;
		} else {
			out = new BufferedWriter(new FileWriter(outputFile, true));
		}

		out.write(message.getId().replaceAll(",","-"));
		for(PerMessageMetric<RecipientType, MessageType> metric : perMessageMetrics) {
			MetricResult result = metric.evaluate(message, events, seedSize);
			out.write("," + result.toString());
		}
		out.newLine();
		out.flush();
		out.close();
	}
	
	private File getPerMessageOutputFile() {
		String suffix = ".csv";
		
		File outputFile = new File(outputFolder, getGroupingType());
		outputFile = new File(outputFile, getPredictorType());
		String weightsLabel = getWeightsLabel();
		if (weightsLabel != null) {
			outputFile = new File(outputFile, getWeightsLabel() + suffix);
		} else {
			outputFile = new File(outputFile.getAbsolutePath() + suffix);
		}
		
		return outputFile;
	}
	
	private void writeHeader(BufferedWriter out) throws IOException {
		out.write("message id");
		for (PerMessageMetric<RecipientType, MessageType> metric : perMessageMetrics) {
			out.write(","+metric.getHeader());
		}
		out.newLine();
		
	}
	
	protected abstract String getGroupingType();
	protected abstract String getPredictorType();
	protected abstract String getWeightsLabel();
	
	protected static String getHalfLifeName(double halfLife) {
		if (halfLife < 1000) {
			return halfLife + " ms";
		}
		halfLife /= 1000;
		if (halfLife < 60) {
			return halfLife + " seconds";
		}
		halfLife /= 60;
		if (halfLife < 60){
			return halfLife + " minutes";
		}
		halfLife /= 60;
		if (halfLife < 24){
			return halfLife + " hours";
		}
		halfLife /= 24;
		if (halfLife < 7) {
			return halfLife + " days";
		}
		if (halfLife <= 28) {
			return halfLife/7 + " weeks";
		}
		halfLife /= 365;
		return halfLife + " years";
	}
}
