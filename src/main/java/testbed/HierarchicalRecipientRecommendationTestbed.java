package testbed;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import metrics.Metric;
import metrics.MetricResult;
import metrics.MetricResultCollection;
import metrics.recipients.PrecisionMetric;
import metrics.recipients.RecallMetric;
import metrics.recipients.RecipientMetric;
import metrics.recipients.RecipientMetricFactory;
import metrics.recipients.RecipientsToAddressPerMessageMetric;
import metrics.recipients.RecommendableMessagesMetric;
import metrics.recipients.RelativeClicksMetric;
import metrics.recipients.RelativeManualEntriesMetric;
import metrics.recipients.RelativeScansMetric;
import metrics.recipients.RelativeSwitchesMetric;
import metrics.recipients.RequestsForListsMetric;
import metrics.recipients.TestWithMultipleFromMetric;
import metrics.recipients.TotalRecipientsToAddressMetric;
import metrics.recipients.TotalSelectedPerClickMetric;
import metrics.recipients.TotalTestMessagesMetric;
import metrics.recipients.TotalTrainMessagesMetric;
import metrics.recipients.TrainWithMultipleFromMetric;
import model.recommendation.recipients.HierarchicalRecipientRecommendationAcceptanceModeler;
import recommendation.recipients.groupbased.GroupBasedRecipientRecommender;
import recommendation.recipients.groupbased.GroupBasedRecipientRecommenderFactory;
import recommendation.recipients.groupbased.GroupScorer;
import recommendation.recipients.groupbased.GroupScorer.GroupScorerFactory;
import recommendation.recipients.groupbased.hierarchical.HierarchicalRecipientRecommender;
import recommendation.recipients.groupbased.interactionrank.InteractionRankGroupBasedRecipientRecommenderFactory;
import recommendation.recipients.groupbased.interactionrank.scoring.IntersectionGroupCount;
import recommendation.recipients.groupbased.interactionrank.scoring.IntersectionGroupScore;
import recommendation.recipients.groupbased.interactionrank.scoring.IntersectionWeightedScore;
import recommendation.recipients.groupbased.interactionrank.scoring.SubsetGroupCount;
import recommendation.recipients.groupbased.interactionrank.scoring.SubsetGroupScore;
import recommendation.recipients.groupbased.interactionrank.scoring.SubsetWeightedScore;
import recommendation.recipients.groupbased.interactionrank.scoring.TopContactScore;
import testbed.dataset.actions.messages.MessageDataset;
import data.representation.actionbased.messages.MessageThread;
import data.representation.actionbased.messages.SingleMessage;

public class HierarchicalRecipientRecommendationTestbed <Id, Collaborator extends Comparable<Collaborator>, Message extends SingleMessage<Collaborator>, MsgThread extends MessageThread<Collaborator, Message>> {

	static double percentTraining = 0.8;
	static int listSize = 4;
	static int minTestableMessages = 5;
	
	Collection<MessageDataset<Id, Collaborator, Message, MsgThread>> datasets = new ArrayList<>();
	Collection<GroupBasedRecipientRecommenderFactory<Collaborator, Message>> recommenderFactories = new ArrayList<>();
	Collection<GroupScorerFactory<Collaborator>> groupScorerFactories = new ArrayList<>();
	Collection<Double> wOuts = new ArrayList<>();
	Collection<Double> halfLives = new ArrayList<>();

	Collection<RecipientMetricFactory<Collaborator, Message>> metricFactories = new ArrayList<>();
	
	public HierarchicalRecipientRecommendationTestbed(
			Collection<MessageDataset<Id, Collaborator, Message, MsgThread>> datasets,
			Class<Collaborator> collaboratorClass, Class<Message> messageClass) {

		this.datasets = datasets;
		init(collaboratorClass, messageClass);
	}
	
	private void init(Class<Collaborator> collaboratorClass, Class<Message> messageClass) {
		recommenderFactories.add(new InteractionRankGroupBasedRecipientRecommenderFactory<Collaborator, Message>());

		// Add GroupScorerFactories
		groupScorerFactories.add(IntersectionGroupCount.factory(collaboratorClass));
		groupScorerFactories.add(IntersectionGroupScore.factory(collaboratorClass));
		groupScorerFactories.add(IntersectionWeightedScore.factory(collaboratorClass));
		groupScorerFactories.add(SubsetGroupCount.factory(collaboratorClass));
		groupScorerFactories.add(SubsetGroupScore.factory(collaboratorClass));
		groupScorerFactories.add(SubsetWeightedScore.factory(collaboratorClass));
		groupScorerFactories.add(TopContactScore.factory(collaboratorClass));

		// Add w_outs
		wOuts.add(0.25);
		wOuts.add(0.5);
		wOuts.add(1.0);
		wOuts.add(2.0);
		wOuts.add(4.0);

		// Add half lives
		halfLives.add(1000.0*60); // 1 minute
		halfLives.add(1000.0*60*60); // 1 hour
		halfLives.add(1000.0*60*60*24); // 1 day
		halfLives.add(1000.0*60*60*24*7); // 1 week
		halfLives.add(1000.0*60*60*24*7*4); // 4 weeks
		halfLives.add(1000.0*60*60*24*365/2); // 6 months
		halfLives.add(1000.0*60*60*24*365); // 1 year
		halfLives.add(1000.0*60*60*24*365*2); // 2 years

		// Add metric factories
		metricFactories.add(TotalTrainMessagesMetric.factory(collaboratorClass, messageClass));
		metricFactories.add(TotalTestMessagesMetric.factory(collaboratorClass, messageClass));
		metricFactories.add(RecommendableMessagesMetric.factory(collaboratorClass, messageClass));
		metricFactories.add(TotalRecipientsToAddressMetric.factory(collaboratorClass, messageClass));
		metricFactories.add(RecipientsToAddressPerMessageMetric.factory(collaboratorClass, messageClass));
		metricFactories.add(RequestsForListsMetric.factory(collaboratorClass, messageClass));
		metricFactories.add(PrecisionMetric.factory(collaboratorClass, messageClass));
		metricFactories.add(RecallMetric.factory(collaboratorClass, messageClass));
		metricFactories.add(TotalSelectedPerClickMetric.factory(collaboratorClass, messageClass));
		metricFactories.add(RelativeScansMetric.factory(collaboratorClass, messageClass));
		metricFactories.add(RelativeClicksMetric.factory(collaboratorClass, messageClass));
		metricFactories.add(RelativeManualEntriesMetric.factory(collaboratorClass, messageClass));
		metricFactories.add(RelativeSwitchesMetric.factory(collaboratorClass, messageClass));
		metricFactories.add(TrainWithMultipleFromMetric.factory(collaboratorClass, messageClass));
		metricFactories.add(TestWithMultipleFromMetric.factory(collaboratorClass, messageClass));
	}
	
	private static String getHalfLifeName(double halfLife) {
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
	
	private boolean underTestableThreshold(Collection<Message> testMessages) {
		if (testMessages.size() < minTestableMessages) {
			return false;
		}
		
		int testableMessages = 0;
		for (Message message : testMessages) {
			if (message.wasSent() && message.getCollaborators().size() > 2) {
				testableMessages++;
				if (testableMessages >= minTestableMessages) {
					return false;
				}
			}
		}
		return testableMessages < minTestableMessages;
	}
	
	public void runTestbed() throws IOException {

		for (MessageDataset<Id, Collaborator, Message, MsgThread> dataset : datasets) {

			Collection<Metric> unusedMetrics = new ArrayList<>();
			for (RecipientMetricFactory<Collaborator, Message> metricFactory : metricFactories) {
				unusedMetrics.add(metricFactory.create());
			}
			String headerPrefix = "recommendationType,group scorer,w_out,half_life,account";
			MetricResultCollection<Id> resultCollection = new MetricResultCollection<Id>(
					headerPrefix, unusedMetrics,
					dataset.getHierarchicalRecipientRecommendationMetricsFile());

			for (Id account : dataset.getAccountIds()) {
				System.out.println(account);

				Collection<Message> trainingMessages = dataset
						.getTrainMessages(account, percentTraining);
				Collection<Message> testMessages = dataset
						.getTestMessages(account, percentTraining);
				
				if (underTestableThreshold(testMessages)) {
					continue;
				}
				
				for (GroupScorerFactory<Collaborator> scorerFactory : groupScorerFactories) {
					
					for (Double wOut : wOuts) {
						
						for (Double halfLife : halfLives) {
							
							GroupScorer<Collaborator> groupScorer = scorerFactory
									.create(wOut, halfLife);

							for (GroupBasedRecipientRecommenderFactory<Collaborator, Message> recommenderFactory : recommenderFactories) {
								GroupBasedRecipientRecommender<Collaborator, Message> recommender = recommenderFactory
										.createRecommender(groupScorer);
								HierarchicalRecipientRecommender<Collaborator, Message> hierarchicalRecommender = new HierarchicalRecipientRecommender<>(recommender);

								Collection<RecipientMetric<Collaborator, Message>> metrics = new ArrayList<>();
								for (RecipientMetricFactory<Collaborator, Message> metricFactory : metricFactories) {
									metrics.add(metricFactory.create());
								}

								HierarchicalRecipientRecommendationAcceptanceModeler<Collaborator, Message> modeler = new HierarchicalRecipientRecommendationAcceptanceModeler<>(
										listSize, hierarchicalRecommender, trainingMessages,
										testMessages, metrics);
								Collection<MetricResult> results = modeler
										.modelRecommendationAcceptance();

								String label = recommender.getTypeOfRecommender();
								label += ","+groupScorer.getName();
								label += ","+wOut;
								label += ","+getHalfLifeName(halfLife);
								
								resultCollection.addResults(label, account,
										results);
							}
						}
					}
				}
			}
		}

	}
}
