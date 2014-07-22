package testbed;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import metrics.Metric;
import metrics.MetricResult;
import metrics.MetricResultCollection;
import metrics.groups.actionbased.GroupCenteredPercentAddedMetric;
import metrics.groups.actionbased.GroupCenteredPercentDeletedMetric;
import metrics.groups.actionbased.MessageCenteredPercentAddedMetric;
import metrics.groups.actionbased.MessageCenteredPercentDeletedMetric;
import metrics.groups.actionbased.RecommendationsMatchedToTestActionMetric;
import metrics.groups.actionbased.RecommendationsToTestActionPerfectMatchesMetric;
import metrics.groups.actionbased.TestActionsMatchedToRecommendationMetric;
import metrics.groups.actionbased.TestActionsToRecommendationPerfectMatchesMetric;
import metrics.groups.actionbased.TotalRecommendedGroupsMetric;
import metrics.groups.actionbased.TotalTestActionsMetric;
import metrics.groups.actionbased.bursty.BurstyGroupMetric;
import metrics.groups.actionbased.bursty.BurstyGroupMetricFactory;
import metrics.groups.actionbased.bursty.MessagesTriggeringGroupCreationMetric;
import metrics.groups.actionbased.bursty.RecommendationsCreatedPerBurstMetric;
import metrics.groups.actionbased.bursty.RepurposedActionBasedGroupMetric;
import model.recommendation.groups.BurstyGroupRecommendationAcceptanceModeler;
import recommendation.groups.seedless.SeedlessGroupRecommenderFactory;
import recommendation.groups.seedless.actionbased.GraphFormingActionBasedSeedlessGroupRecommender;
import recommendation.groups.seedless.actionbased.bursty.BurstyGroupRecommender;
import recommendation.groups.seedless.actionbased.bursty.RelativeEditsThresholdMatcher;
import recommendation.groups.seedless.hybrid.HybridRecommenderFactory;
import testbed.dataset.actions.ActionsDataSet;
import testbed.dataset.actions.messages.email.EnronEmailDataSet;
import testbed.dataset.actions.messages.newsgroups.NewsgroupDataset;
import testbed.dataset.actions.messages.newsgroups.Newsgroups20Dataset;
import data.preprocess.graphbuilder.ActionBasedGraphBuilder;
import data.preprocess.graphbuilder.ActionBasedGraphBuilderFactory;
import data.preprocess.graphbuilder.InteractionRankWeightedActionBasedGraphBuilder;
import data.preprocess.graphbuilder.SimpleActionBasedGraphBuilder;
import data.preprocess.graphbuilder.TimeThresholdActionBasedGraphBuilder;
import data.representation.actionbased.CollaborativeAction;
import data.representation.actionbased.messages.ComparableAddress;
import data.representation.actionbased.messages.email.EmailMessage;
import data.representation.actionbased.messages.email.EmailThread;
import data.representation.actionbased.messages.newsgroup.JavaMailNewsgroupPost;
import data.representation.actionbased.messages.newsgroup.NewsgroupThread;

public class NewsgroupBurstyGroupCreationTestBed {
	static double percentTraining = 0.8;

	static Collection<NewsgroupDataset<Integer, ComparableAddress, JavaMailNewsgroupPost, NewsgroupThread<ComparableAddress, JavaMailNewsgroupPost>>> dataSets = new ArrayList<>();
	
	static Collection<SeedlessGroupRecommenderFactory<ComparableAddress>> seedlessRecommenderFactories = new ArrayList<>();
	static Collection<ActionBasedGraphBuilderFactory<ComparableAddress, CollaborativeAction<ComparableAddress>>> graphBuilderFactories = new ArrayList<>();

	static Map<Class<? extends ActionBasedGraphBuilder>, Collection<ConstantValues>> constants = new HashMap<>();

	static Collection<Double> distanceThresholds = new ArrayList<>();

	static Collection<BurstyGroupMetricFactory<ComparableAddress, JavaMailNewsgroupPost>> metricFactories = new ArrayList<>();

	static {

		// Add data sets
		dataSets.add(new Newsgroups20Dataset("20Newsgroups", new File(
				"data/20 Newsgroups")));

		// Add seedless recommender factories
		seedlessRecommenderFactories.add(new HybridRecommenderFactory<ComparableAddress>(false));
		//seedlessRecommenderFactories.add(new FellowsRecommenderFactory<String>());

		// Add graph builders
		graphBuilderFactories.add(SimpleActionBasedGraphBuilder.factory(String.class, EmailMessage.class));
		graphBuilderFactories.add(TimeThresholdActionBasedGraphBuilder.factory(String.class, EmailMessage.class));
		graphBuilderFactories.add(InteractionRankWeightedActionBasedGraphBuilder.factory(String.class, EmailMessage.class));

		
		Collection<ConstantValues> simpleConstants = new ArrayList<>();
		Object[] simpleConstantSet = {};
		simpleConstants.add(new ConstantValues(simpleConstantSet));
		constants.put(SimpleActionBasedGraphBuilder.class, simpleConstants);
		
		Collection<ConstantValues> timeThresholdConstants = new ArrayList<>();
		Object[] timeThresholdConstantSet1 = {1000L*60*60*24*7}; //1.0 weeks
		Object[] timeThresholdConstantSet2 = {1000L*60*60*24*7*4}; //4.0 weeks
		Object[] timeThresholdConstantSet3 = {1000L*60*60*24*365/2}; //0.5 years
		Object[] timeThresholdConstantSet4 = {1000L*60*60*24*365}; //1 year
		Object[] timeThresholdConstantSet5 = {1000L*60*60*24*365*2}; //2 years
		timeThresholdConstants.add(new ConstantValues(timeThresholdConstantSet1));
		timeThresholdConstants.add(new ConstantValues(timeThresholdConstantSet2));
		timeThresholdConstants.add(new ConstantValues(timeThresholdConstantSet3));
		timeThresholdConstants.add(new ConstantValues(timeThresholdConstantSet4));
		timeThresholdConstants.add(new ConstantValues(timeThresholdConstantSet5));
		constants.put(TimeThresholdActionBasedGraphBuilder.class, timeThresholdConstants);
		
		Collection<ConstantValues> interactionRankConstants = new ArrayList<>();
		Object[] interactionRankConstantSet1 = {1.0, 1000L*60*60*24*7*4, 0.02}; //wOut=1.0, halfLife=1.0 weeks, threshold=0.02
		Object[] interactionRankConstantSet2 = {1.0, 1000L*60*60*24*7*4, 0.6}; //wOut=1.0, halfLife=4 weeks, threshold=0.6
		Object[] interactionRankConstantSet3 = {1.0, 1000L*60*60*24*365/2, 1.7}; //wOut=1.0, halfLife=0.5 years, threshold=1.7
		Object[] interactionRankConstantSet4 = {1.0, 1000L*60*60*24*365, 1.8}; //wOut=1.0, halfLife=1 year, threshold=1.8
		Object[] interactionRankConstantSet5 = {1.0, 1000L*60*60*24*365*2, 0.01}; //wOut=1.0, halfLife=2 years, threshold=0.01
		interactionRankConstants.add(new ConstantValues(interactionRankConstantSet1));
		interactionRankConstants.add(new ConstantValues(interactionRankConstantSet2));
		interactionRankConstants.add(new ConstantValues(interactionRankConstantSet3));
		interactionRankConstants.add(new ConstantValues(interactionRankConstantSet4));
		interactionRankConstants.add(new ConstantValues(interactionRankConstantSet5));
		constants.put(InteractionRankWeightedActionBasedGraphBuilder.class, interactionRankConstants);

		// Add thresholds for seed and recommendation matchers
		distanceThresholds.add(0.00);
		//distanceThresholds.add(0.05);
		distanceThresholds.add(0.10);
		//distanceThresholds.add(0.15);
		distanceThresholds.add(0.20);
		//distanceThresholds.add(0.25);
		//distanceThresholds.add(0.30);

		// Add metrics
		metricFactories.add(MessagesTriggeringGroupCreationMetric.factory(ComparableAddress.class, EmailMessage.class));
		metricFactories.add(RecommendationsCreatedPerBurstMetric.factory(ComparableAddress.class, EmailMessage.class));
		metricFactories.add(RepurposedActionBasedGroupMetric.factory(new TotalTestActionsMetric<ComparableAddress, JavaMailNewsgroupPost>()));
		metricFactories.add(RepurposedActionBasedGroupMetric.factory(new TotalRecommendedGroupsMetric<ComparableAddress, JavaMailNewsgroupPost>()));
		metricFactories.add(RepurposedActionBasedGroupMetric.factory(new GroupCenteredPercentDeletedMetric<ComparableAddress, JavaMailNewsgroupPost>()));
		metricFactories.add(RepurposedActionBasedGroupMetric.factory(new GroupCenteredPercentAddedMetric<ComparableAddress, JavaMailNewsgroupPost>()));
		metricFactories.add(RepurposedActionBasedGroupMetric.factory(new MessageCenteredPercentDeletedMetric<ComparableAddress, JavaMailNewsgroupPost>()));
		metricFactories.add(RepurposedActionBasedGroupMetric.factory(new MessageCenteredPercentAddedMetric<ComparableAddress, JavaMailNewsgroupPost>()));
		metricFactories.add(RepurposedActionBasedGroupMetric.factory(new TestActionsMatchedToRecommendationMetric<ComparableAddress, JavaMailNewsgroupPost>()));
		metricFactories.add(RepurposedActionBasedGroupMetric.factory(new TestActionsToRecommendationPerfectMatchesMetric<ComparableAddress, JavaMailNewsgroupPost>()));
		metricFactories.add(RepurposedActionBasedGroupMetric.factory(new RecommendationsMatchedToTestActionMetric<ComparableAddress, JavaMailNewsgroupPost>()));
		metricFactories.add(RepurposedActionBasedGroupMetric.factory(new RecommendationsToTestActionPerfectMatchesMetric<ComparableAddress, JavaMailNewsgroupPost>()));

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
		if (halfLife < 60) {
			return halfLife + " minutes";
		}
		halfLife /= 60;
		if (halfLife < 24) {
			return halfLife + " hours";
		}
		halfLife /= 24;
		if (halfLife < 7) {
			return halfLife + " days";
		}
		if (halfLife <= 28) {
			return halfLife / 7 + " weeks";
		}
		halfLife /= 365;
		return halfLife + " years";
	}

	private static Collection<MetricResult> collectResults(
			Collection<JavaMailNewsgroupPost> trainMessages,
			Collection<JavaMailNewsgroupPost> testMessages,
			Double seedClosenessThreshold,
			Double recommendationClosenessThreshold,
			GraphFormingActionBasedSeedlessGroupRecommender<ComparableAddress> recommender) {

		ArrayList<BurstyGroupMetric<ComparableAddress, JavaMailNewsgroupPost>> metrics = new ArrayList<>();
		for (BurstyGroupMetricFactory<ComparableAddress, JavaMailNewsgroupPost> metricFactory : metricFactories) {
			metrics.add(metricFactory.create());
		}

		BurstyGroupRecommender<ComparableAddress> burstyRecommender = new BurstyGroupRecommender<>(
				recommender, new RelativeEditsThresholdMatcher<ComparableAddress>(
						seedClosenessThreshold),
				new RelativeEditsThresholdMatcher<ComparableAddress>(
						recommendationClosenessThreshold));
		BurstyGroupRecommendationAcceptanceModeler<ComparableAddress, JavaMailNewsgroupPost> modeler = new BurstyGroupRecommendationAcceptanceModeler<ComparableAddress, JavaMailNewsgroupPost>(
				burstyRecommender, trainMessages, testMessages,
				new ArrayList<Set<ComparableAddress>>(), metrics);

		Collection<MetricResult> results = modeler
				.modelRecommendationAcceptance();
		return results;
	}

	private static void useGraphBuilderNoArgs(
			Integer account,
			Collection<JavaMailNewsgroupPost> trainMessages,
			Collection<JavaMailNewsgroupPost> testMessages,
			SeedlessGroupRecommenderFactory<ComparableAddress> seedlessRecommenderFactory,
			ActionBasedGraphBuilderFactory<ComparableAddress, CollaborativeAction<ComparableAddress>> graphBuilderFactory,
			MetricResultCollection<Integer> resultCollection) throws IOException {

		ActionBasedGraphBuilder<ComparableAddress, CollaborativeAction<ComparableAddress>> graphBuilder = graphBuilderFactory
				.create();

		GraphFormingActionBasedSeedlessGroupRecommender<ComparableAddress> recommender = new GraphFormingActionBasedSeedlessGroupRecommender<ComparableAddress>(
				seedlessRecommenderFactory, graphBuilder);
		
		Collection<ConstantValues> constantSets = constants.get(graphBuilder.getClass());
		for (ConstantValues constantSet : constantSets) {
			for (Double seedThreshold : distanceThresholds) {
				for (Double recommendationThreshold : distanceThresholds) {
					String label = graphBuilder.getName() + ",";
					label += seedThreshold + ","
							+ recommendationThreshold + ",";
					label += "N/A,N/A,N/A";
					System.out.println("\t"+label);
					
					Collection<MetricResult> results = collectResults(
							trainMessages, testMessages, seedThreshold,
							recommendationThreshold, recommender);

					resultCollection.addResults(label, account, results);
				}
			}
		}

	}

	private static void useGraphBuilderTimeThreshold(
			Integer account,
			Collection<JavaMailNewsgroupPost> trainMessages,
			Collection<JavaMailNewsgroupPost> testMessages,
			SeedlessGroupRecommenderFactory<ComparableAddress> seedlessRecommenderFactory,
			ActionBasedGraphBuilderFactory<ComparableAddress, CollaborativeAction<ComparableAddress>> graphBuilderFactory,
			MetricResultCollection<Integer> resultCollection) throws IOException {

		

		ActionBasedGraphBuilder<ComparableAddress, CollaborativeAction<ComparableAddress>> tempGraphBuilder = graphBuilderFactory
				.create(0L);
		
		Collection<ConstantValues> constantSets = constants.get(tempGraphBuilder.getClass());
		for (ConstantValues constantSet : constantSets) {
			Long timeThreshold = (Long) constantSet.constants[0];
			
			ActionBasedGraphBuilder<ComparableAddress, CollaborativeAction<ComparableAddress>> graphBuilder = graphBuilderFactory
					.create(timeThreshold);

			GraphFormingActionBasedSeedlessGroupRecommender<ComparableAddress> recommender = new GraphFormingActionBasedSeedlessGroupRecommender<>(
					seedlessRecommenderFactory, graphBuilder);
			
			for (Double seedThreshold : distanceThresholds) {
				for (Double recommendationThreshold : distanceThresholds) {
					String label = graphBuilder.getName() + ",";
					label += seedThreshold + ","
							+ recommendationThreshold + ",";
					label += "N/A,N/A,"
							+ getHalfLifeName(timeThreshold);
					System.out.println("\t"+label);
					
					Collection<MetricResult> results = collectResults(
							trainMessages, testMessages, seedThreshold,
							recommendationThreshold, recommender);

					resultCollection.addResults(label, account, results);
				}
			}
			
		}
	}

	private static void useGraphBuilderScoredEdges(
			Integer account,
			Collection<JavaMailNewsgroupPost> trainMessages,
			Collection<JavaMailNewsgroupPost> testMessages,
			SeedlessGroupRecommenderFactory<ComparableAddress> seedlessRecommenderFactory,
			ActionBasedGraphBuilderFactory<ComparableAddress, CollaborativeAction<ComparableAddress>> graphBuilderFactory,
			MetricResultCollection<Integer> resultCollection) throws IOException {

		ActionBasedGraphBuilder<ComparableAddress, CollaborativeAction<ComparableAddress>> tempGraphBuilder = graphBuilderFactory
				.create(0L, 0.0, 0.0);
		
		Collection<ConstantValues> constantSets = constants.get(tempGraphBuilder.getClass());
		for (ConstantValues constantSet : constantSets) {
			
			Double wOut = (Double) constantSet.constants[0];
			Long halfLife = (Long) constantSet.constants[1];
			Double scoreThreshold = (Double) constantSet.constants[2];
			
			ActionBasedGraphBuilder<ComparableAddress, CollaborativeAction<ComparableAddress>> graphBuilder = graphBuilderFactory
					.create((long) halfLife, wOut, scoreThreshold);

			GraphFormingActionBasedSeedlessGroupRecommender<ComparableAddress> recommender = new GraphFormingActionBasedSeedlessGroupRecommender<>(
					seedlessRecommenderFactory, graphBuilder);

			for (Double seedThreshold : distanceThresholds) {
				for (Double recommendationThreshold : distanceThresholds) {

					String label = graphBuilder.getName() + ",";
					label += seedThreshold + ","
							+ recommendationThreshold + ",";
					label += getHalfLifeName(halfLife) + "," + wOut
							+ "," + scoreThreshold;
					System.out.println("\t"+label);
					
					Collection<MetricResult> results = collectResults(
							trainMessages, testMessages, seedThreshold,
							recommendationThreshold, recommender);

					resultCollection
							.addResults(label, account, results);
				}
			}
		}
	}

	public static void main(String[] args) throws IOException {

		for (NewsgroupDataset<Integer, ComparableAddress, JavaMailNewsgroupPost, NewsgroupThread<ComparableAddress, JavaMailNewsgroupPost>> dataset : dataSets) {

			ArrayList<Metric> tempMetrics = new ArrayList<>();
			for (BurstyGroupMetricFactory<ComparableAddress, JavaMailNewsgroupPost> metricFactory : metricFactories) {
				tempMetrics.add(metricFactory.create());
			}
			String headerPrefix = "graph builder,seed threshold,recommendation threshold,"
					+ "account,half_life,w_out,edge threshold";
			MetricResultCollection<Integer> resultCollection = new MetricResultCollection<Integer>(
					headerPrefix, tempMetrics,
					dataset.getBurstyGroupsMetricsFile());

			for (Integer account : dataset.getAccountIds()) {
				System.out.println(account);

				Collection<JavaMailNewsgroupPost> trainMessages = dataset
						.getTrainMessages(account, percentTraining);
				Collection<JavaMailNewsgroupPost> testMessages = dataset
						.getTestMessages(account, percentTraining);

				for (SeedlessGroupRecommenderFactory<ComparableAddress> seedlessRecommenderFactory : seedlessRecommenderFactories) {
					for (ActionBasedGraphBuilderFactory<ComparableAddress, CollaborativeAction<ComparableAddress>> graphBuilderFactory : graphBuilderFactories) {

						if (graphBuilderFactory.takesScoredEdgeWithThreshold()) {
							useGraphBuilderScoredEdges(account, trainMessages,
									testMessages, seedlessRecommenderFactory,
									graphBuilderFactory, resultCollection);
						} else if (graphBuilderFactory.takesTime()) {
							useGraphBuilderTimeThreshold(account,
									trainMessages, testMessages,
									seedlessRecommenderFactory,
									graphBuilderFactory, resultCollection);
						} else {
							useGraphBuilderNoArgs(account, trainMessages,
									testMessages, seedlessRecommenderFactory,
									graphBuilderFactory, resultCollection);
						}

					}
				}
			}
		}
	}
}
