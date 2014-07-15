package testbed;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream.GetField;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

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
import metrics.Metric;
import metrics.MetricResult;
import metrics.MetricResultCollection;
import metrics.groups.actionbased.ActionBasedGroupMetric;
import metrics.groups.actionbased.MessageCenteredPercentAddedMetric;
import metrics.groups.actionbased.MessageCenteredPercentDeletedMetric;
import metrics.groups.actionbased.RecommendationsMatchedToTestActionMetric;
import metrics.groups.actionbased.RecommendationsToTestActionPerfectMatchesMetric;
import metrics.groups.actionbased.TestActionsMatchedToRecommendationMetric;
import metrics.groups.actionbased.TestActionsToRecommendationPerfectMatchesMetric;
import metrics.groups.actionbased.TotalRecommendedGroupsMetric;
import metrics.groups.actionbased.TotalTestActionsMetric;
import model.recommendation.groups.ActionBasedSeedlessGroupRecommendationAcceptanceModeler;
import recommendation.groups.seedless.SeedlessGroupRecommenderFactory;
import recommendation.groups.seedless.actionbased.GraphFormingActionBasedSeedlessGroupRecommender;
import recommendation.groups.seedless.fellows.FellowsRecommenderFactory;
import recommendation.groups.seedless.hybrid.HybridRecommenderFactory;
import testbed.dataset.actions.ActionsDataSet;
import testbed.dataset.actions.messages.email.EnronEmailDataSet;
import testbed.dataset.actions.messages.newsgroups.NewsgroupDataset;
import testbed.dataset.actions.messages.newsgroups.Newsgroups20Dataset;

public class NewsgroupActionBasedSeedlessGroupRecommendationTestBed {

	static double percentTraining = 0.8;

	static Collection<NewsgroupDataset<Integer, ComparableAddress, JavaMailNewsgroupPost, NewsgroupThread<ComparableAddress, JavaMailNewsgroupPost>>> dataSets = new ArrayList<>();
	
	static Collection<SeedlessGroupRecommenderFactory<ComparableAddress>> seedlessRecommenderFactories = new ArrayList<>();
	static Collection<ActionBasedGraphBuilderFactory<ComparableAddress, CollaborativeAction<ComparableAddress>>> graphBuilderFactories = new ArrayList<>();
	
	static Collection<Double> wOuts = new ArrayList<>();
	static Collection<Double> halfLives = new ArrayList<>();
	static Collection<Double> scoreThresholds = new ArrayList<>();

	static Collection<ActionBasedGroupMetric<ComparableAddress, JavaMailNewsgroupPost>> metrics = new ArrayList<>();

	static {

		// Add data sets
		dataSets.add(new Newsgroups20Dataset("20Newsgroups", new File(
				"data/20 Newsgroups")));
		
		// Add seedless recommender factories
		seedlessRecommenderFactories.add(new HybridRecommenderFactory<ComparableAddress>());
		//seedlessRecommenderFactories.add(new FellowsRecommenderFactory<String>());
		
		// Add graph builders
		graphBuilderFactories.add(SimpleActionBasedGraphBuilder.factory(String.class, EmailMessage.class));
		graphBuilderFactories.add(TimeThresholdActionBasedGraphBuilder.factory(String.class, EmailMessage.class));
		graphBuilderFactories.add(InteractionRankWeightedActionBasedGraphBuilder.factory(String.class, EmailMessage.class));
		
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
		
		// Add score thresholds
		scoreThresholds.add(0.05);
		scoreThresholds.add(0.10);
		scoreThresholds.add(0.15);
		scoreThresholds.add(0.20);
		scoreThresholds.add(0.25);
		scoreThresholds.add(0.30);
		scoreThresholds.add(0.35);
		scoreThresholds.add(0.40);
		scoreThresholds.add(0.45);
		scoreThresholds.add(0.50);
		
		// Add metrics
		metrics.add(new TotalTestActionsMetric<ComparableAddress, JavaMailNewsgroupPost>());
		metrics.add(new TotalRecommendedGroupsMetric<ComparableAddress, JavaMailNewsgroupPost>());
		metrics.add(new MessageCenteredPercentDeletedMetric<ComparableAddress, JavaMailNewsgroupPost>());
		metrics.add(new MessageCenteredPercentAddedMetric<ComparableAddress, JavaMailNewsgroupPost>());
		metrics.add(new TestActionsMatchedToRecommendationMetric<ComparableAddress, JavaMailNewsgroupPost>());
		metrics.add(new TestActionsToRecommendationPerfectMatchesMetric<ComparableAddress, JavaMailNewsgroupPost>());
		metrics.add(new RecommendationsMatchedToTestActionMetric<ComparableAddress, JavaMailNewsgroupPost>());
		metrics.add(new RecommendationsToTestActionPerfectMatchesMetric<ComparableAddress, JavaMailNewsgroupPost>());
		
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
	
	private static Collection<MetricResult> collectResults(
			Collection<JavaMailNewsgroupPost> trainMessages,
			Collection<JavaMailNewsgroupPost> testMessages,
			GraphFormingActionBasedSeedlessGroupRecommender<ComparableAddress> recommender) {
		
		for (JavaMailNewsgroupPost pastAction : trainMessages) {
			recommender.addPastAction(pastAction);
		}

		Collection<Set<ComparableAddress>> recommendations = recommender
				.getRecommendations();
		ActionBasedSeedlessGroupRecommendationAcceptanceModeler<ComparableAddress, JavaMailNewsgroupPost> modeler = new ActionBasedSeedlessGroupRecommendationAcceptanceModeler<ComparableAddress, JavaMailNewsgroupPost>(
				recommendations, new ArrayList<Set<ComparableAddress>>(),
				testMessages, metrics);

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
		
		ActionBasedGraphBuilder<ComparableAddress, CollaborativeAction<ComparableAddress>> graphBuilder =
				graphBuilderFactory.create();
		
		GraphFormingActionBasedSeedlessGroupRecommender<ComparableAddress> recommender = 
				new GraphFormingActionBasedSeedlessGroupRecommender<>(
						seedlessRecommenderFactory, graphBuilder);

		Collection<MetricResult> results = collectResults(trainMessages,
				testMessages, recommender);
		
		String label = graphBuilder.getName() + ",N/A,N/A,N/A";
		resultCollection.addResults(label, account, results);
	}
	
	private static void useGraphBuilderTimeThreshold(
			Integer account,
			Collection<JavaMailNewsgroupPost> trainMessages,
			Collection<JavaMailNewsgroupPost> testMessages,
			SeedlessGroupRecommenderFactory<ComparableAddress> seedlessRecommenderFactory,
			ActionBasedGraphBuilderFactory<ComparableAddress, CollaborativeAction<ComparableAddress>> graphBuilderFactory,
			MetricResultCollection<Integer> resultCollection) throws IOException {
		
		for (double timeThreshold : halfLives) {
			ActionBasedGraphBuilder<ComparableAddress, CollaborativeAction<ComparableAddress>> graphBuilder =
					graphBuilderFactory.create((long) timeThreshold);

			GraphFormingActionBasedSeedlessGroupRecommender<ComparableAddress> recommender = 
					new GraphFormingActionBasedSeedlessGroupRecommender<>(
							seedlessRecommenderFactory, graphBuilder);

			Collection<MetricResult> results = collectResults(trainMessages,
					testMessages, recommender);

			String label = graphBuilder.getName() + ",N/A,N/A," + getHalfLifeName(timeThreshold);
			resultCollection.addResults(label, account, results);
		}
	}
	
	private static void useGraphBuilderScoredEdges(
			Integer account,
			Collection<JavaMailNewsgroupPost> trainMessages,
			Collection<JavaMailNewsgroupPost> testMessages,
			SeedlessGroupRecommenderFactory<ComparableAddress> seedlessRecommenderFactory,
			ActionBasedGraphBuilderFactory<ComparableAddress, CollaborativeAction<ComparableAddress>> graphBuilderFactory,
			MetricResultCollection<Integer> resultCollection) throws IOException {
		
		for (double halfLife : halfLives) {
			for (double wOut : wOuts) {
				for (double scoreThreshold : scoreThresholds) {
					ActionBasedGraphBuilder<ComparableAddress, CollaborativeAction<ComparableAddress>> graphBuilder =
							graphBuilderFactory.create( (long) halfLife, wOut, scoreThreshold);

					GraphFormingActionBasedSeedlessGroupRecommender<ComparableAddress> recommender = 
							new GraphFormingActionBasedSeedlessGroupRecommender<ComparableAddress>(
									seedlessRecommenderFactory, graphBuilder);

					Collection<MetricResult> results = collectResults(
							trainMessages, testMessages, recommender);

					String label = graphBuilder.getName() +
							"," + getHalfLifeName(halfLife) +
							"," + wOut +
							"," + scoreThreshold;
					resultCollection.addResults(label, account, results);
				}
			}
		}
	}
	
	private static Collection<JavaMailNewsgroupPost> getFirstMessagesOfThreads(Collection<NewsgroupThread<ComparableAddress, JavaMailNewsgroupPost>> threads) {
		Collection<JavaMailNewsgroupPost> messages = new ArrayList<>();
		for (NewsgroupThread<ComparableAddress, JavaMailNewsgroupPost> thread : threads) {
			JavaMailNewsgroupPost firstMessage = (new ArrayList<>(thread.getThreadedActions())).get(0);
			messages.add(firstMessage);
		}
		return messages;
	}
	
	public static void main(String[] args) throws IOException {
		
		for (NewsgroupDataset<Integer, ComparableAddress, JavaMailNewsgroupPost, NewsgroupThread<ComparableAddress, JavaMailNewsgroupPost>> dataset : dataSets) {
			
			String headerPrefix = "graph builder,w_out,half_life,threshold,account";
			MetricResultCollection<Integer> resultCollection =
					new MetricResultCollection<Integer>(
							headerPrefix, new ArrayList<Metric>(metrics),
							dataset.getActionBasedSeedlessGroupsMetricsFile());
			
			for (Integer account : dataset.getAccountIds()) {
				System.out.println(account);
				
				Collection<JavaMailNewsgroupPost> trainMessages = getFirstMessagesOfThreads(dataset.getTrainThreads(account, percentTraining));
				Collection<JavaMailNewsgroupPost> testMessages =  getFirstMessagesOfThreads(dataset.getTestThreads(account, percentTraining));
				
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
