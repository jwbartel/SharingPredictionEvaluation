package testbed.datasetspecific.old;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import data.representation.actionbased.messages.email.EmailMessage;
import data.representation.actionbased.messages.email.EmailThread;
import metrics.Metric;
import metrics.MetricResult;
import metrics.MetricResultCollection;
import metrics.permessage.PerMessageMetric;
import metrics.permessage.RecipientPerMessageMetric;
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
import testbed.dataset.actions.messages.email.EmailDataSet;
import testbed.dataset.actions.messages.email.EnronEmailDataSet;
import testbed.dataset.actions.messages.email.ResponseTimeStudyDataSet;

public class EmailHierarchicalRecipientRecommendationTestBed {

	static double percentTraining = 0.8;
	static int listSize = 4;
	static int minTestableMessages = 5;

	static Collection<EmailDataSet<String, String, EmailMessage<String>, EmailThread<String, EmailMessage<String>>>> dataSets = new ArrayList<>();
	static Collection<GroupBasedRecipientRecommenderFactory<String, EmailMessage<String>>> recommenderFactories = new ArrayList<>();
	static Collection<GroupScorerFactory<String>> groupScorerFactories = new ArrayList<>();
	static Collection<Double> wOuts = new ArrayList<>();
	static Collection<Double> halfLives = new ArrayList<>();

	static Collection<RecipientMetricFactory<String, EmailMessage<String>>> metricFactories = new ArrayList<>();
	static Collection<PerMessageMetric.Factory<String, EmailMessage<String>>> perMessageMetricFactories = new ArrayList<>();

	static {

		// Add data sets
		dataSets.add(new EnronEmailDataSet("enron",
				EnronEmailDataSet.DEFAULT_ACCOUNTS, new File("data/Enron")));
		dataSets.add(new ResponseTimeStudyDataSet("response time", new File(
				"data/Email Response Study")));

		// Add recommender factories
		recommenderFactories
				.add(new InteractionRankGroupBasedRecipientRecommenderFactory<String, EmailMessage<String>>());
		
		// Add GroupScorerFactories
		groupScorerFactories.add(IntersectionGroupCount.factory(String.class));
		groupScorerFactories.add(IntersectionGroupScore.factory(String.class));
		groupScorerFactories.add(IntersectionWeightedScore.factory(String.class));
		groupScorerFactories.add(SubsetGroupCount.factory(String.class));
		groupScorerFactories.add(SubsetGroupScore.factory(String.class));
		groupScorerFactories.add(SubsetWeightedScore.factory(String.class));
		groupScorerFactories.add(TopContactScore.factory(String.class));
		
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
		metricFactories.add(TotalTrainMessagesMetric.factory(String.class, EmailMessage.class));
		metricFactories.add(TotalTestMessagesMetric.factory(String.class, EmailMessage.class));
		metricFactories.add(RecommendableMessagesMetric.factory(String.class, EmailMessage.class));
		metricFactories.add(RecipientsToAddressPerMessageMetric.factory(String.class, EmailMessage.class));
		metricFactories.add(TotalRecipientsToAddressMetric.factory(String.class, EmailMessage.class));
		metricFactories.add(RequestsForListsMetric.factory(String.class, EmailMessage.class));
		metricFactories.add(PrecisionMetric.factory(String.class, EmailMessage.class));
		metricFactories.add(RecallMetric.factory(String.class, EmailMessage.class));
		metricFactories.add(TotalSelectedPerClickMetric.factory(String.class, EmailMessage.class));
		metricFactories.add(RelativeScansMetric.factory(String.class, EmailMessage.class));
		metricFactories.add(RelativeClicksMetric.factory(String.class, EmailMessage.class));
		metricFactories.add(RelativeManualEntriesMetric.factory(String.class, EmailMessage.class));
		metricFactories.add(RelativeSwitchesMetric.factory(String.class, EmailMessage.class));
		metricFactories.add(TrainWithMultipleFromMetric.factory(String.class, EmailMessage.class));
		metricFactories.add(TestWithMultipleFromMetric.factory(String.class, EmailMessage.class));
		
		perMessageMetricFactories.add(RecipientPerMessageMetric.factory(TotalRecipientsToAddressMetric.factory(String.class, EmailMessage.class)));
		perMessageMetricFactories.add(RecipientPerMessageMetric.factory(RequestsForListsMetric.factory(String.class, EmailMessage.class)));
		perMessageMetricFactories.add(RecipientPerMessageMetric.factory(PrecisionMetric.factory(String.class, EmailMessage.class)));
		perMessageMetricFactories.add(RecipientPerMessageMetric.factory(RecallMetric.factory(String.class, EmailMessage.class)));perMessageMetricFactories.add(RecipientPerMessageMetric.factory(RecallMetric.factory(String.class, EmailMessage.class)));
		perMessageMetricFactories.add(RecipientPerMessageMetric.factory(TotalSelectedPerClickMetric.factory(String.class, EmailMessage.class)));
		perMessageMetricFactories.add(RecipientPerMessageMetric.factory(RelativeScansMetric.factory(String.class, EmailMessage.class)));
		perMessageMetricFactories.add(RecipientPerMessageMetric.factory(RelativeClicksMetric.factory(String.class, EmailMessage.class)));
		perMessageMetricFactories.add(RecipientPerMessageMetric.factory(RelativeManualEntriesMetric.factory(String.class, EmailMessage.class)));
		perMessageMetricFactories.add(RecipientPerMessageMetric.factory(RelativeSwitchesMetric.factory(String.class, EmailMessage.class)));
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
	
	private static boolean underTestableThreshold(Collection<EmailMessage<String>> testMessages) {
		if (testMessages.size() < minTestableMessages) {
			return false;
		}
		
		int testableMessages = 0;
		for (EmailMessage<String> message : testMessages) {
			if (message.wasSent() && message.getCollaborators().size() > 2) {
				testableMessages++;
				if (testableMessages >= minTestableMessages) {
					return false;
				}
			}
		}
		return testableMessages < minTestableMessages;
	}

	public static void main(String[] args) throws IOException {

		for (EmailDataSet<String, String, EmailMessage<String>, EmailThread<String, EmailMessage<String>>> dataset : dataSets) {

			Collection<Metric> unusedMetrics = new ArrayList<>();
			for (RecipientMetricFactory<String, EmailMessage<String>> metricFactory : metricFactories) {
				unusedMetrics.add(metricFactory.create());
			}
			String headerPrefix = "recommendationType,group scorer,w_out,half_life,account";
			MetricResultCollection<String> resultCollection = new MetricResultCollection<String>(
					headerPrefix, unusedMetrics,
					dataset.getHierarchicalRecipientRecommendationMetricsFile());
			
			File perMessageFolder = dataset.getPerMessageMetricsFolder();

			for (String account : dataset.getAccountIds()) {
				System.out.println(account);
				
				File accountPerMessageFolder = new File(perMessageFolder, ""+account);

				Collection<EmailMessage<String>> trainingMessages = dataset
						.getTrainMessages(account, percentTraining);
				Collection<EmailMessage<String>> testMessages = dataset
						.getTestMessages(account, percentTraining);
				
				if (underTestableThreshold(testMessages)) {
					continue;
				}
				
				for (GroupScorerFactory<String> scorerFactory : groupScorerFactories) {
					
					for (Double wOut : wOuts) {
						
						for (Double halfLife : halfLives) {
							
							GroupScorer<String> groupScorer = scorerFactory
									.create(wOut, halfLife);

							for (GroupBasedRecipientRecommenderFactory<String, EmailMessage<String>> recommenderFactory : recommenderFactories) {
								GroupBasedRecipientRecommender<String, EmailMessage<String>> recommender = recommenderFactory
										.createRecommender(groupScorer);
								HierarchicalRecipientRecommender<String, EmailMessage<String>> hierarchicalRecommender = new HierarchicalRecipientRecommender<>(recommender);

								Collection<RecipientMetric<String, EmailMessage<String>>> metrics = new ArrayList<>();
								for (RecipientMetricFactory<String, EmailMessage<String>> metricFactory : metricFactories) {
									metrics.add(metricFactory.create());
								}
								
								Collection<PerMessageMetric<String, EmailMessage<String>>> perMessageMetrics = new ArrayList<>();
								for (PerMessageMetric.Factory<String, EmailMessage<String>> factory : perMessageMetricFactories) {
									perMessageMetrics.add(factory.create());
								}

								HierarchicalRecipientRecommendationAcceptanceModeler<String, EmailMessage<String>> modeler = new HierarchicalRecipientRecommendationAcceptanceModeler<>(
										listSize, hierarchicalRecommender, trainingMessages,
										testMessages, metrics, perMessageMetrics, accountPerMessageFolder);
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
