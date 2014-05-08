package testbed;

import groups.seedless.SeedlessGroupRecommender;
import groups.seedless.SeedlessGroupRecommenderFactory;
import groups.seedless.fellows.FellowsRecommenderFactory;
import groups.seedless.kelli.HybridRecommenderFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
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

import bus.thunderbird.FileIO;
import testbed.dataset.GroupDataSet;
import testbed.dataset.SnapGroupDataSet;

public class SeedlessGroupRecommendationTestBed {

	static Collection<GroupDataSet> dataSets = new ArrayList<GroupDataSet>();
	static Collection<GroupDistanceMetric<Integer>> similarityMetrics = new ArrayList<GroupDistanceMetric<Integer>>();
	static Collection<SeedlessGroupRecommenderFactory<Integer>> recommenderFactories = new ArrayList<SeedlessGroupRecommenderFactory<Integer>>();

	static Collection<GroupMetric<Integer>> metrics = new ArrayList<GroupMetric<Integer>>();
	
	static {
		
		//Add data sets
		int[] snapAccounts = {0, 348, 414, 686, 698, 1684, 3437, 3980};
		dataSets.add(new SnapGroupDataSet("snap_facebook", snapAccounts, new File("data/Stanford_snap/facebook")));
		
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
		
		for (GroupDataSet dataset : dataSets) {
			MetricResultCollection<Integer> resultCollection = new MetricResultCollection<Integer>("type,account", new ArrayList<Metric>(metrics));
			for (int accountId : dataset.getAccountIds()) {
				
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
			
			FileUtils.write(dataset.getSeedlessMetricsFile(), resultCollection.toString());
		}
		
	}

}
