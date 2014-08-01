package testbed;

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
import data.preprocess.graphbuilder.ActionBasedGraphBuilder;
import data.preprocess.graphbuilder.ActionBasedGraphBuilderFactory;
import data.preprocess.graphbuilder.InteractionRankWeightedActionBasedGraphBuilder;
import data.preprocess.graphbuilder.SimpleActionBasedGraphBuilder;
import data.preprocess.graphbuilder.TimeThresholdActionBasedGraphBuilder;
import data.representation.actionbased.CollaborativeAction;
import data.representation.actionbased.CollaborativeActionThread;

public class ActionBasedBurstyGroupCreationTestBed<Id, Collaborator extends Comparable<Collaborator>, Action extends CollaborativeAction<Collaborator>, ActionThread extends CollaborativeActionThread<Collaborator, Action>> {


	static final double PERCENT_TRAINING = 0.8;
	
	Collection<ActionsDataSet<Id, Collaborator, Action, ActionThread>> datasets = new ArrayList<>();

	Collection<SeedlessGroupRecommenderFactory<Collaborator>> seedlessRecommenderFactories = new ArrayList<>();
	Collection<ActionBasedGraphBuilderFactory<Collaborator, Action>> graphBuilderFactories = new ArrayList<>();

	Map<String, Collection<ConstantValues>> graphConstants = new HashMap<>();

	Collection<Double> distanceThresholds = new ArrayList<>();

	Collection<BurstyGroupMetricFactory<Collaborator, Action>> metricFactories = new ArrayList<>();
	
	public ActionBasedBurstyGroupCreationTestBed(
			Collection<ActionsDataSet<Id, Collaborator, Action, ActionThread>> datasets,
			Map<String, Collection<ConstantValues>> graphConstants,
			Class<Collaborator> collaboratorClass, Class<Action> actionClass) {
		this.datasets = datasets;
		this.graphConstants = graphConstants;
		init(collaboratorClass, actionClass);
	}

	private void init(Class<Collaborator> collaboratorClass,
			Class<Action> actionClass) {

		// Add seedless recommender factories
		seedlessRecommenderFactories.add(new HybridRecommenderFactory<Collaborator>(false));
		//seedlessRecommenderFactories.add(new FellowsRecommenderFactory<Collaborator>());

		// Add graph builders
		graphBuilderFactories.add(SimpleActionBasedGraphBuilder.factory(collaboratorClass, actionClass));
		graphBuilderFactories.add(TimeThresholdActionBasedGraphBuilder.factory(collaboratorClass, actionClass));
		graphBuilderFactories.add(InteractionRankWeightedActionBasedGraphBuilder.factory(collaboratorClass, actionClass));

		// Add metrics
		metricFactories.add(MessagesTriggeringGroupCreationMetric.factory(collaboratorClass, actionClass));
		metricFactories.add(RecommendationsCreatedPerBurstMetric.factory(collaboratorClass, actionClass));
		metricFactories.add(RepurposedActionBasedGroupMetric.factory(new TotalTestActionsMetric<Collaborator,Action>()));
		metricFactories.add(RepurposedActionBasedGroupMetric.factory(new TotalRecommendedGroupsMetric<Collaborator,Action>()));
		metricFactories.add(RepurposedActionBasedGroupMetric.factory(new GroupCenteredPercentDeletedMetric<Collaborator,Action>()));
		metricFactories.add(RepurposedActionBasedGroupMetric.factory(new GroupCenteredPercentAddedMetric<Collaborator,Action>()));
		metricFactories.add(RepurposedActionBasedGroupMetric.factory(new MessageCenteredPercentDeletedMetric<Collaborator,Action>()));
		metricFactories.add(RepurposedActionBasedGroupMetric.factory(new MessageCenteredPercentAddedMetric<Collaborator,Action>()));
		metricFactories.add(RepurposedActionBasedGroupMetric.factory(new TestActionsMatchedToRecommendationMetric<Collaborator,Action>()));
		metricFactories.add(RepurposedActionBasedGroupMetric.factory(new TestActionsToRecommendationPerfectMatchesMetric<Collaborator,Action>()));
		metricFactories.add(RepurposedActionBasedGroupMetric.factory(new RecommendationsMatchedToTestActionMetric<Collaborator,Action>()));
		metricFactories.add(RepurposedActionBasedGroupMetric.factory(new RecommendationsToTestActionPerfectMatchesMetric<Collaborator,Action>()));
		
		distanceThresholds.add(0.0);
		distanceThresholds.add(0.1);
		distanceThresholds.add(0.2);
		
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

	private  Collection<MetricResult> collectResults(
			Collection<Action> trainMessages,
			Collection<Action> testMessages,
			Double seedClosenessThreshold,
			Double recommendationClosenessThreshold,
			GraphFormingActionBasedSeedlessGroupRecommender<Collaborator, Action> recommender) {

		ArrayList<BurstyGroupMetric<Collaborator, Action>> metrics = new ArrayList<>();
		for (BurstyGroupMetricFactory<Collaborator, Action> metricFactory : metricFactories) {
			metrics.add(metricFactory.create());
		}

		BurstyGroupRecommender<Collaborator, Action> burstyRecommender = new BurstyGroupRecommender<>(
				recommender, new RelativeEditsThresholdMatcher<Collaborator>(
						seedClosenessThreshold),
				new RelativeEditsThresholdMatcher<Collaborator>(
						recommendationClosenessThreshold));
		BurstyGroupRecommendationAcceptanceModeler<Collaborator, Action> modeler = new BurstyGroupRecommendationAcceptanceModeler<Collaborator, Action>(
				burstyRecommender, trainMessages, testMessages,
				new ArrayList<Set<Collaborator>>(), metrics);

		Collection<MetricResult> results = modeler
				.modelRecommendationAcceptance();
		return results;
	}

	private void useGraphBuilderNoArgs(
			Id account,
			Collection<Action> trainMessages,
			Collection<Action> testMessages,
			SeedlessGroupRecommenderFactory<Collaborator> seedlessRecommenderFactory,
			ActionBasedGraphBuilderFactory<Collaborator, Action> graphBuilderFactory,
			MetricResultCollection<Id> resultCollection) throws IOException {

		ActionBasedGraphBuilder<Collaborator, Action> graphBuilder = graphBuilderFactory
				.create();

		GraphFormingActionBasedSeedlessGroupRecommender<Collaborator, Action> recommender = new GraphFormingActionBasedSeedlessGroupRecommender<>(
				seedlessRecommenderFactory, graphBuilder);
		
		Collection<ConstantValues> constantSets = graphConstants.get(graphBuilder.getClass().getName());
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

	private void useGraphBuilderTimeThreshold(
			Id account,
			Collection<Action> trainMessages,
			Collection<Action> testMessages,
			SeedlessGroupRecommenderFactory<Collaborator> seedlessRecommenderFactory,
			ActionBasedGraphBuilderFactory<Collaborator, Action> graphBuilderFactory,
			MetricResultCollection<Id> resultCollection) throws IOException {

		

		ActionBasedGraphBuilder<Collaborator, Action> tempGraphBuilder = graphBuilderFactory
				.create(0L);
		
		Collection<ConstantValues> constantSets = graphConstants.get(tempGraphBuilder.getClass().getName());
		for (ConstantValues constantSet : constantSets) {
			Long timeThreshold = (Long) constantSet.constants[0];
			
			ActionBasedGraphBuilder<Collaborator, Action> graphBuilder = graphBuilderFactory
					.create(timeThreshold);

			GraphFormingActionBasedSeedlessGroupRecommender<Collaborator, Action> recommender = new GraphFormingActionBasedSeedlessGroupRecommender<>(
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

	private void useGraphBuilderScoredEdges(
			Id account,
			Collection<Action> trainMessages,
			Collection<Action> testMessages,
			SeedlessGroupRecommenderFactory<Collaborator> seedlessRecommenderFactory,
			ActionBasedGraphBuilderFactory<Collaborator, Action> graphBuilderFactory,
			MetricResultCollection<Id> resultCollection) throws IOException {

		ActionBasedGraphBuilder<Collaborator, Action> tempGraphBuilder = graphBuilderFactory
				.create(0L, 0.0, 0.0);
		
		Collection<ConstantValues> constantSets = graphConstants.get(tempGraphBuilder.getClass().getName());
		for (ConstantValues constantSet : constantSets) {
			
			Double wOut = (Double) constantSet.constants[0];
			Long halfLife = (Long) constantSet.constants[1];
			Double scoreThreshold = (Double) constantSet.constants[2];
			
			ActionBasedGraphBuilder<Collaborator, Action> graphBuilder = graphBuilderFactory
					.create((long) halfLife, wOut, scoreThreshold);

			GraphFormingActionBasedSeedlessGroupRecommender<Collaborator, Action> recommender = new GraphFormingActionBasedSeedlessGroupRecommender<>(
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
	
	public void runTestbed() throws IOException {

		for (ActionsDataSet<Id, Collaborator, Action, ActionThread> dataset : datasets) {

			ArrayList<Metric> tempMetrics = new ArrayList<>();
			for (BurstyGroupMetricFactory<Collaborator, Action> metricFactory : metricFactories) {
				tempMetrics.add(metricFactory.create());
			}
			String headerPrefix = "graph builder,seed threshold,recommendation threshold,"
					+ "account,half_life,w_out,edge threshold";
			MetricResultCollection<Id> resultCollection = new MetricResultCollection<Id>(
					headerPrefix, tempMetrics,
					dataset.getBurstyGroupsMetricsFile());

			for (Id account : dataset.getAccountIds()) {
				System.out.println(account);

				Collection<Action> trainMessages = dataset
						.getTrainMessages(account, PERCENT_TRAINING);
				Collection<Action> testMessages = dataset
						.getTestMessages(account, PERCENT_TRAINING);

				for (SeedlessGroupRecommenderFactory<Collaborator> seedlessRecommenderFactory : seedlessRecommenderFactories) {
					for (ActionBasedGraphBuilderFactory<Collaborator, Action> graphBuilderFactory : graphBuilderFactories) {

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
