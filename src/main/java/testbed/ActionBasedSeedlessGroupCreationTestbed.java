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
import recommendation.groups.seedless.fellows.FellowsRecommenderFactory;
import recommendation.groups.seedless.hybrid.HybridRecommenderFactory;
import recommendation.groups.seedless.hybrid.IOFunctions;
import testbed.dataset.actions.ActionsDataSet;
import data.preprocess.graphbuilder.ActionBasedGraphBuilder;
import data.preprocess.graphbuilder.ActionBasedGraphBuilderFactory;
import data.preprocess.graphbuilder.InteractionRankWeightedActionBasedGraphBuilder;
import data.preprocess.graphbuilder.SimpleActionBasedGraphBuilder;
import data.preprocess.graphbuilder.TimeThresholdActionBasedGraphBuilder;
import data.representation.actionbased.CollaborativeAction;
import data.representation.actionbased.CollaborativeActionThread;

public class ActionBasedSeedlessGroupCreationTestbed<Id, Collaborator extends Comparable<Collaborator>, Action extends CollaborativeAction<Collaborator>, ActionThread extends CollaborativeActionThread<Collaborator, Action>> {


	static final double PERCENT_TRAINING = 0.8;
	
	Class<Collaborator> collaboratorClass;
	Class<Action> actionClass;
	
	Collection<ActionsDataSet<Id, Collaborator, Action, ActionThread>> datasets = new ArrayList<>();

	Collection<SeedlessGroupRecommenderFactory<Collaborator>> seedlessRecommenderFactories = new ArrayList<>();
	Collection<ActionBasedGraphBuilderFactory<Collaborator, Action>> graphBuilderFactories = new ArrayList<>();
	Collection<ActionBasedGroupMetric<Collaborator, Action>> metrics = new ArrayList<>();

	Map<String, Collection<ConstantValues>> graphConstants = new HashMap<>();
	
	public ActionBasedSeedlessGroupCreationTestbed(
			Collection<ActionsDataSet<Id, Collaborator, Action, ActionThread>> datasets,
			Map<String, Collection<ConstantValues>> graphConstants,
			Class<Collaborator> collaboratorClass, Class<Action> actionClass) {
		this.datasets = datasets;
		this.graphConstants = graphConstants;
		this.collaboratorClass = collaboratorClass;
		this.actionClass = actionClass;
		init(collaboratorClass, actionClass);
	}

	private void init(Class<Collaborator> collaboratorClass,
			Class<Action> actionClass) {

		// Add seedless recommender factories
		seedlessRecommenderFactories.add(new HybridRecommenderFactory<Collaborator>(false));
		seedlessRecommenderFactories.add(new FellowsRecommenderFactory<Collaborator>());

		// Add graph builders
		graphBuilderFactories.add(SimpleActionBasedGraphBuilder.factory(collaboratorClass, actionClass));
		graphBuilderFactories.add(TimeThresholdActionBasedGraphBuilder.factory(collaboratorClass, actionClass));
		graphBuilderFactories.add(InteractionRankWeightedActionBasedGraphBuilder.factory(collaboratorClass, actionClass));

		// Add metrics
		
		// Add metrics
		metrics.add(new TotalTestActionsMetric<Collaborator, Action>());
		metrics.add(new TotalRecommendedGroupsMetric<Collaborator, Action>());
		metrics.add(new GroupCenteredPercentDeletedMetric<Collaborator, Action>());
		metrics.add(new GroupCenteredPercentAddedMetric<Collaborator, Action>());
		metrics.add(new MessageCenteredPercentDeletedMetric<Collaborator, Action>());
		metrics.add(new MessageCenteredPercentAddedMetric<Collaborator, Action>());
		metrics.add(new TestActionsMatchedToRecommendationMetric<Collaborator, Action>());
		metrics.add(new TestActionsToRecommendationPerfectMatchesMetric<Collaborator, Action>());
		metrics.add(new RecommendationsMatchedToTestActionMetric<Collaborator, Action>());
		metrics.add(new RecommendationsToTestActionPerfectMatchesMetric<Collaborator, Action>());
		
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
	
	private void printGraph(File output,
			UndirectedGraph<Collaborator, DefaultEdge> graph) throws IOException {
		for (DefaultEdge edge : graph.edgeSet()) {
			Collaborator source = graph.getEdgeSource(edge);
			Collaborator target = graph.getEdgeTarget(edge);
			String edgeStr = target + "\t" + source + "\t" + graph.getEdgeWeight(edge);
			FileUtils.write(output, edgeStr + "\n", true);
		}
	}
	
	private Collection<MetricResult> collectResults(
			Collection<Action> trainMessages,
			Collection<Action> testMessages,
			GraphFormingActionBasedSeedlessGroupRecommender<Collaborator, Action> recommender,
			File groupOutputFile,
			File graphOutputFile) {
		
		for (Action pastAction : trainMessages) {
			recommender.addPastAction(pastAction);
		}

//		Collection<Set<Collaborator>> recommendations = new ArrayList<>();
		Collection<Set<Collaborator>> recommendations = recommender
				.getRecommendations();
		
		if (!groupOutputFile.getParentFile().exists()) {
			groupOutputFile.getParentFile().mkdirs();
		}
		IOFunctions<Collaborator> ioHelp = new  IOFunctions<>(collaboratorClass);
		ioHelp.printCliqueIDsToFile(groupOutputFile.getAbsolutePath(), recommendations);
		
		if (!graphOutputFile.getParentFile().exists()) {
			graphOutputFile.getParentFile().mkdirs();
		}
		try {
			printGraph(graphOutputFile, recommender.getGraph());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		ActionBasedSeedlessGroupRecommendationAcceptanceModeler<Collaborator, Action> modeler = new ActionBasedSeedlessGroupRecommendationAcceptanceModeler<Collaborator, Action>(
				recommendations, new ArrayList<Set<Collaborator>>(),
				testMessages, metrics);

		Collection<MetricResult> results = modeler
				.modelRecommendationAcceptance();
		return results;
	}
	
	private void useGraphBuilderNoArgs(
			ActionsDataSet<Id,Collaborator,Action,ActionThread> dataset,
			Id account,
			Collection<Action> trainMessages,
			Collection<Action> testMessages,
			SeedlessGroupRecommenderFactory<Collaborator> seedlessRecommenderFactory,
			ActionBasedGraphBuilderFactory<Collaborator, Action> graphBuilderFactory,
			MetricResultCollection<Id> resultCollection) throws IOException {

		Collection<ConstantValues> constantSets = graphConstants.get(SimpleActionBasedGraphBuilder.class);
		for (@SuppressWarnings("unused") ConstantValues constantSet : constantSets) {
			ActionBasedGraphBuilder<Collaborator, Action> graphBuilder = graphBuilderFactory
					.create();

			GraphFormingActionBasedSeedlessGroupRecommender<Collaborator, Action> recommender =
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
	
	private void useGraphBuilderTimeThreshold(
			ActionsDataSet<Id,Collaborator,Action,ActionThread> dataset,
			Id account,
			Collection<Action> trainMessages,
			Collection<Action> testMessages,
			SeedlessGroupRecommenderFactory<Collaborator> seedlessRecommenderFactory,
			ActionBasedGraphBuilderFactory<Collaborator, Action> graphBuilderFactory,
			MetricResultCollection<Id> resultCollection) throws IOException {
		

		Collection<ConstantValues> constantSets = graphConstants.get(TimeThresholdActionBasedGraphBuilder.class);
		for (ConstantValues constantSet : constantSets) {
			long timeThreshold = (Long) constantSet.constants[0];
			
			ActionBasedGraphBuilder<Collaborator, Action> graphBuilder =
					graphBuilderFactory.create(timeThreshold);

			GraphFormingActionBasedSeedlessGroupRecommender<Collaborator, Action> recommender = 
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
	
	private void useGraphBuilderScoredEdges(
			ActionsDataSet<Id,Collaborator,Action,ActionThread> dataset,
			Id account,
			Collection<Action> trainMessages,
			Collection<Action> testMessages,
			SeedlessGroupRecommenderFactory<Collaborator> seedlessRecommenderFactory,
			ActionBasedGraphBuilderFactory<Collaborator, Action> graphBuilderFactory,
			MetricResultCollection<Id> resultCollection) throws IOException {
		
		Collection<ConstantValues> constantSets = graphConstants.get(InteractionRankWeightedActionBasedGraphBuilder.class);
		for (ConstantValues constantSet : constantSets) {
			double wOut = (Double) constantSet.constants[0];
			long halfLife = (Long) constantSet.constants[1];
			double scoreThreshold = (Double) constantSet.constants[2];
			
			ActionBasedGraphBuilder<Collaborator, Action> graphBuilder =
					graphBuilderFactory.create(halfLife, wOut, scoreThreshold);

			GraphFormingActionBasedSeedlessGroupRecommender<Collaborator, Action> recommender = 
					new GraphFormingActionBasedSeedlessGroupRecommender<Collaborator, Action>(
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
	
	public void runTestbed() throws IOException {
		
		for (ActionsDataSet<Id,Collaborator,Action,ActionThread> dataset : datasets) {
			
			String headerPrefix = "graph builder,w_out,half_life,threshold,account";
			MetricResultCollection<Id> resultCollection =
					new MetricResultCollection<Id>(
							headerPrefix, new ArrayList<Metric>(metrics),
							dataset.getActionBasedSeedlessGroupsMetricsFile());
			
			for (Id account : dataset.getAccountIds()) {
				System.out.println(account);
				
				Collection<Action> trainMessages = dataset.getTrainMessages(account, PERCENT_TRAINING);
				Collection<Action> testMessages = dataset.getTestMessages(account, PERCENT_TRAINING);
				
				if (trainMessages.size() + testMessages.size() < 10) {
					continue;
				}
				
				for (SeedlessGroupRecommenderFactory<Collaborator> seedlessRecommenderFactory : seedlessRecommenderFactories) {
					for (ActionBasedGraphBuilderFactory<Collaborator, Action> graphBuilderFactory : graphBuilderFactories) {
						
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
			}
		}
	}

}
