package testbed.datasetspecific.old;

import java.io.File;
import java.io.IOException;
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
import recommendation.recipients.groupbased.interactionrank.scoring.IntersectionGroupCount;
import recommendation.recipients.groupbased.interactionrank.scoring.IntersectionGroupScore;
import recommendation.recipients.groupbased.interactionrank.scoring.IntersectionWeightedScore;
import recommendation.recipients.groupbased.interactionrank.scoring.SubsetGroupCount;
import recommendation.recipients.groupbased.interactionrank.scoring.SubsetGroupScore;
import recommendation.recipients.groupbased.interactionrank.scoring.SubsetWeightedScore;
import recommendation.recipients.groupbased.interactionrank.scoring.TopContactScore;
import testbed.dataset.actions.messages.newsgroups.NewsgroupDataset;
import testbed.dataset.actions.messages.newsgroups.Newsgroups20Dataset;
import data.representation.actionbased.messages.ComparableAddress;
import data.representation.actionbased.messages.newsgroup.JavaMailNewsgroupPost;
import data.representation.actionbased.messages.newsgroup.NewsgroupThread;

public class NewsgroupHierarchicalRecipientRecommendationTestBed {

	static double percentTraining = 0.8;
	static int listSize = 4;

	static Collection<NewsgroupDataset<Integer, ComparableAddress, JavaMailNewsgroupPost, NewsgroupThread<ComparableAddress, JavaMailNewsgroupPost>>> dataSets = new ArrayList<>();
	static Collection<GroupBasedRecipientRecommenderFactory<ComparableAddress, JavaMailNewsgroupPost>> recommenderFactories = new ArrayList<>();
	static Collection<GroupScorerFactory<ComparableAddress>> groupScorerFactories = new ArrayList<>();
	static Collection<Double> wOuts = new ArrayList<>();
	static Collection<Double> halfLives = new ArrayList<>();

	static Collection<RecipientMetricFactory<ComparableAddress, JavaMailNewsgroupPost>> metricFactories = new ArrayList<>();
	static Collection<PerMessageMetric.Factory<ComparableAddress, JavaMailNewsgroupPost>> perMessageMetricFactories = new ArrayList<>();

	static {

		// Add data sets
		dataSets.add(new Newsgroups20Dataset("20Newsgroups", new File(
				"data/20 Newsgroups")));

		// Add recommender factories
		recommenderFactories
				.add(new InteractionRankGroupBasedRecipientRecommenderFactory<ComparableAddress, JavaMailNewsgroupPost>());
		
		// Add GroupScorerFactories
//		groupScorerFactories.add(IntersectionGroupCount.factory(ComparableAddress.class));
		groupScorerFactories.add(IntersectionGroupScore.factory(ComparableAddress.class));
		groupScorerFactories.add(IntersectionWeightedScore.factory(ComparableAddress.class));
//		groupScorerFactories.add(SubsetGroupCount.factory(ComparableAddress.class));
		groupScorerFactories.add(SubsetGroupScore.factory(ComparableAddress.class));
		groupScorerFactories.add(SubsetWeightedScore.factory(ComparableAddress.class));
//		groupScorerFactories.add(TopContactScore.factory(ComparableAddress.class));
		
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
		metricFactories.add(TotalTrainMessagesMetric.factory(ComparableAddress.class, JavaMailNewsgroupPost.class));
		metricFactories.add(TotalTestMessagesMetric.factory(ComparableAddress.class, JavaMailNewsgroupPost.class));
		metricFactories.add(RecommendableMessagesMetric.factory(ComparableAddress.class, JavaMailNewsgroupPost.class));
		metricFactories.add(RecipientsToAddressPerMessageMetric.factory(ComparableAddress.class, JavaMailNewsgroupPost.class));
		metricFactories.add(TotalRecipientsToAddressMetric.factory(ComparableAddress.class, JavaMailNewsgroupPost.class));
		metricFactories.add(RecipientsToAddressPerMessageMetric.factory(ComparableAddress.class, JavaMailNewsgroupPost.class));
		metricFactories.add(RequestsForListsMetric.factory(ComparableAddress.class, JavaMailNewsgroupPost.class));
		metricFactories.add(PrecisionMetric.factory(ComparableAddress.class, JavaMailNewsgroupPost.class));
		metricFactories.add(RecallMetric.factory(ComparableAddress.class, JavaMailNewsgroupPost.class));
		metricFactories.add(TotalSelectedPerClickMetric.factory(ComparableAddress.class, JavaMailNewsgroupPost.class));
		metricFactories.add(RelativeScansMetric.factory(ComparableAddress.class, JavaMailNewsgroupPost.class));
		metricFactories.add(RelativeClicksMetric.factory(ComparableAddress.class, JavaMailNewsgroupPost.class));
		metricFactories.add(RelativeManualEntriesMetric.factory(ComparableAddress.class, JavaMailNewsgroupPost.class));
		metricFactories.add(RelativeSwitchesMetric.factory(ComparableAddress.class, JavaMailNewsgroupPost.class));
		metricFactories.add(TrainWithMultipleFromMetric.factory(ComparableAddress.class, JavaMailNewsgroupPost.class));
		metricFactories.add(TestWithMultipleFromMetric.factory(ComparableAddress.class, JavaMailNewsgroupPost.class));
		
		perMessageMetricFactories.add(RecipientPerMessageMetric.factory(TotalRecipientsToAddressMetric.factory(ComparableAddress.class, JavaMailNewsgroupPost.class)));
		perMessageMetricFactories.add(RecipientPerMessageMetric.factory(RequestsForListsMetric.factory(ComparableAddress.class, JavaMailNewsgroupPost.class)));
		perMessageMetricFactories.add(RecipientPerMessageMetric.factory(PrecisionMetric.factory(ComparableAddress.class, JavaMailNewsgroupPost.class)));
		perMessageMetricFactories.add(RecipientPerMessageMetric.factory(RecallMetric.factory(ComparableAddress.class, JavaMailNewsgroupPost.class)));
		perMessageMetricFactories.add(RecipientPerMessageMetric.factory(RecallMetric.factory(ComparableAddress.class, JavaMailNewsgroupPost.class)));
		perMessageMetricFactories.add(RecipientPerMessageMetric.factory(TotalSelectedPerClickMetric.factory(ComparableAddress.class, JavaMailNewsgroupPost.class)));
		perMessageMetricFactories.add(RecipientPerMessageMetric.factory(RelativeScansMetric.factory(ComparableAddress.class, JavaMailNewsgroupPost.class)));
		perMessageMetricFactories.add(RecipientPerMessageMetric.factory(RelativeClicksMetric.factory(ComparableAddress.class, JavaMailNewsgroupPost.class)));
		perMessageMetricFactories.add(RecipientPerMessageMetric.factory(RelativeManualEntriesMetric.factory(ComparableAddress.class, JavaMailNewsgroupPost.class)));
		perMessageMetricFactories.add(RecipientPerMessageMetric.factory(RelativeSwitchesMetric.factory(ComparableAddress.class, JavaMailNewsgroupPost.class)));
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
	
	private static void runWithoutWoutAndHalfLife(Integer account,
			Collection<JavaMailNewsgroupPost> trainingMessages,
			Collection<JavaMailNewsgroupPost> testMessages,
			MetricResultCollection<Integer> resultCollection,
			GroupBasedRecipientRecommenderFactory<ComparableAddress, JavaMailNewsgroupPost> recommenderFactory,
			GroupScorerFactory<ComparableAddress> scorerFactory)
			throws IOException {
		
		
		GroupScorer<ComparableAddress> groupScorer = scorerFactory
				.create();

		
			GroupBasedRecipientRecommender<ComparableAddress, JavaMailNewsgroupPost> recommender = recommenderFactory
					.createRecommender(groupScorer);
			HierarchicalRecipientRecommender<ComparableAddress, JavaMailNewsgroupPost> hierarchicalRecommender = new HierarchicalRecipientRecommender<>(recommender);



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
			Integer account,
			Collection<JavaMailNewsgroupPost> trainingMessages,
			Collection<JavaMailNewsgroupPost> testMessages,
			MetricResultCollection<Integer> resultCollection,
			GroupBasedRecipientRecommenderFactory<ComparableAddress, JavaMailNewsgroupPost> recommenderFactory,
			GroupScorerFactory<ComparableAddress> scorerFactory)
			throws IOException {

		for (Double wOut : wOuts) {

			for (Double halfLife : halfLives) {

				GroupScorer<ComparableAddress> groupScorer = scorerFactory
						.create(wOut, halfLife);

				GroupBasedRecipientRecommender<ComparableAddress, JavaMailNewsgroupPost> recommender = recommenderFactory
						.createRecommender(groupScorer);
				HierarchicalRecipientRecommender<ComparableAddress, JavaMailNewsgroupPost> hierarchicalRecommender = new HierarchicalRecipientRecommender<>(
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
			Collection<JavaMailNewsgroupPost> trainingMessages,
			Collection<JavaMailNewsgroupPost> testMessages,
			HierarchicalRecipientRecommender<ComparableAddress, JavaMailNewsgroupPost> hierarchicalRecommender) {
		
		Collection<RecipientMetric<ComparableAddress, JavaMailNewsgroupPost>> metrics = new ArrayList<>();
		for (RecipientMetricFactory<ComparableAddress, JavaMailNewsgroupPost> metricFactory : metricFactories) {
			metrics.add(metricFactory.create());
		}
		
		Collection<PerMessageMetric<ComparableAddress, JavaMailNewsgroupPost>> perMessageMetrics = new ArrayList<>();
		for (PerMessageMetric.Factory<ComparableAddress, JavaMailNewsgroupPost> factory : perMessageMetricFactories) {
			perMessageMetrics.add(factory.create());
		}
		
		//TODO: fix to not be temp
		File tempFolder = new File("temp");

		HierarchicalRecipientRecommendationAcceptanceModeler<ComparableAddress, JavaMailNewsgroupPost> modeler = new NewsgroupHierarchicalRecipientRecommendationAcceptanceModeler<>(
				listSize, hierarchicalRecommender, trainingMessages,
				testMessages, metrics, perMessageMetrics, tempFolder);
		Collection<MetricResult> results = modeler
				.modelRecommendationAcceptance();
		return results;
	}

	public static void main(String[] args) throws IOException {

		for (NewsgroupDataset<Integer, ComparableAddress, JavaMailNewsgroupPost, NewsgroupThread<ComparableAddress, JavaMailNewsgroupPost>> dataset : dataSets) {

			Collection<Metric> unusedMetrics = new ArrayList<>();
			for (RecipientMetricFactory<ComparableAddress, JavaMailNewsgroupPost> metricFactory : metricFactories) {
				unusedMetrics.add(metricFactory.create());
			}
			String headerPrefix = "recommendationType,group scorer,w_out,half_life,account";
			MetricResultCollection<Integer> resultCollection = new MetricResultCollection<Integer>(
					headerPrefix, unusedMetrics,
					dataset.getHierarchicalRecipientRecommendationMetricsFile());
			
			File perMessageFolder = dataset.getPerMessageMetricsFolder();

			for (Integer account : dataset.getAccountIds()) {

				Collection<JavaMailNewsgroupPost> trainingMessages = dataset
						.getTrainMessages(account, percentTraining);
				Collection<JavaMailNewsgroupPost> testMessages = dataset
						.getTestMessages(account, percentTraining);

				for (GroupBasedRecipientRecommenderFactory<ComparableAddress, JavaMailNewsgroupPost> recommenderFactory : recommenderFactories) {
					for (GroupScorerFactory<ComparableAddress> scorerFactory : groupScorerFactories) {

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
