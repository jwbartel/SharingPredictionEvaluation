package testbed;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import metrics.Metric;
import metrics.MetricResult;
import metrics.MetricResultCollection;
import metrics.groups.actionbased.ActionBasedGroupMetric;
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
import model.recommendation.groups.ActionBasedSeedlessGroupRecommendationAcceptanceModeler;

import org.apache.commons.io.FileUtils;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import recommendation.groups.seedless.SeedlessGroupRecommenderFactory;
import recommendation.groups.seedless.actionbased.GraphFormingActionBasedSeedlessGroupRecommender;
import recommendation.groups.seedless.hybrid.HybridRecommenderFactory;
import recommendation.groups.seedless.hybrid.IOFunctions;
import testbed.dataset.actions.messages.newsgroups.NewsgroupDataset;
import testbed.dataset.actions.messages.newsgroups.Newsgroups20Dataset;
import util.tools.io.ComparableAddressValueParser;
import util.tools.io.StringValueWriterAndParser;
import data.preprocess.graphbuilder.ActionBasedGraphBuilder;
import data.preprocess.graphbuilder.ActionBasedGraphBuilderFactory;
import data.preprocess.graphbuilder.InteractionRankWeightedActionBasedGraphBuilder;
import data.preprocess.graphbuilder.SimpleActionBasedGraphBuilder;
import data.preprocess.graphbuilder.TimeThresholdActionBasedGraphBuilder;
import data.representation.actionbased.CollaborativeAction;
import data.representation.actionbased.messages.ComparableAddress;
import data.representation.actionbased.messages.email.EmailMessage;
import data.representation.actionbased.messages.newsgroup.JavaMailNewsgroupPost;
import data.representation.actionbased.messages.newsgroup.NewsgroupThread;

public class NewsgroupActionBasedSeedlessGroupRecommendationTestBed {

	static double percentTraining = 0.8;

	static Collection<NewsgroupDataset<Integer, ComparableAddress, JavaMailNewsgroupPost, NewsgroupThread<ComparableAddress, JavaMailNewsgroupPost>>> dataSets = new ArrayList<>();
	
	static Map<Class<? extends ActionBasedGraphBuilder>, Collection<ConstantValues>> constants = new HashMap<>();
	
	static Collection<SeedlessGroupRecommenderFactory<ComparableAddress>> seedlessRecommenderFactories = new ArrayList<>();
	static Collection<ActionBasedGraphBuilderFactory<ComparableAddress, CollaborativeAction<ComparableAddress>>> graphBuilderFactories = new ArrayList<>();
	
//	static Collection<Double> wOuts = new ArrayList<>();
//	static Collection<Long> halfLives = new ArrayList<>();
//	static Collection<Double> scoreThresholds = new ArrayList<>();

	static Collection<ActionBasedGroupMetric<ComparableAddress, JavaMailNewsgroupPost>> metrics = new ArrayList<>();

	static {

		// Add data sets
		dataSets.add(new Newsgroups20Dataset("20Newsgroups", new File(
				"data/20 Newsgroups")));
		
		// Add seedless recommender factories
		seedlessRecommenderFactories.add(new HybridRecommenderFactory<ComparableAddress>(new ComparableAddressValueParser(), 700));
		//seedlessRecommenderFactories.add(new FellowsRecommenderFactory<String>());
		
		// Add graph builders
		graphBuilderFactories.add(SimpleActionBasedGraphBuilder.factory(String.class, EmailMessage.class));
		graphBuilderFactories.add(TimeThresholdActionBasedGraphBuilder.factory(String.class, EmailMessage.class));
		graphBuilderFactories.add(InteractionRankWeightedActionBasedGraphBuilder.factory(String.class, EmailMessage.class));
		
		// Add w_outs
//		wOuts.add(0.125);
//		wOuts.add(0.25);
//		wOuts.add(0.5);
//		wOuts.add(1.0);
//		wOuts.add(2.0);
//		wOuts.add(4.0);
//		wOuts.add(8.0);
		
//		// Add half lives
//		halfLives.add(1000.0*60); // 1 minute
//		halfLives.add(1000L*60*60); // 1 hour
//		halfLives.add(1000L*60*60*24); // 1 day
//		halfLives.add(1000L*60*60*24*7); // 1 week
//		halfLives.add(1000L*60*60*24*7*4); // 4 weeks
//		halfLives.add(1000L*60*60*24*365/2); // 6 months
//		halfLives.add(1000L*60*60*24*365); // 1 year
//		halfLives.add(1000L*60*60*24*365*2); // 2 years
		
		// Add score thresholds
//		scoreThresholds.add(0.0);
//		scoreThresholds.add(0.05);
//		scoreThresholds.add(0.10);
//		scoreThresholds.add(0.15);
//		scoreThresholds.add(0.20);
//		scoreThresholds.add(0.25);
//		scoreThresholds.add(0.30);
//		scoreThresholds.add(0.35);
//		scoreThresholds.add(0.40);
//		scoreThresholds.add(0.45);
//		scoreThresholds.add(0.50);

		
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
		
		// Add metrics
		metrics.add(new TotalTestActionsMetric<ComparableAddress, JavaMailNewsgroupPost>());
		metrics.add(new TotalRecommendedGroupsMetric<ComparableAddress, JavaMailNewsgroupPost>());
		metrics.add(new GroupCenteredPercentDeletedMetric<ComparableAddress, JavaMailNewsgroupPost>());
		metrics.add(new GroupCenteredPercentAddedMetric<ComparableAddress, JavaMailNewsgroupPost>());
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
	
	private static void printGraph(File output,
			UndirectedGraph<ComparableAddress, DefaultEdge> graph) throws IOException {
		for (DefaultEdge edge : graph.edgeSet()) {
			ComparableAddress source = graph.getEdgeSource(edge);
			ComparableAddress target = graph.getEdgeTarget(edge);
			String edgeStr = target + "\t" + source + "\t" + graph.getEdgeWeight(edge);
			FileUtils.write(output, edgeStr + "\n", true);
		}
	}
	
	private static Collection<MetricResult> collectResults(
			Collection<JavaMailNewsgroupPost> trainMessages,
			Collection<JavaMailNewsgroupPost> testMessages,
			GraphFormingActionBasedSeedlessGroupRecommender<ComparableAddress> recommender,
			File groupOutputFile,
			File graphOutputFile) {
		
		for (JavaMailNewsgroupPost pastAction : trainMessages) {
			recommender.addPastAction(pastAction);
		}

//		Collection<Set<ComparableAddress>> recommendations = new ArrayList<>();
		Collection<Set<ComparableAddress>> recommendations = recommender
				.getRecommendations();
		Collection<Set<String>> strRecommendations  = new ArrayList<Set<String>>();
		for (Set<ComparableAddress> recommendation : recommendations) {
			Set<String> strRecommendation = new TreeSet<>();
			for (ComparableAddress member : recommendation) {
				strRecommendation.add(member.toString());
			}
			strRecommendations.add(strRecommendation);
		}
		
		if (!groupOutputFile.getParentFile().exists()) {
			groupOutputFile.getParentFile().mkdirs();
		}
		IOFunctions<String> ioHelp = new  IOFunctions<>(String.class);
		ioHelp.printCliqueIDsToFile(groupOutputFile.getAbsolutePath(), strRecommendations);
		
		if (!graphOutputFile.getParentFile().exists()) {
			graphOutputFile.getParentFile().mkdirs();
		}
		try {
			printGraph(graphOutputFile, recommender.getGraph());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		ActionBasedSeedlessGroupRecommendationAcceptanceModeler<ComparableAddress, JavaMailNewsgroupPost> modeler = new ActionBasedSeedlessGroupRecommendationAcceptanceModeler<ComparableAddress, JavaMailNewsgroupPost>(
				recommendations, new ArrayList<Set<ComparableAddress>>(),
				testMessages, metrics);

		Collection<MetricResult> results = modeler
				.modelRecommendationAcceptance();
		return results;
	}
	
	private static void useGraphBuilderNoArgs(
			NewsgroupDataset<Integer, ComparableAddress, JavaMailNewsgroupPost, NewsgroupThread<ComparableAddress, JavaMailNewsgroupPost>> dataset,
			Integer account,
			Collection<JavaMailNewsgroupPost> trainMessages,
			Collection<JavaMailNewsgroupPost> testMessages,
			SeedlessGroupRecommenderFactory<ComparableAddress> seedlessRecommenderFactory,
			ActionBasedGraphBuilderFactory<ComparableAddress, CollaborativeAction<ComparableAddress>> graphBuilderFactory,
			MetricResultCollection<Integer> resultCollection) throws IOException {

		Collection<ConstantValues> constantSets = constants.get(SimpleActionBasedGraphBuilder.class);
		for (ConstantValues constantSet : constantSets) {
			ActionBasedGraphBuilder<ComparableAddress, CollaborativeAction<ComparableAddress>> graphBuilder = graphBuilderFactory
					.create();

			GraphFormingActionBasedSeedlessGroupRecommender<ComparableAddress> recommender =
					new GraphFormingActionBasedSeedlessGroupRecommender<>(
							seedlessRecommenderFactory, graphBuilder);

			File groupsFile = dataset.getArgumentlessGraphBasedGroupsFile(account,
					graphBuilder.getName());
			File graphFile = dataset.getArgumentlessGraphBasedGraphFile(account,
					graphBuilder.getName());
			Collection<MetricResult> results = collectResults(trainMessages, testMessages,
					recommender, groupsFile, graphFile);

			String label = graphBuilder.getName() + ",N/A,N/A,N/A";
			System.out.println(label);
			resultCollection.addResults(label, account, results);
		}
	}
	
	private static void useGraphBuilderTimeThreshold(
			NewsgroupDataset<Integer, ComparableAddress, JavaMailNewsgroupPost, NewsgroupThread<ComparableAddress, JavaMailNewsgroupPost>> dataset,
			Integer account,
			Collection<JavaMailNewsgroupPost> trainMessages,
			Collection<JavaMailNewsgroupPost> testMessages,
			SeedlessGroupRecommenderFactory<ComparableAddress> seedlessRecommenderFactory,
			ActionBasedGraphBuilderFactory<ComparableAddress, CollaborativeAction<ComparableAddress>> graphBuilderFactory,
			MetricResultCollection<Integer> resultCollection) throws IOException {
		
		Collection<ConstantValues> constantSets = constants.get(TimeThresholdActionBasedGraphBuilder.class);
		for (ConstantValues constantSet : constantSets) {
			long timeThreshold = (Long) constantSet.constants[0];
			
			ActionBasedGraphBuilder<ComparableAddress, CollaborativeAction<ComparableAddress>> graphBuilder =
					graphBuilderFactory.create(timeThreshold);

			GraphFormingActionBasedSeedlessGroupRecommender<ComparableAddress> recommender = 
					new GraphFormingActionBasedSeedlessGroupRecommender<>(
							seedlessRecommenderFactory, graphBuilder);
					
			File groupsFile = dataset.getTimeThresholdGraphBasedGroupsFile(
					account, graphBuilder.getName(),
					getHalfLifeName(timeThreshold));
			File graphFile = dataset.getTimeThresholdGraphBasedGraphFile(
					account, graphBuilder.getName(),
					getHalfLifeName(timeThreshold));
			Collection<MetricResult> results = collectResults(trainMessages,
					testMessages, recommender, groupsFile, graphFile);

			String label = graphBuilder.getName() + ",N/A,N/A," + getHalfLifeName(timeThreshold);
			System.out.println(label);
			resultCollection.addResults(label, account, results);
		}
	}
	
	private static void useGraphBuilderScoredEdges(
			NewsgroupDataset<Integer, ComparableAddress, JavaMailNewsgroupPost, NewsgroupThread<ComparableAddress, JavaMailNewsgroupPost>> dataset,
			Integer account,
			Collection<JavaMailNewsgroupPost> trainMessages,
			Collection<JavaMailNewsgroupPost> testMessages,
			SeedlessGroupRecommenderFactory<ComparableAddress> seedlessRecommenderFactory,
			ActionBasedGraphBuilderFactory<ComparableAddress, CollaborativeAction<ComparableAddress>> graphBuilderFactory,
			MetricResultCollection<Integer> resultCollection) throws IOException {
		
		Collection<ConstantValues> constantSets = constants.get(InteractionRankWeightedActionBasedGraphBuilder.class);
		for (ConstantValues constantSet : constantSets) {
			double wOut = (Double) constantSet.constants[0];
			long halfLife = (Long) constantSet.constants[1];
			double scoreThreshold = (Double) constantSet.constants[2];
			
			ActionBasedGraphBuilder<ComparableAddress, CollaborativeAction<ComparableAddress>> graphBuilder =
					graphBuilderFactory.create(halfLife, wOut, scoreThreshold);

			GraphFormingActionBasedSeedlessGroupRecommender<ComparableAddress> recommender = 
					new GraphFormingActionBasedSeedlessGroupRecommender<ComparableAddress>(
							seedlessRecommenderFactory, graphBuilder);

			File groupsFile = dataset
					.getScoredEdgesGraphBasedGroupsFile(account,
							graphBuilder.getName(),
							getHalfLifeName(halfLife),
							wOut,
							scoreThreshold);
			File graphFile = dataset
					.getScoredEdgesGraphBasedGraphFile(account,
							graphBuilder.getName(),
							getHalfLifeName(halfLife),
							wOut,
							scoreThreshold);
			Collection<MetricResult> results = collectResults(
					trainMessages, testMessages, recommender, groupsFile, graphFile);

			String label = graphBuilder.getName() +
					"," + getHalfLifeName(halfLife) +
					"," + wOut +
					"," + scoreThreshold;
			System.out.println(label);
			resultCollection.addResults(label, account, results);
		}
	}
	
	private static Collection<JavaMailNewsgroupPost> getFirstMessagesOfThreads(Collection<NewsgroupThread<ComparableAddress, JavaMailNewsgroupPost>> threads) {
		Collection<JavaMailNewsgroupPost> messages = new ArrayList<>();
		for (NewsgroupThread<ComparableAddress, JavaMailNewsgroupPost> thread : threads) {
			ArrayList<JavaMailNewsgroupPost> threadedMessages = new ArrayList<>(thread.getThreadedActions());
			Collections.sort(threadedMessages);
			JavaMailNewsgroupPost firstMessage = threadedMessages.get(0);
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
							useGraphBuilderScoredEdges(dataset, account, trainMessages,
									testMessages, seedlessRecommenderFactory,
									graphBuilderFactory, resultCollection);
						} else if (graphBuilderFactory.takesTime()) {
							useGraphBuilderTimeThreshold(dataset, account,
									trainMessages, testMessages,
									seedlessRecommenderFactory,
									graphBuilderFactory, resultCollection);
						} else {
							useGraphBuilderNoArgs(dataset, account, trainMessages,
									testMessages, seedlessRecommenderFactory,
									graphBuilderFactory, resultCollection);
						}

					}
				}
			}
		}
	}
}
