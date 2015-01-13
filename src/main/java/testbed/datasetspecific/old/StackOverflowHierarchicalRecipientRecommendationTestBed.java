package testbed.datasetspecific.old;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;

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
import model.recommendation.recipients.NewsgroupHierarchicalRecipientRecommendationAcceptanceModeler;
import recommendation.recipients.groupbased.GroupBasedRecipientRecommender;
import recommendation.recipients.groupbased.GroupBasedRecipientRecommenderFactory;
import recommendation.recipients.groupbased.GroupScorer;
import recommendation.recipients.groupbased.GroupScorer.GroupScorerFactory;
import recommendation.recipients.groupbased.hierarchical.HierarchicalRecipientRecommender;
import recommendation.recipients.groupbased.interactionrank.InteractionRankGroupBasedRecipientRecommenderFactory;
import recommendation.recipients.groupbased.interactionrank.scoring.IntersectionGroupScore;
import recommendation.recipients.groupbased.interactionrank.scoring.IntersectionWeightedScore;
import recommendation.recipients.groupbased.interactionrank.scoring.SubsetGroupScore;
import recommendation.recipients.groupbased.interactionrank.scoring.SubsetWeightedScore;
import testbed.dataset.actions.messages.stackoverflow.SampledStackOverflowDataset;
import testbed.dataset.actions.messages.stackoverflow.StackOverflowDataset;
import data.representation.actionbased.messages.ComparableAddress;
import data.representation.actionbased.messages.newsgroup.JavaMailNewsgroupPost;
import data.representation.actionbased.messages.stackoverflow.StackOverflowMessage;
import data.representation.actionbased.messages.stackoverflow.StackOverflowThread;

public class StackOverflowHierarchicalRecipientRecommendationTestBed {

	static double percentTraining = 0.8;
	static int listSize = 4;

	static Collection<StackOverflowDataset<String, StackOverflowMessage<String>, StackOverflowThread<String, StackOverflowMessage<String>>>> dataSets = new ArrayList<>();
	static Collection<GroupBasedRecipientRecommenderFactory<String, StackOverflowMessage<String>>> recommenderFactories = new ArrayList<>();
	static Collection<GroupScorerFactory<String>> groupScorerFactories = new ArrayList<>();
	static Collection<Double> wOuts = new ArrayList<>();
	static Collection<Double> halfLives = new ArrayList<>();

	static Collection<RecipientMetricFactory<String, StackOverflowMessage<String>>> metricFactories = new ArrayList<>();
	static Collection<PerMessageMetric.Factory<String, StackOverflowMessage<String>>> perMessageMetricFactories = new ArrayList<>();

	static {

		// Add data sets
		try {
			dataSets.add(new SampledStackOverflowDataset(
					"Sampled StackOverflow", new File(
							"data/Stack Overflow/10000 Random Questions")));
		} catch (IOException | ParseException e) {
			e.printStackTrace();
			System.exit(0);
		}

		// Add recommender factories
		recommenderFactories
				.add(new InteractionRankGroupBasedRecipientRecommenderFactory<String, StackOverflowMessage<String>>());
		
		// Add GroupScorerFactories
//		groupScorerFactories.add(IntersectionGroupCount.factory(String.class));
		groupScorerFactories.add(IntersectionGroupScore.factory(String.class));
		groupScorerFactories.add(IntersectionWeightedScore.factory(String.class));
//		groupScorerFactories.add(SubsetGroupCount.factory(String.class));
		groupScorerFactories.add(SubsetGroupScore.factory(String.class));
		groupScorerFactories.add(SubsetWeightedScore.factory(String.class));
//		groupScorerFactories.add(TopContactScore.factory(String.class));
		
		// Add w_outs
		wOuts.add(1.0);
		
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
		metricFactories.add(TotalTrainMessagesMetric.factory(String.class, StackOverflowMessage.class));
		metricFactories.add(TotalTestMessagesMetric.factory(String.class, StackOverflowMessage.class));
		metricFactories.add(RecommendableMessagesMetric.factory(String.class, StackOverflowMessage.class));
		metricFactories.add(RecipientsToAddressPerMessageMetric.factory(String.class, StackOverflowMessage.class));
		metricFactories.add(TotalRecipientsToAddressMetric.factory(String.class, StackOverflowMessage.class));
		metricFactories.add(RecipientsToAddressPerMessageMetric.factory(String.class, StackOverflowMessage.class));
		metricFactories.add(RequestsForListsMetric.factory(String.class, StackOverflowMessage.class));
		metricFactories.add(PrecisionMetric.factory(String.class, StackOverflowMessage.class));
		metricFactories.add(RecallMetric.factory(String.class, StackOverflowMessage.class));
		metricFactories.add(TotalSelectedPerClickMetric.factory(String.class, StackOverflowMessage.class));
		metricFactories.add(RelativeScansMetric.factory(String.class, StackOverflowMessage.class));
		metricFactories.add(RelativeClicksMetric.factory(String.class, StackOverflowMessage.class));
		metricFactories.add(RelativeManualEntriesMetric.factory(String.class, StackOverflowMessage.class));
		metricFactories.add(RelativeSwitchesMetric.factory(String.class, StackOverflowMessage.class));
		metricFactories.add(TrainWithMultipleFromMetric.factory(String.class, StackOverflowMessage.class));
		metricFactories.add(TestWithMultipleFromMetric.factory(String.class, StackOverflowMessage.class));
		
		perMessageMetricFactories.add(RecipientPerMessageMetric.factory(TotalRecipientsToAddressMetric.factory(String.class, StackOverflowMessage.class)));
		perMessageMetricFactories.add(RecipientPerMessageMetric.factory(RequestsForListsMetric.factory(String.class, StackOverflowMessage.class)));
		perMessageMetricFactories.add(RecipientPerMessageMetric.factory(PrecisionMetric.factory(String.class, StackOverflowMessage.class)));
		perMessageMetricFactories.add(RecipientPerMessageMetric.factory(RecallMetric.factory(String.class, StackOverflowMessage.class)));
		perMessageMetricFactories.add(RecipientPerMessageMetric.factory(RecallMetric.factory(String.class, StackOverflowMessage.class)));
		perMessageMetricFactories.add(RecipientPerMessageMetric.factory(TotalSelectedPerClickMetric.factory(String.class, StackOverflowMessage.class)));
		perMessageMetricFactories.add(RecipientPerMessageMetric.factory(RelativeScansMetric.factory(String.class, StackOverflowMessage.class)));
		perMessageMetricFactories.add(RecipientPerMessageMetric.factory(RelativeClicksMetric.factory(String.class, StackOverflowMessage.class)));
		perMessageMetricFactories.add(RecipientPerMessageMetric.factory(RelativeManualEntriesMetric.factory(String.class, StackOverflowMessage.class)));
		perMessageMetricFactories.add(RecipientPerMessageMetric.factory(RelativeSwitchesMetric.factory(String.class, StackOverflowMessage.class)));
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
	
	private static void runWithoutWoutAndHalfLife(Long account,
			Collection<StackOverflowMessage<String>> trainingMessages,
			Collection<StackOverflowMessage<String>> testMessages,
			MetricResultCollection<Long> resultCollection,
			GroupBasedRecipientRecommenderFactory<String, StackOverflowMessage<String>> recommenderFactory,
			GroupScorerFactory<String> scorerFactory)
			throws IOException {
		
		
		GroupScorer<String> groupScorer = scorerFactory
				.create();

		
			GroupBasedRecipientRecommender<String, StackOverflowMessage<String>> recommender = recommenderFactory
					.createRecommender(groupScorer);
			HierarchicalRecipientRecommender<String, StackOverflowMessage<String>> hierarchicalRecommender = new HierarchicalRecipientRecommender<>(recommender);



			String label = recommender.getTypeOfRecommender();
			label += ","+groupScorer.getName();
			label += ",N/A";
			System.out.println(label);

			Collection<MetricResult> results = modelRecommendations(
					trainingMessages, testMessages, hierarchicalRecommender);
			
			resultCollection.addResults(label, account,
					results);
	}
	
	private static void runWithWoutAndHalfLife(
			Long account,
			Collection<StackOverflowMessage<String>> trainingMessages,
			Collection<StackOverflowMessage<String>> testMessages,
			MetricResultCollection<Long> resultCollection,
			GroupBasedRecipientRecommenderFactory<String, StackOverflowMessage<String>> recommenderFactory,
			GroupScorerFactory<String> scorerFactory)
			throws IOException {

		for (Double wOut : wOuts) {

			for (Double halfLife : halfLives) {

				GroupScorer<String> groupScorer = scorerFactory
						.create(wOut, halfLife);

				GroupBasedRecipientRecommender<String, StackOverflowMessage<String>> recommender = recommenderFactory
						.createRecommender(groupScorer);
				HierarchicalRecipientRecommender<String, StackOverflowMessage<String>> hierarchicalRecommender = new HierarchicalRecipientRecommender<>(
						recommender);


				String label = recommender.getTypeOfRecommender();
				label += "," + groupScorer.getName();
				label += "," + getHalfLifeName(halfLife);
				System.out.println(label);

				Collection<MetricResult> results = modelRecommendations(
						trainingMessages, testMessages, hierarchicalRecommender);

				resultCollection.addResults(label, account, results);
			}
		}
	}
	
	private static Collection<MetricResult> modelRecommendations(
			Collection<StackOverflowMessage<String>> trainingMessages,
			Collection<StackOverflowMessage<String>> testMessages,
			HierarchicalRecipientRecommender<String, StackOverflowMessage<String>> hierarchicalRecommender) {
		
		Collection<RecipientMetric<String, StackOverflowMessage<String>>> metrics = new ArrayList<>();
		for (RecipientMetricFactory<String, StackOverflowMessage<String>> metricFactory : metricFactories) {
			metrics.add(metricFactory.create());
		}
		
		Collection<PerMessageMetric<String, StackOverflowMessage<String>>> perMessageMetrics = new ArrayList<>();
		for (PerMessageMetric.Factory<String, StackOverflowMessage<String>> factory : perMessageMetricFactories) {
			perMessageMetrics.add(factory.create());
		}
		
		//TODO: fix to not be temp
		File tempFolder = new File("temp");

		HierarchicalRecipientRecommendationAcceptanceModeler<String, StackOverflowMessage<String>> modeler = new NewsgroupHierarchicalRecipientRecommendationAcceptanceModeler<>(
				listSize, hierarchicalRecommender, trainingMessages,
				testMessages, metrics, perMessageMetrics, tempFolder);
		Collection<MetricResult> results = modeler
				.modelRecommendationAcceptance();
		return results;
	}

	public static void main(String[] args) throws IOException {

		for (StackOverflowDataset<String, StackOverflowMessage<String>, StackOverflowThread<String, StackOverflowMessage<String>>> dataset : dataSets) {

			Collection<Metric> unusedMetrics = new ArrayList<>();
			for (RecipientMetricFactory<String, StackOverflowMessage<String>> metricFactory : metricFactories) {
				unusedMetrics.add(metricFactory.create());
			}
			String headerPrefix = "recommendationType,group scorer,w_out,half_life,account";
			MetricResultCollection<Long> resultCollection = new MetricResultCollection<Long>(
					headerPrefix, unusedMetrics,
					dataset.getHierarchicalRecipientRecommendationMetricsFile());

			for (Long account : dataset.getAccountIds()) {
				System.out.println(account);

				Collection<StackOverflowMessage<String>> trainingMessages = dataset
						.getTrainQuestions(account, percentTraining);
				Collection<StackOverflowMessage<String>> testMessages = dataset
						.getTestQuestions(account, percentTraining);

				for (GroupBasedRecipientRecommenderFactory<String, StackOverflowMessage<String>> recommenderFactory : recommenderFactories) {
					for (GroupScorerFactory<String> scorerFactory : groupScorerFactories) {

						if (scorerFactory.takesWOutAndHalfLife()) {
							runWithWoutAndHalfLife(account, trainingMessages,
									testMessages, resultCollection,
									recommenderFactory, scorerFactory);
						} else {
							runWithoutWoutAndHalfLife(account,
									trainingMessages, testMessages,
									resultCollection, recommenderFactory,
									scorerFactory);
						}
					}
				}
			}
		}

	}

}
