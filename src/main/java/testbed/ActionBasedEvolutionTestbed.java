package testbed;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
import metrics.groups.actionbased.evolution.ActionBasedGroupEvolutionMetric;
import metrics.groups.actionbased.evolution.EdgeGrowthRateMetric;
import metrics.groups.actionbased.evolution.EvolutionRepurposedActionBasedGroupMetric;
import metrics.groups.actionbased.evolution.PercentMatchedRecommendedCreationRateMetric;
import metrics.groups.actionbased.evolution.PercentMatchedRecommendedEvolutionsRateMetric;
import metrics.groups.actionbased.evolution.PercentMatchedRecommendedUnchangedRateMetric;
import metrics.groups.actionbased.evolution.PercentRecommendedCreationRateMetric;
import metrics.groups.actionbased.evolution.PercentRecommendedEvolutionsRateMetric;
import metrics.groups.actionbased.evolution.PercentRecommendedUnchangedRateMetric;
import metrics.groups.actionbased.evolution.VertexGrowthRateMetric;
import model.recommendation.groups.ActionBasedEvolutionGroupRecommendationAcceptanceModeler;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import recommendation.groups.evolution.GroupEvolutionRecommender;
import recommendation.groups.evolution.GroupEvolutionRecommenderFactory;
import recommendation.groups.evolution.composed.ComposedGroupEvolutionRecommenderFactory;
import recommendation.groups.evolution.fullrecommendation.FullRecommendationGroupEvolutionRecommenderFactory;
import recommendation.groups.evolution.recommendations.RecommendedEvolution;
import recommendation.groups.seedless.SeedlessGroupRecommender;
import recommendation.groups.seedless.SeedlessGroupRecommenderFactory;
import recommendation.groups.seedless.hybrid.HybridRecommenderFactory;
import testbed.dataset.actions.ActionsDataSet;
import data.preprocess.graphbuilder.ActionBasedGraphBuilder;
import data.preprocess.graphbuilder.ActionBasedGraphBuilderFactory;
import data.preprocess.graphbuilder.InteractionRankWeightedActionBasedGraphBuilder;
import data.preprocess.graphbuilder.SimpleActionBasedGraphBuilder;
import data.preprocess.graphbuilder.TimeThresholdActionBasedGraphBuilder;
import data.representation.actionbased.CollaborativeAction;
import data.representation.actionbased.CollaborativeActionThread;

public abstract class ActionBasedEvolutionTestbed<Id, Collaborator, Action extends CollaborativeAction<Collaborator>, ActionThread extends CollaborativeActionThread<Collaborator, Action>> {

	public static final double MAX_VERTEX_GROWTH_RATE = 1.0;
	public static final double PERCENT_TRAINING = 0.8;
	
	Collection<ActionsDataSet<Id, Collaborator, Action, ActionThread>> datasets;

	Collection<GroupEvolutionRecommenderFactory<Collaborator>> evolutionRecommenderFactories;
	Collection<SeedlessGroupRecommenderFactory<Collaborator>> seedlessGroupRecommenderFactories;
	Collection<ActionBasedGraphBuilderFactory<Collaborator, Action>> graphBuilderFactories;

	Map<Class<? extends ActionBasedGraphBuilder<Collaborator, Action>>, Collection<ConstantValues>> graphConstants = new HashMap<>();

	Collection<ActionBasedGroupEvolutionMetric<Collaborator, Action>> metrics;

	public ActionBasedEvolutionTestbed(
			Collection<ActionsDataSet<Id, Collaborator, Action, ActionThread>> datasets,
			Map<Class<? extends ActionBasedGraphBuilder<Collaborator, Action>>, Collection<ConstantValues>> graphConstants,
			Class<Collaborator> collaboratorClass, Class<Action> actionClass) {
		this.datasets = datasets;
		this.graphConstants = graphConstants;
		init(collaboratorClass, actionClass);
	}

	private void init(Class<Collaborator> collaboratorClass,
			Class<Action> actionClass) {

		evolutionRecommenderFactories = new ArrayList<>();
		evolutionRecommenderFactories
				.add(new FullRecommendationGroupEvolutionRecommenderFactory<Collaborator>());
		evolutionRecommenderFactories
				.add(new ComposedGroupEvolutionRecommenderFactory<Collaborator>());

		seedlessGroupRecommenderFactories = new ArrayList<>();
		seedlessGroupRecommenderFactories
				.add(new HybridRecommenderFactory<Collaborator>());

		graphBuilderFactories = new ArrayList<>();
		graphBuilderFactories.add(SimpleActionBasedGraphBuilder.factory(
				collaboratorClass, actionClass));
		graphBuilderFactories.add(TimeThresholdActionBasedGraphBuilder.factory(
				collaboratorClass, actionClass));
		graphBuilderFactories
				.add(InteractionRankWeightedActionBasedGraphBuilder.factory(
						collaboratorClass, actionClass));

		metrics = new ArrayList<>();
		metrics.add(new VertexGrowthRateMetric<Collaborator, Action>());
		metrics.add(new EdgeGrowthRateMetric<Collaborator, Action>());
		metrics.add(new EvolutionRepurposedActionBasedGroupMetric<>(
				new GroupCenteredPercentDeletedMetric<Collaborator, Action>()));
		metrics.add(new EvolutionRepurposedActionBasedGroupMetric<>(
				new GroupCenteredPercentAddedMetric<Collaborator, Action>()));
		metrics.add(new EvolutionRepurposedActionBasedGroupMetric<>(
				new MessageCenteredPercentDeletedMetric<Collaborator, Action>()));
		metrics.add(new EvolutionRepurposedActionBasedGroupMetric<>(
				new MessageCenteredPercentAddedMetric<Collaborator, Action>()));
		metrics.add(new EvolutionRepurposedActionBasedGroupMetric<>(
				new RecommendationsMatchedToTestActionMetric<Collaborator, Action>()));
		metrics.add(new EvolutionRepurposedActionBasedGroupMetric<>(
				new RecommendationsToTestActionPerfectMatchesMetric<Collaborator, Action>()));
		metrics.add(new EvolutionRepurposedActionBasedGroupMetric<>(
				new TestActionsMatchedToRecommendationMetric<Collaborator, Action>()));
		metrics.add(new EvolutionRepurposedActionBasedGroupMetric<>(
				new TestActionsToRecommendationPerfectMatchesMetric<Collaborator, Action>()));
		metrics.add(new PercentRecommendedCreationRateMetric<Collaborator, Action>());
		metrics.add(new PercentRecommendedEvolutionsRateMetric<Collaborator, Action>());
		metrics.add(new PercentRecommendedUnchangedRateMetric<Collaborator, Action>());
		metrics.add(new PercentMatchedRecommendedCreationRateMetric<Collaborator, Action>());
		metrics.add(new PercentMatchedRecommendedEvolutionsRateMetric<Collaborator, Action>());
		metrics.add(new PercentMatchedRecommendedUnchangedRateMetric<Collaborator, Action>());
	}
	
	private static String getHalfLifeName(long halfLife) {
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

	private boolean graphsAreDifferent(
			UndirectedGraph<Collaborator, DefaultEdge> oldGraph,
			UndirectedGraph<Collaborator, DefaultEdge> newGraph) {
		if (oldGraph == null || newGraph == null) {
			return oldGraph != null || newGraph != null;
		}
		if (!(oldGraph.vertexSet().containsAll(newGraph.vertexSet()))) {
			return true;
		}

		for (DefaultEdge edge : oldGraph.edgeSet()) {
			Collaborator src = oldGraph.getEdgeSource(edge);
			Collaborator tgt = oldGraph.getEdgeTarget(edge);
			if (!newGraph.containsEdge(src, tgt)) {
				return true;
			}
		}

		for (DefaultEdge edge : newGraph.edgeSet()) {
			Collaborator src = newGraph.getEdgeSource(edge);
			Collaborator tgt = newGraph.getEdgeTarget(edge);
			if (!oldGraph.containsEdge(src, tgt)) {
				return true;
			}
		}
		return false;
	}

	private ActionBasedGraphBuilder<Collaborator, Action> creatGraphBuilder(
			ConstantValues constantSets,
			ActionBasedGraphBuilderFactory<Collaborator, Action> graphBuilderFactory) {

		if (graphBuilderFactory.takesScoredEdgeWithThreshold()) {
			double wOut = (Double) constantSets.constants[0];
			long halfLife = (Long) constantSets.constants[1];
			double scoreThreshold = (Double) constantSets.constants[2];
			return graphBuilderFactory.create(halfLife, wOut, scoreThreshold);
		} else if (graphBuilderFactory.takesTime()) {
			long timeThreshold = (Long) constantSets.constants[0];
			return graphBuilderFactory.create(timeThreshold);
		} else {
			return graphBuilderFactory.create();
		}
	}
	
	private UndirectedGraph<Collaborator, DefaultEdge> buildGraph(
			ConstantValues constantSets,
			Collection<Action> actions,
			ActionBasedGraphBuilderFactory<Collaborator, Action> graphBuilderFactory) {

		ActionBasedGraphBuilder<Collaborator, Action> graphBuilder = creatGraphBuilder(
				constantSets, graphBuilderFactory);

		ArrayList<Action> pastActions = new ArrayList<>(actions);
		Collections.sort(pastActions);
		Action currentAction = pastActions.remove(pastActions.size() - 1);
		return graphBuilder.addActionToGraph(null, currentAction, pastActions);
	}
	
	private Collection<ConstantValues> getConstantSets(ActionBasedGraphBuilderFactory<Collaborator, Action> graphBuilderFactory) {
		if (graphBuilderFactory.takesScoredEdgeWithThreshold()) {
			return graphConstants.get(InteractionRankWeightedActionBasedGraphBuilder.class);
		} else if (graphBuilderFactory.takesTime()) {
			return graphConstants.get(TimeThresholdActionBasedGraphBuilder.class);
		} else {
			return graphConstants.get(SimpleActionBasedGraphBuilder.class);
		}
		
	}

	private Collection<MetricResult> collectResults(
			Collection<Action> testActions,
			UndirectedGraph<Collaborator, DefaultEdge> oldGraph,
			UndirectedGraph<Collaborator, DefaultEdge> newGraph,
			Collection<Set<Collaborator>> newSeedlessRecommendations,
			SeedlessGroupRecommenderFactory<Collaborator> seedlessRecommenderFactory,
			GroupEvolutionRecommenderFactory<Collaborator> evolutionRecommendationFactory) {

		Collection<Set<Collaborator>> oldSeedlessRecommendations = seedlessRecommenderFactory
				.create(oldGraph).getRecommendations();
		GroupEvolutionRecommender<Collaborator> evolutionRecommender = evolutionRecommendationFactory
				.create(seedlessRecommenderFactory, newSeedlessRecommendations);
		Collection<RecommendedEvolution<Collaborator>> recommendations = evolutionRecommender
				.generateRecommendations(oldGraph, newGraph,
						oldSeedlessRecommendations);

		ActionBasedEvolutionGroupRecommendationAcceptanceModeler<Collaborator, Action> modeler =
				new ActionBasedEvolutionGroupRecommendationAcceptanceModeler<>(
						oldGraph, newGraph, recommendations, testActions, metrics);
		
		return modeler.modelRecommendationAcceptance();
	}
	
	private String getPrefix(
			ConstantValues constants,
			ActionBasedGraphBuilder<Collaborator, Action> graphBuilder,
			GroupEvolutionRecommender<Collaborator> evolutionRecommender) {
	
		String prefix = evolutionRecommender.getTypeOfRecommender() + ","+ graphBuilder.getName() + ",";
		if (constants.constants.length == 0) {
			prefix += "N/A,N/A,N/A";
		} else if (constants.constants.length == 1) {
			long timeThreshold = (Long) constants.constants[0];
			prefix += "N/A,N/A," + getHalfLifeName(timeThreshold);
		} else if (constants.constants.length == 1) {
			double wOut = (Double) constants.constants[0];
			long halfLife = (Long) constants.constants[1];
			double scoreThreshold = (Double) constants.constants[2];
			prefix += wOut + "," + getHalfLifeName(halfLife) + "," + scoreThreshold;
		}
		return prefix;
	}

	private void testAcrossGraphSizes(
			Id account,
			MetricResultCollection<Id> resultsCollection,
			ConstantValues constants,
			Collection<Action> trainActions,
			Collection<Action> testActions,
			SeedlessGroupRecommenderFactory<Collaborator> seedlessRecommenderFactory,
			ActionBasedGraphBuilderFactory<Collaborator, Action> graphBuilderFactory,
			GroupEvolutionRecommenderFactory<Collaborator> evolutionRecommendationFactory) throws IOException {

		UndirectedGraph<Collaborator, DefaultEdge> newGraph = buildGraph(
				constants, trainActions, graphBuilderFactory);
		SeedlessGroupRecommender<Collaborator> seedlessRecommender = seedlessRecommenderFactory.create(newGraph);
		Collection<Set<Collaborator>> newSeedlessRecommendations = seedlessRecommender.getRecommendations();

		List<Action> trainActionsList = new ArrayList<>(trainActions);
		Collections.sort(trainActionsList);

		UndirectedGraph<Collaborator, DefaultEdge> oldGraph = null;
		for (int i = trainActionsList.size() - 1; i >= 0; i--) {
			trainActionsList.remove(i);

			UndirectedGraph<Collaborator, DefaultEdge> candidateOldGraph = buildGraph(
					constants, trainActionsList, graphBuilderFactory);
			if (graphsAreDifferent(candidateOldGraph, oldGraph)
					&& graphsAreDifferent(oldGraph, newGraph)) {
				
				oldGraph = candidateOldGraph;
				
				Set<Collaborator> oldCollaborators = new HashSet<>(oldGraph.vertexSet());
				Set<Collaborator> newCollaborators = new HashSet<>(newGraph.vertexSet());
				newCollaborators.removeAll(oldCollaborators);
				double vertexGrowthRate = ((double) newCollaborators.size())
						/ oldCollaborators.size();
				if (vertexGrowthRate > MAX_VERTEX_GROWTH_RATE) {
					break;
				}
				
				Collection<MetricResult> results = collectResults(testActions, oldGraph, newGraph, newSeedlessRecommendations, seedlessRecommenderFactory, evolutionRecommendationFactory);

				ActionBasedGraphBuilder<Collaborator, Action> graphBuilder = creatGraphBuilder(
						constants, graphBuilderFactory);
				GroupEvolutionRecommender<Collaborator> evolutionRecommender = evolutionRecommendationFactory
						.create(seedlessRecommenderFactory, newSeedlessRecommendations);
				String prefix = getPrefix(constants, graphBuilder, evolutionRecommender);
				resultsCollection.addResults(prefix, account, results);
			}
		}
	}
	
	private void testAcrossFactories(
			Id account,
			MetricResultCollection<Id> resultsCollection,
			Collection<Action> trainActions,
			Collection<Action> testActions) throws IOException {
		
		for (GroupEvolutionRecommenderFactory<Collaborator> evolutionFactory : evolutionRecommenderFactories) {
			for (SeedlessGroupRecommenderFactory<Collaborator> seedlessFactory : seedlessGroupRecommenderFactories) {
				for(ActionBasedGraphBuilderFactory<Collaborator, Action> graphBuilderFactory : graphBuilderFactories) {
					Collection<ConstantValues> constantSets = getConstantSets(graphBuilderFactory);
					for (ConstantValues constantSet : constantSets) {
						testAcrossGraphSizes(account, resultsCollection, constantSet, trainActions, testActions, seedlessFactory, graphBuilderFactory, evolutionFactory);
					}
				}
			}
		}
	}
	
	public void runTestbed() throws IOException {
		for (ActionsDataSet<Id, Collaborator, Action, ActionThread> dataset : datasets) {
			File metricResultsFile = dataset.getEvolutionMetricsFile();
			
			String headerPrefix = "evolution recommender,graph builder,w_out,half_life,threshold,account";
			Collection<Metric> tempMetrics = new ArrayList<>();
			for (Metric metric : metrics) {
				tempMetrics.add(metric);
			}
			MetricResultCollection<Id> resultsCollection = new MetricResultCollection<Id>(headerPrefix, tempMetrics, metricResultsFile);
			
			for (Id account : dataset.getAccountIds()) {
				Collection<Action> train = dataset.getTrainMessages(account, PERCENT_TRAINING);
				Collection<Action> test = dataset.getTestMessages(account, PERCENT_TRAINING);
				
				testAcrossFactories(account, resultsCollection, train, test);
			}
		}
	}
}
