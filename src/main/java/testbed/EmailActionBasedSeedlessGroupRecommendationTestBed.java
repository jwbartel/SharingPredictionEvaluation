package testbed;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import javax.imageio.stream.FileImageOutputStream;

import org.apache.commons.io.FileUtils;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import data.preprocess.graphbuilder.ActionBasedGraphBuilder;
import data.preprocess.graphbuilder.ActionBasedGraphBuilderFactory;
import data.preprocess.graphbuilder.InteractionRankWeightedActionBasedGraphBuilder;
import data.preprocess.graphbuilder.SimpleActionBasedGraphBuilder;
import data.preprocess.graphbuilder.TimeThresholdActionBasedGraphBuilder;
import data.representation.actionbased.CollaborativeAction;
import data.representation.actionbased.messages.email.EmailMessage;
import data.representation.actionbased.messages.email.EmailThread;
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
import recommendation.groups.seedless.hybrid.IOFunctions;
import testbed.dataset.actions.ActionsDataSet;
import testbed.dataset.actions.messages.email.EnronEmailDataSet;
import testbed.dataset.actions.messages.email.ResponseTimeStudyDataSet;
import testbed.dataset.actions.messages.newsgroups.Newsgroups20Dataset;

public class EmailActionBasedSeedlessGroupRecommendationTestBed {

	static double percentTraining = 0.8;

	static Collection<ActionsDataSet<String,String,EmailMessage<String>,EmailThread<String, EmailMessage<String>>>> dataSets = new ArrayList<>();
	
	static Collection<SeedlessGroupRecommenderFactory<String>> seedlessRecommenderFactories = new ArrayList<>();
	static Collection<ActionBasedGraphBuilderFactory<String, CollaborativeAction<String>>> graphBuilderFactories = new ArrayList<>();
	
	static Collection<Double> wOuts = new ArrayList<>();
	static Collection<Double> halfLives = new ArrayList<>();
	static Collection<Double> scoreThresholds = new ArrayList<>();

	static Collection<ActionBasedGroupMetric<String, EmailMessage<String>>> metrics = new ArrayList<>();

	static {

		// Add data sets
//		dataSets.add(new EnronEmailDataSet("enron",
//				EnronEmailDataSet.DEFAULT_ACCOUNTS, new File("data/Enron")));
		dataSets.add(new ResponseTimeStudyDataSet("response time", new File(
				"data/Email Response Study")));
		
		// Add seedless recommender factories
		seedlessRecommenderFactories.add(new HybridRecommenderFactory<String>());
		//seedlessRecommenderFactories.add(new FellowsRecommenderFactory<String>());
		
		// Add graph builders
		graphBuilderFactories.add(SimpleActionBasedGraphBuilder.factory(String.class, EmailMessage.class));
		graphBuilderFactories.add(TimeThresholdActionBasedGraphBuilder.factory(String.class, EmailMessage.class));
		graphBuilderFactories.add(InteractionRankWeightedActionBasedGraphBuilder.factory(String.class, EmailMessage.class));
		
		// Add w_outs
//		wOuts.add(0.25);
//		wOuts.add(0.5);
		wOuts.add(1.0);
//		wOuts.add(2.0);
//		wOuts.add(4.0);
		
		// Add half lives
//		halfLives.add(1000.0*60); // 1 minute
//		halfLives.add(1000.0*60*60); // 1 hour
//		halfLives.add(1000.0*60*60*24); // 1 day
		halfLives.add(1000.0*60*60*24*7); // 1 week
		halfLives.add(1000.0*60*60*24*7*2); // 2 weeks
		halfLives.add(1000.0*60*60*24*7*4); // 1 month
		halfLives.add(1000.0*60*60*24*7*4*2); // 2 months
//		halfLives.add(1000.0*60*60*24*365/2); // 6 months
//		halfLives.add(1000.0*60*60*24*365); // 1 year
//		halfLives.add(1000.0*60*60*24*365*2); // 2 years
		
		// Add score thresholds
//		scoreThresholds.add(0.05);
//		scoreThresholds.add(0.10);
//		scoreThresholds.add(0.15);
//		scoreThresholds.add(0.20);
		scoreThresholds.add(0.25);
//		scoreThresholds.add(0.30);
//		scoreThresholds.add(0.35);
//		scoreThresholds.add(0.40);
//		scoreThresholds.add(0.45);
//		scoreThresholds.add(0.50);
		
		// Add metrics
		metrics.add(new TotalTestActionsMetric<String, EmailMessage<String>>());
		metrics.add(new TotalRecommendedGroupsMetric<String, EmailMessage<String>>());
		metrics.add(new MessageCenteredPercentDeletedMetric<String, EmailMessage<String>>());
		metrics.add(new MessageCenteredPercentAddedMetric<String, EmailMessage<String>>());
		metrics.add(new TestActionsMatchedToRecommendationMetric<String, EmailMessage<String>>());
		metrics.add(new TestActionsToRecommendationPerfectMatchesMetric<String, EmailMessage<String>>());
		metrics.add(new RecommendationsMatchedToTestActionMetric<String, EmailMessage<String>>());
		metrics.add(new RecommendationsToTestActionPerfectMatchesMetric<String, EmailMessage<String>>());
		
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
		if (halfLife < 28) {
			return halfLife/7 + " weeks";
		}
		if (halfLife < 365/2) {
			halfLife = halfLife/28;
			return halfLife + " months";
		}
		halfLife /= 365;
		return halfLife + " years";
	}
	
	private static void printGraph(File output,
			UndirectedGraph<String, DefaultEdge> graph) throws IOException {
		for (DefaultEdge edge : graph.edgeSet()) {
			String source = graph.getEdgeSource(edge);
			String target = graph.getEdgeTarget(edge);
			FileUtils.write(output, target + "\t" + source + "\n", true);
		}
	}
	
	private static Collection<MetricResult> collectResults(
			Collection<EmailMessage<String>> trainMessages,
			Collection<EmailMessage<String>> testMessages,
			GraphFormingActionBasedSeedlessGroupRecommender<String> recommender,
			File groupOutputFile,
			File graphOutputFile) {
		
		for (EmailMessage<String> pastAction : trainMessages) {
			recommender.addPastAction(pastAction);
		}

		Collection<Set<String>> recommendations = recommender
				.getRecommendations();
		
		if (!groupOutputFile.getParentFile().exists()) {
			groupOutputFile.getParentFile().mkdirs();
		}
		IOFunctions<String> ioHelp = new  IOFunctions<>(String.class);
		ioHelp.printCliqueIDsToFile(groupOutputFile.getAbsolutePath(), recommendations);
		
		if (!graphOutputFile.getParentFile().exists()) {
			graphOutputFile.getParentFile().mkdirs();
		}
		try {
			printGraph(graphOutputFile, recommender.getGraph());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		ActionBasedSeedlessGroupRecommendationAcceptanceModeler<String, EmailMessage<String>> modeler = new ActionBasedSeedlessGroupRecommendationAcceptanceModeler<String, EmailMessage<String>>(
				recommendations, new ArrayList<Set<String>>(),
				testMessages, metrics);

		Collection<MetricResult> results = modeler
				.modelRecommendationAcceptance();
		return results;
	}
	
	private static void useGraphBuilderNoArgs(
			ActionsDataSet<String,String,EmailMessage<String>,EmailThread<String, EmailMessage<String>>> dataset,
			String account,
			Collection<EmailMessage<String>> trainMessages,
			Collection<EmailMessage<String>> testMessages,
			SeedlessGroupRecommenderFactory<String> seedlessRecommenderFactory,
			ActionBasedGraphBuilderFactory<String, CollaborativeAction<String>> graphBuilderFactory,
			MetricResultCollection<String> resultCollection) throws IOException {
		
		ActionBasedGraphBuilder<String, CollaborativeAction<String>> graphBuilder =
				graphBuilderFactory.create();
		
		GraphFormingActionBasedSeedlessGroupRecommender<String> recommender = 
				new GraphFormingActionBasedSeedlessGroupRecommender<>(
						seedlessRecommenderFactory, graphBuilder);

		File groupsFile = dataset.getArgumentlessGraphBasedGroupsFile(
				account, graphBuilder.getName());
		File graphFile = dataset.getArgumentlessGraphBasedGraphFile(
				account, graphBuilder.getName());
		Collection<MetricResult> results = collectResults(trainMessages,
				testMessages, recommender, groupsFile, graphFile);
		
		String label = graphBuilder.getName() + ",N/A,N/A,N/A";
		resultCollection.addResults(label, account, results);
	}
	
	private static void useGraphBuilderTimeThreshold(
			ActionsDataSet<String,String,EmailMessage<String>,EmailThread<String, EmailMessage<String>>> dataset,
			String account,
			Collection<EmailMessage<String>> trainMessages,
			Collection<EmailMessage<String>> testMessages,
			SeedlessGroupRecommenderFactory<String> seedlessRecommenderFactory,
			ActionBasedGraphBuilderFactory<String, CollaborativeAction<String>> graphBuilderFactory,
			MetricResultCollection<String> resultCollection) throws IOException {
		
		for (double timeThreshold : halfLives) {
			ActionBasedGraphBuilder<String, CollaborativeAction<String>> graphBuilder =
					graphBuilderFactory.create((long) timeThreshold);

			GraphFormingActionBasedSeedlessGroupRecommender<String> recommender = 
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
			resultCollection.addResults(label, account, results);
		}
	}
	
	private static void useGraphBuilderScoredEdges(
			ActionsDataSet<String,String,EmailMessage<String>,EmailThread<String, EmailMessage<String>>> dataset,
			String account,
			Collection<EmailMessage<String>> trainMessages,
			Collection<EmailMessage<String>> testMessages,
			SeedlessGroupRecommenderFactory<String> seedlessRecommenderFactory,
			ActionBasedGraphBuilderFactory<String, CollaborativeAction<String>> graphBuilderFactory,
			MetricResultCollection<String> resultCollection) throws IOException {
		
		for (double halfLife : halfLives) {
			for (double wOut : wOuts) {
				for (double scoreThreshold : scoreThresholds) {
					ActionBasedGraphBuilder<String, CollaborativeAction<String>> graphBuilder =
							graphBuilderFactory.create( (long) halfLife, wOut, scoreThreshold);

					GraphFormingActionBasedSeedlessGroupRecommender<String> recommender = 
							new GraphFormingActionBasedSeedlessGroupRecommender<>(
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
					resultCollection.addResults(label, account, results);
				}
			}
		}
	}
	
	public static void main(String[] args) throws IOException {
		
		for (ActionsDataSet<String,String,EmailMessage<String>,EmailThread<String, EmailMessage<String>>> dataset : dataSets) {
			
			String headerPrefix = "graph builder,w_out,half_life,threshold,account";
			MetricResultCollection<String> resultCollection =
					new MetricResultCollection<String>(
							headerPrefix, new ArrayList<Metric>(metrics),
							dataset.getActionBasedSeedlessGroupsMetricsFile());
			
			for (String account : dataset.getAccountIds()) {
				System.out.println(account);
				
				Collection<EmailMessage<String>> trainMessages = dataset.getTrainMessages(account, percentTraining);
				Collection<EmailMessage<String>> testMessages = dataset.getTestMessages(account, percentTraining);
				
				if (trainMessages.size() + testMessages.size() < 10) {
					continue;
				}
				
				for (SeedlessGroupRecommenderFactory<String> seedlessRecommenderFactory : seedlessRecommenderFactories) {
					for (ActionBasedGraphBuilderFactory<String, CollaborativeAction<String>> graphBuilderFactory : graphBuilderFactories) {
						
						if (graphBuilderFactory.takesScoredEdgeWithThreshold()) {
							useGraphBuilderScoredEdges(dataset, account,
									trainMessages, testMessages,
									seedlessRecommenderFactory,
									graphBuilderFactory, resultCollection);
						} else if (graphBuilderFactory.takesTime()) {
							useGraphBuilderTimeThreshold(dataset, account,
									trainMessages, testMessages,
									seedlessRecommenderFactory,
									graphBuilderFactory, resultCollection);
						} else {
							useGraphBuilderNoArgs(dataset, account,
									trainMessages, testMessages,
									seedlessRecommenderFactory,
									graphBuilderFactory, resultCollection);
						}

					}
				}
				break;
			}
		}
	}
}
