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
import metrics.groups.similarity.AddsAndDeletesGroupSimilarity;
import metrics.groups.similarity.GoodnessGroupSimilarity;
import metrics.groups.similarity.GroupSimilarityMetric;
import metrics.groups.similarity.JaccardGroupSimilarity;
import model.recommendation.groups.SeedlessGroupRecommenationAcceptanceModeler;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;

public class SeedlessGroupRecommendationTestBed {

	static Collection<GroupMetric<Integer>> metrics = new ArrayList<GroupMetric<Integer>>();
	static Collection<GroupSimilarityMetric<Integer>> similarityMetrics = new ArrayList<GroupSimilarityMetric<Integer>>();
	static Collection<SeedlessGroupRecommenderFactory<Integer>> recommenderFactories = new ArrayList<SeedlessGroupRecommenderFactory<Integer>>();
	
	static {
		
		//TODO: add metrics
		
		//Add similarity metrics
		similarityMetrics.add(new GoodnessGroupSimilarity<Integer>());
		similarityMetrics.add(new AddsAndDeletesGroupSimilarity<Integer>());
		similarityMetrics.add(new JaccardGroupSimilarity<Integer>());
		
		//Add recommender factories
		recommenderFactories.add(new HybridRecommenderFactory<Integer>());
		recommenderFactories.add(new FellowsRecommenderFactory<Integer>());
	}
	
	
	public static void main(String[] args) {
		IOFunctions<Integer> ioHelp = new IOFunctions<>(Integer.class);
		
		File graphLocation = new File(""); //TODO: specify graph location
		File idealsLocation = new File(""); //TODO: specify ideal location
		File maximalCliquesLocation = new File(""); //TODO: specify maximal cliqueLocation
		
		UndirectedGraph<Integer, DefaultEdge> graph = ioHelp.createUIDGraph(graphLocation.getAbsolutePath());
		Collection<Set<Integer>> idealGroups = ioHelp.loadCliqueIDs(maximalCliquesLocation.getAbsolutePath());
		Collection<Set<Integer>> maximalCliques = ioHelp.loadCliqueIDs(maximalCliquesLocation.getAbsolutePath());
		
		for(SeedlessGroupRecommenderFactory<Integer> recommenderFactory : recommenderFactories) {
			SeedlessGroupRecommender<Integer> recommender = recommenderFactory.create(graph, maximalCliques);
			for (GroupSimilarityMetric<Integer> similarityMetric : similarityMetrics) {
				
				SeedlessGroupRecommenationAcceptanceModeler<Integer> modeler = new SeedlessGroupRecommenationAcceptanceModeler<>(
						similarityMetric, recommender, idealGroups, metrics);
				
				Collection<MetricResult> results = modeler.modelRecommendationAcceptance();
				//TODO: print results
				
			}
		}
		
	}

}
