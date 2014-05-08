package testbed;

import groups.evolution.GroupEvolutionRecommender;
import groups.evolution.GroupEvolutionRecommenderFactory;
import groups.evolution.composed.ComposedGroupEvolutionRecommenderFactory;
import groups.evolution.fullrecommendation.FullRecommendationGroupEvolutionRecommenderFactory;
import groups.evolution.recommendations.RecommendedEvolution;
import groups.seedless.SeedlessGroupRecommender;
import groups.seedless.SeedlessGroupRecommenderFactory;
import groups.seedless.fellows.FellowsRecommenderFactory;
import groups.seedless.hybrid.HybridRecommenderFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import metrics.Metric;
import metrics.MetricResult;
import metrics.MetricResultCollection;
import metrics.groups.distance.AddsAndDeletesGroupDistance;
import metrics.groups.distance.GoodnessGroupDistance;
import metrics.groups.distance.GroupDistanceMetric;
import metrics.groups.distance.JaccardGroupDistance;
import metrics.groups.evolution.GroupEvolutionMetric;
import metrics.groups.evolution.MissedIdealSizes;
import metrics.groups.evolution.PercentMissedCreatedIdeals;
import metrics.groups.evolution.PercentMissedEvolvedIdeals;
import metrics.groups.evolution.PercentMissedIdeals;
import metrics.groups.evolution.PercentUnusedChangeRecommendations;
import metrics.groups.evolution.PercentUnusedCreationRecommendations;
import metrics.groups.evolution.PercentUnusedRecommendations;
import metrics.groups.evolution.RelativeAdditions;
import metrics.groups.evolution.RelativeChangeAdditions;
import metrics.groups.evolution.RelativeChangeDeletions;
import metrics.groups.evolution.RelativeCreationAdditions;
import metrics.groups.evolution.RelativeCreationDeletions;
import metrics.groups.evolution.RelativeDeletions;
import model.recommendation.groups.EvolutionGroupRecommendationAcceptanceModeler;
import model.tools.evolution.MembershipChangeFinder;

import org.apache.commons.io.FileUtils;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import testbed.dataset.GroupDataSet;
import testbed.dataset.SnapGroupDataSet;

public class GroupEvolutionRecommendationTestBed {

	static double[] growthRates = {0.01, 0.02, 0.03, 0.04, 0.05, 0.06, 0.07, 0.08, 0.09, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9};
	static int[] testIds = {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19};
	static Collection<GroupDataSet> dataSets = new ArrayList<GroupDataSet>();
	static Collection<GroupDistanceMetric<Integer>> distanceMetrics = new ArrayList<GroupDistanceMetric<Integer>>();
	static Collection<GroupEvolutionRecommenderFactory<Integer>> evolutionRecommenderFactories = new ArrayList<GroupEvolutionRecommenderFactory<Integer>>();
	static Collection<SeedlessGroupRecommenderFactory<Integer>> seedlessRecommenderFactories = new ArrayList<SeedlessGroupRecommenderFactory<Integer>>();

	static Collection<GroupEvolutionMetric<Integer>> metrics = new ArrayList<GroupEvolutionMetric<Integer>>();
	
	static {
		
		//Add data sets
		int[] snapAccounts = {0, 348, 414, 686, 698, 1684, 3437, 3980};
		dataSets.add(new SnapGroupDataSet("snap_facebook", snapAccounts, new File("data/Stanford_snap/facebook")));
		
		//Add similarity metrics
		distanceMetrics.add(new GoodnessGroupDistance<Integer>());
		distanceMetrics.add(new AddsAndDeletesGroupDistance<Integer>());
		distanceMetrics.add(new JaccardGroupDistance<Integer>());
		
		//Add evolution recommender factories
		evolutionRecommenderFactories.add(new ComposedGroupEvolutionRecommenderFactory<Integer>());
		evolutionRecommenderFactories.add(new FullRecommendationGroupEvolutionRecommenderFactory<Integer>());
		
		//Add seedless recommender factories
		seedlessRecommenderFactories.add(new HybridRecommenderFactory<Integer>());
		seedlessRecommenderFactories.add(new FellowsRecommenderFactory<Integer>());
		
		//Add metrics
		metrics.add(new PercentMissedIdeals<Integer>());
		metrics.add(new MissedIdealSizes<Integer>());
		metrics.add(new PercentMissedEvolvedIdeals<Integer>());
		metrics.add(new PercentMissedCreatedIdeals<Integer>());
		metrics.add(new PercentUnusedRecommendations<Integer>());
		metrics.add(new PercentUnusedChangeRecommendations<Integer>());
		metrics.add(new PercentUnusedCreationRecommendations<Integer>());
		metrics.add(new RelativeAdditions<Integer>());
		metrics.add(new RelativeDeletions<Integer>());
		metrics.add(new RelativeChangeAdditions<Integer>());
		metrics.add(new RelativeChangeDeletions<Integer>());
		metrics.add(new RelativeCreationAdditions<Integer>());
		metrics.add(new RelativeCreationDeletions<Integer>());
	}
	
	private static Collection<Set<Integer>> getNewlyCreatedGroups(
			Collection<Set<Integer>> idealGroups,
			Map<Set<Integer>, Collection<Set<Integer>>> oldToNewIdeals) {
		
		Collection<Set<Integer>> newlyCreatedGroups = new HashSet<>(idealGroups);
		for(Collection<Set<Integer>> evolvedGroups : oldToNewIdeals.values()) {
			newlyCreatedGroups.retainAll(evolvedGroups);
		}
		return newlyCreatedGroups;
	}
	
	private static UndirectedGraph<Integer, DefaultEdge> createOldGraph(
			UndirectedGraph<Integer, DefaultEdge> graph, Set<Integer> newMembers) {

		UndirectedGraph<Integer, DefaultEdge> oldGraph = new SimpleGraph<>(DefaultEdge.class);
		for(Integer vertex : graph.vertexSet()) {
			if (!newMembers.contains(vertex)) {
				oldGraph.addVertex(vertex);
			}
		}
		for (DefaultEdge edge : graph.edgeSet()) {
			Integer source = graph.getEdgeSource(edge);
			Integer target = graph.getEdgeTarget(edge);
			if (!newMembers.contains(source) && !newMembers.contains(target)) {
				oldGraph.addEdge(source, target);
			}
		}
		return oldGraph;
	}
	
	
	public static void main(String[] args) throws IOException {
		
		for (GroupDataSet dataset : dataSets) {
			String headerPrefix = "evolution-type,distance measure,growth rate,test,account";
			MetricResultCollection<Integer> resultCollection = new MetricResultCollection<Integer>(headerPrefix, new ArrayList<Metric>(metrics));
			for (int accountId : dataset.getAccountIds()) {
				
				UndirectedGraph<Integer, DefaultEdge> graph = dataset.getGraph(accountId);
				Collection<Set<Integer>> idealGroups = dataset.getIdealGroups(accountId);
				Collection<Set<Integer>> maximalCliques = dataset.getMaximalCliques(accountId);
				
				
				for(SeedlessGroupRecommenderFactory<Integer> recommenderFactory : seedlessRecommenderFactories) {
					SeedlessGroupRecommender<Integer> seedlessRecommender = recommenderFactory.create(graph, maximalCliques);
					Collection<Set<Integer>> seedlessRecommendations = seedlessRecommender.getRecommendations();
					
					for (GroupEvolutionRecommenderFactory<Integer> evolutionRecommenderFactory : evolutionRecommenderFactories) {

						GroupEvolutionRecommender<Integer> evolutionRecommender = evolutionRecommenderFactory.create(recommenderFactory, seedlessRecommendations);
						
						for(double growthRate : growthRates) {
							for (int test : testIds) {

								Set<Integer> newMembers = dataset.getNewMembers(accountId, growthRate, test);

								MembershipChangeFinder<Integer> changeFinder = new MembershipChangeFinder<>();
								Map<Set<Integer>,Collection<Set<Integer>>> oldToNewIdeals = changeFinder.getUnmaintainedToMaintainedGroups(idealGroups, newMembers);

								UndirectedGraph<Integer, DefaultEdge> oldGraph = createOldGraph(graph, newMembers);
								Collection<RecommendedEvolution<Integer>> evolutionRecommendations =
										evolutionRecommender.generateRecommendations(oldGraph, graph, oldToNewIdeals.keySet());
								Collection<Set<Integer>> newlyCreatedIdealGroups = getNewlyCreatedGroups(idealGroups, oldToNewIdeals);
								
								for (GroupDistanceMetric<Integer> distanceMetric : distanceMetrics) {

									EvolutionGroupRecommendationAcceptanceModeler<Integer> modeler = new EvolutionGroupRecommendationAcceptanceModeler<Integer>(
											distanceMetric, evolutionRecommendations, oldToNewIdeals, newlyCreatedIdealGroups, metrics);
									
									Collection<MetricResult> results = modeler.modelRecommendationAcceptance();
									String rowLabel = evolutionRecommender.getTypeOfRecommender() + "-" + seedlessRecommender.getTypeOfRecommender();
									rowLabel += "," + distanceMetric.getDistanceName();
									rowLabel += "," + growthRate;
									rowLabel += "," + test;
									resultCollection.addResults(rowLabel, accountId, results);

								}

							}
						}
					}			
					
				}
				
			}
			
			FileUtils.write(dataset.getEvolutionMetricsFile(), resultCollection.toString());
		}
		
	}

}
