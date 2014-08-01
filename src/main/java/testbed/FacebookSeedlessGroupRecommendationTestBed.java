package testbed;

import java.io.File;
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

import org.apache.commons.io.FileUtils;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import recommendation.groups.seedless.SeedlessGroupRecommender;
import recommendation.groups.seedless.SeedlessGroupRecommenderFactory;
import recommendation.groups.seedless.fellows.FellowsRecommenderFactory;
import recommendation.groups.seedless.hybrid.HybridRecommenderFactory;
import testbed.dataset.group.GroupDataSet;
import testbed.dataset.group.MixedInitiativeDataSet;
import testbed.dataset.group.SnapGroupDataSet;

public class FacebookSeedlessGroupRecommendationTestBed {

	static Collection<GroupDataSet<Integer>> dataSets = new ArrayList<GroupDataSet<Integer>>();
	static Collection<GroupDistanceMetric<Integer>> similarityMetrics = new ArrayList<GroupDistanceMetric<Integer>>();
	static Collection<SeedlessGroupRecommenderFactory<Integer>> recommenderFactories = new ArrayList<SeedlessGroupRecommenderFactory<Integer>>();

	static Collection<GroupMetric<Integer>> metrics = new ArrayList<GroupMetric<Integer>>();
	
	static {
		
		//Add data sets
		Integer[] snapAccounts = SnapGroupDataSet.DEFAULT_ACCOUNT_SET;
		dataSets.add(new SnapGroupDataSet("snap_facebook", snapAccounts,
				new File("data/Stanford_snap/facebook")));
		Integer[] mixedInitiativeAccounts = MixedInitiativeDataSet.DEFAULT_ACCOUNT_SET;
		dataSets.add(new MixedInitiativeDataSet("mixed_initiative",
				mixedInitiativeAccounts, new File("data/kelli")));
		
		//Add similarity metrics
		similarityMetrics.add(new GoodnessGroupDistance<Integer>());
		similarityMetrics.add(new AddsAndDeletesGroupDistance<Integer>());
		similarityMetrics.add(new JaccardGroupDistance<Integer>());
		
		//Add recommender factories
		recommenderFactories.add(new HybridRecommenderFactory<Integer>());
		recommenderFactories.add(new FellowsRecommenderFactory<Integer>());
		
		//Add metrics
		metrics.add(new PercentUnusedRecommendations<Integer>());
		metrics.add(new PercentMissedIdeals<Integer>());
		metrics.add(new MissedIdealSizes<Integer>());
		metrics.add(new RelativeRequiredAdds<Integer>());
		metrics.add(new RelativeRequiredDeletes<Integer>());
	}
	
	
	public static void main(String[] args) throws IOException {
		
		for (GroupDataSet<Integer> dataset : dataSets) {
			MetricResultCollection<Integer> resultCollection = new MetricResultCollection<Integer>(
					"type,account", new ArrayList<Metric>(metrics),
					dataset.getSeedlessMetricsFile());
			for (Integer accountId : dataset.getAccountIds()) {
				
				UndirectedGraph<Integer, DefaultEdge> graph = dataset.getGraph(accountId);
				Collection<Set<Integer>> idealGroups = dataset.getIdealGroups(accountId);
				Collection<Set<Integer>> maximalCliques = dataset.getMaximalCliques(accountId);
				
				for(SeedlessGroupRecommenderFactory<Integer> recommenderFactory : recommenderFactories) {
					SeedlessGroupRecommender<Integer> recommender = recommenderFactory.create(graph, maximalCliques);
					Collection<Set<Integer>> recommendations = recommender.getRecommendations();
					
					dataset.writeGroupPredictions(recommender.getTypeOfRecommender(), accountId, recommendations);
					
					for (GroupDistanceMetric<Integer> similarityMetric : similarityMetrics) {

						SeedlessGroupRecommenationAcceptanceModeler<Integer> modeler = new SeedlessGroupRecommenationAcceptanceModeler<>(
								similarityMetric, recommendations, idealGroups,
								metrics);

						Collection<MetricResult> results = modeler.modelRecommendationAcceptance();
						resultCollection.addResults(recommender.getTypeOfRecommender() + "-" + similarityMetric.getDistanceName(), accountId, results);
						
					}
				}
				
			}
		}
		
	}

}
