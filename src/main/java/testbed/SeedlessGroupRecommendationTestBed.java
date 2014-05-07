package testbed;

import groups.seedless.SeedlessGroupRecommender;
import groups.seedless.SeedlessGroupRecommenderFactory;
import groups.seedless.fellows.FellowsRecommenderFactory;
import groups.seedless.kelli.HybridRecommenderFactory;
import groups.seedless.kelli.IOFunctions;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import metrics.MetricResult;
import metrics.groups.GroupMetric;
import metrics.groups.distance.AddsAndDeletesGroupDistance;
import metrics.groups.distance.GoodnessGroupDistance;
import metrics.groups.distance.GroupDistanceMetric;
import metrics.groups.distance.JaccardGroupDistance;
import model.recommendation.groups.SeedlessGroupRecommenationAcceptanceModeler;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;

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
		
		//TODO: add metrics
	}
	
	
	public static void main(String[] args) {
		
		for (GroupDataSet dataset : dataSets) {
			for (int accountId : dataset.getAccountIds()) {
				
				UndirectedGraph<Integer, DefaultEdge> graph = dataset.getGraph(accountId);
				Collection<Set<Integer>> idealGroups = dataset.getIdealGroups(accountId);
				Collection<Set<Integer>> maximalCliques = dataset.getMaximalCliques(accountId);
				
				for(SeedlessGroupRecommenderFactory<Integer> recommenderFactory : recommenderFactories) {
					SeedlessGroupRecommender<Integer> recommender = recommenderFactory.create(graph, maximalCliques);
					Collection<Set<Integer>> recommendations = recommender.getRecommendations();
					for (GroupDistanceMetric<Integer> similarityMetric : similarityMetrics) {

						SeedlessGroupRecommenationAcceptanceModeler<Integer> modeler = new SeedlessGroupRecommenationAcceptanceModeler<>(
								similarityMetric, recommendations, idealGroups,
								metrics);

						Collection<MetricResult> results = modeler.modelRecommendationAcceptance();
						//TODO: print results
						
					}
				}
				
			}
		}
		
	}

}
