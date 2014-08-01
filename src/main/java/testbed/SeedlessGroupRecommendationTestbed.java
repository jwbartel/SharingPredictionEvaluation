package testbed;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import metrics.Metric;
import metrics.MetricResult;
import metrics.MetricResultCollection;
import metrics.groups.GroupMetric;
import metrics.groups.MissedIdealSizes;
import metrics.groups.PercentMissedIdeals;
import metrics.groups.PercentUnusedRecommendations;
import metrics.groups.RelativeRequiredAdds;
import metrics.groups.RelativeRequiredDeletes;
import metrics.groups.distance.AddsAndDeletesGroupDistance;
import metrics.groups.distance.GoodnessGroupDistance;
import metrics.groups.distance.GroupDistanceMetric;
import metrics.groups.distance.JaccardGroupDistance;
import model.recommendation.groups.SeedlessGroupRecommenationAcceptanceModeler;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import recommendation.groups.seedless.SeedlessGroupRecommender;
import recommendation.groups.seedless.SeedlessGroupRecommenderFactory;
import recommendation.groups.seedless.fellows.FellowsRecommenderFactory;
import recommendation.groups.seedless.hybrid.HybridRecommenderFactory;
import testbed.dataset.group.GroupDataSet;

public class SeedlessGroupRecommendationTestbed<Id, Collaborator extends Comparable<Collaborator>> {

	Collection<GroupDataSet<Id, Collaborator>> datasets;

	Collection<GroupDistanceMetric<Collaborator>> distanceMetrics = new ArrayList<>();
	Collection<SeedlessGroupRecommenderFactory<Collaborator>> seedlessRecommenderFactories = new ArrayList<>();

	Collection<GroupMetric<Collaborator>> metrics = new ArrayList<>();

	public SeedlessGroupRecommendationTestbed(
			Collection<GroupDataSet<Id, Collaborator>> datasets) {
		this.datasets = datasets;
		init();
	}

	private void init() {

		// Add distance metrics
		distanceMetrics.add(new GoodnessGroupDistance<Collaborator>());
		distanceMetrics.add(new AddsAndDeletesGroupDistance<Collaborator>());
		distanceMetrics.add(new JaccardGroupDistance<Collaborator>());

		// Add recommender factories
		seedlessRecommenderFactories
				.add(new HybridRecommenderFactory<Collaborator>());
		seedlessRecommenderFactories
				.add(new FellowsRecommenderFactory<Collaborator>());

		// Add metrics
		metrics.add(new PercentUnusedRecommendations<Collaborator>());
		metrics.add(new PercentMissedIdeals<Collaborator>());
		metrics.add(new MissedIdealSizes<Collaborator>());
		metrics.add(new RelativeRequiredAdds<Collaborator>());
		metrics.add(new RelativeRequiredDeletes<Collaborator>());

	}

	public void runTestbed() throws IOException {

		for (GroupDataSet<Id, Collaborator> dataset : datasets) {
			MetricResultCollection<Id> resultCollection = new MetricResultCollection<Id>(
					"type,account", new ArrayList<Metric>(metrics),
					dataset.getSeedlessMetricsFile());
			for (Id accountId : dataset.getAccountIds()) {

				UndirectedGraph<Collaborator, DefaultEdge> graph = dataset
						.getGraph(accountId);
				Collection<Set<Collaborator>> idealGroups = dataset
						.getIdealGroups(accountId);
				Collection<Set<Collaborator>> maximalCliques = dataset
						.getMaximalCliques(accountId);

				for (SeedlessGroupRecommenderFactory<Collaborator> recommenderFactory : seedlessRecommenderFactories) {
					SeedlessGroupRecommender<Collaborator> recommender = recommenderFactory
							.create(graph, maximalCliques);
					Collection<Set<Collaborator>> recommendations = recommender
							.getRecommendations();

					dataset.writeGroupPredictions(
							recommender.getTypeOfRecommender(), accountId,
							recommendations);

					for (GroupDistanceMetric<Collaborator> distanceMetric : distanceMetrics) {

						SeedlessGroupRecommenationAcceptanceModeler<Collaborator> modeler = new SeedlessGroupRecommenationAcceptanceModeler<>(
								distanceMetric, recommendations, idealGroups,
								metrics);

						Collection<MetricResult> results = modeler
								.modelRecommendationAcceptance();
						resultCollection.addResults(
								recommender.getTypeOfRecommender() + "-"
										+ distanceMetric.getDistanceName(),
								accountId, results);

					}
				}

			}
		}

	}

}
