package testbed;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import metrics.Metric;
import metrics.MetricResult;
import metrics.MetricResultCollection;
import metrics.groups.distance.AddsAndDeletesGroupDistance;
import metrics.groups.distance.GoodnessGroupDistance;
import metrics.groups.distance.GroupDistanceMetric;
import metrics.groups.distance.JaccardGroupDistance;
import metrics.groups.evolution.Additions;
import metrics.groups.evolution.AdditionsRelativeToManual;
import metrics.groups.evolution.Deletions;
import metrics.groups.evolution.DeletionsRelativeToManual;
import metrics.groups.evolution.EvolutionAdditions;
import metrics.groups.evolution.EvolutionAdditionsRelativeToManual;
import metrics.groups.evolution.EvolutionDeletions;
import metrics.groups.evolution.EvolutionDeletionsRelativeToManual;
import metrics.groups.evolution.GroupEvolutionMetric;
import metrics.groups.evolution.ManualAdditions;
import metrics.groups.evolution.ManualAdditionsToCreateGroups;
import metrics.groups.evolution.ManualDeletions;
import metrics.groups.evolution.MissedIdealSizes;
import metrics.groups.evolution.NumIdeals;
import metrics.groups.evolution.NumNewMembers;
import metrics.groups.evolution.PercentEvolvedIdeals;
import metrics.groups.evolution.PercentMissedCreatedIdeals;
import metrics.groups.evolution.PercentMissedEvolvedIdeals;
import metrics.groups.evolution.PercentMissedIdeals;
import metrics.groups.evolution.PercentMissedUnchangedIdeals;
import metrics.groups.evolution.PercentNewlyCreatedIdeals;
import metrics.groups.evolution.PercentUnchangedIdeals;
import metrics.groups.evolution.PercentUnusedChangeRecommendations;
import metrics.groups.evolution.PercentUnusedCreationRecommendations;
import metrics.groups.evolution.PercentUnusedRecommendations;
import model.recommendation.groups.EvolutionGroupRecommendationAcceptanceModeler;
import model.tools.evolution.MembershipChangeFinder;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import recommendation.groups.evolution.GroupEvolutionRecommender;
import recommendation.groups.evolution.GroupEvolutionRecommenderFactory;
import recommendation.groups.evolution.composed.ComposedGroupEvolutionRecommenderFactory;
import recommendation.groups.evolution.fullrecommendation.FullRecommendationGroupEvolutionRecommenderFactory;
import recommendation.groups.evolution.recommendations.RecommendedEvolution;
import recommendation.groups.seedless.SeedlessGroupRecommender;
import recommendation.groups.seedless.SeedlessGroupRecommenderFactory;
import recommendation.groups.seedless.hybrid.HybridRecommenderFactory;
import testbed.dataset.group.GroupDataSet;
import testbed.dataset.group.MixedInitiativeDataSet;
import testbed.dataset.group.SnapGroupDataSet;

public class FacebookGroupEvolutionRecommendationTestBed {

	static double[] growthRates = {0.01, 0.02, 0.03, 0.04, 0.05, 0.06, 0.07, 0.08, 0.09, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9};
	static int[] testIds = {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19};
	static Collection<GroupDataSet<Integer>> dataSets = new ArrayList<GroupDataSet<Integer>>();
	static Collection<GroupDistanceMetric<Integer>> distanceMetrics = new ArrayList<GroupDistanceMetric<Integer>>();
	static Collection<GroupEvolutionRecommenderFactory<Integer>> evolutionRecommenderFactories = new ArrayList<GroupEvolutionRecommenderFactory<Integer>>();
	static Collection<SeedlessGroupRecommenderFactory<Integer>> seedlessRecommenderFactories = new ArrayList<SeedlessGroupRecommenderFactory<Integer>>();

	static Collection<GroupEvolutionMetric<Integer>> metrics = new ArrayList<GroupEvolutionMetric<Integer>>();
	
	static {
		
		//Add data sets
		Integer[] snapAccounts = {0, 348, 414, 686, 698, 1684, 3437, 3980};
		dataSets.add(new SnapGroupDataSet("snap_facebook", snapAccounts, new File("data/Stanford_snap/facebook")));
		Integer[] mixedInitiativeAccounts = MixedInitiativeDataSet.DEFAULT_ACCOUNT_SET;
		dataSets.add(new MixedInitiativeDataSet("mixed_initiative",
				mixedInitiativeAccounts, new File("data/kelli")));
		
		//Add similarity metrics
		distanceMetrics.add(new GoodnessGroupDistance<Integer>());
		distanceMetrics.add(new AddsAndDeletesGroupDistance<Integer>());
		distanceMetrics.add(new JaccardGroupDistance<Integer>());
		
		//Add evolution recommender factories
		evolutionRecommenderFactories.add(new ComposedGroupEvolutionRecommenderFactory<Integer>());
		evolutionRecommenderFactories.add(new FullRecommendationGroupEvolutionRecommenderFactory<Integer>());
		
		//Add seedless recommender factories
		seedlessRecommenderFactories.add(new HybridRecommenderFactory<Integer>());
//		seedlessRecommenderFactories.add(new FellowsRecommenderFactory<Integer>());
		
		//Add metrics
		metrics.add(new NumNewMembers<Integer>());
		metrics.add(new NumIdeals<Integer>());
		metrics.add(new PercentUnchangedIdeals<Integer>());
		metrics.add(new PercentNewlyCreatedIdeals<Integer>());
		metrics.add(new PercentEvolvedIdeals<Integer>());
		metrics.add(new ManualAdditions<Integer>());
		metrics.add(new ManualDeletions<Integer>());
		metrics.add(new ManualAdditionsToCreateGroups<Integer>());
		metrics.add(new Additions<Integer>());
		metrics.add(new Deletions<Integer>());
		metrics.add(new AdditionsRelativeToManual<Integer>());
		metrics.add(new DeletionsRelativeToManual<Integer>());
		metrics.add(new EvolutionAdditions<Integer>());
		metrics.add(new EvolutionDeletions<Integer>());
		metrics.add(new EvolutionAdditionsRelativeToManual<Integer>());
		metrics.add(new EvolutionDeletionsRelativeToManual<Integer>());
		metrics.add(new PercentMissedIdeals<Integer>());
		metrics.add(new MissedIdealSizes<Integer>());
		metrics.add(new PercentMissedUnchangedIdeals<Integer>());
		metrics.add(new PercentMissedEvolvedIdeals<Integer>());
		metrics.add(new PercentMissedCreatedIdeals<Integer>());
		metrics.add(new PercentUnusedRecommendations<Integer>());
		metrics.add(new PercentUnusedChangeRecommendations<Integer>());
		metrics.add(new PercentUnusedCreationRecommendations<Integer>());
	}
	
	private static Collection<Set<Integer>> getNewlyCreatedGroups(
			Collection<Set<Integer>> idealGroups,
			Map<Set<Integer>, Collection<Set<Integer>>> oldToNewIdeals) {
		
		Collection<Set<Integer>> newlyCreatedGroups = new HashSet<>(idealGroups);
		Collection<Set<Integer>> evolvedGroups = new HashSet<>();
		Collection<Set<Integer>> emptyOldGroups = new ArrayList<>();
		for(Entry<Set<Integer>,Collection<Set<Integer>>> entry : oldToNewIdeals.entrySet()) {
			Set<Integer> oldGroup = entry.getKey();
			if (oldGroup.size() == 0) {
				emptyOldGroups.add(oldGroup);
			} else {
				evolvedGroups.addAll(entry.getValue());
			}
		}
		newlyCreatedGroups.removeAll(evolvedGroups);
		for (Set<Integer> emptyOldGroup : emptyOldGroups) {
			oldToNewIdeals.remove(emptyOldGroup);
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
		
		for (GroupDataSet<Integer> dataset : dataSets) {
			String headerPrefix = "evolution-type,distance measure,growth rate,test,account";
			MetricResultCollection<Integer> resultCollection = new MetricResultCollection<Integer>(
					headerPrefix, new ArrayList<Metric>(metrics),dataset.getEvolutionMetricsFile());
			for (Integer accountId : dataset.getAccountIds()) {
				
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
								Collection<Set<Integer>> newlyCreatedIdealGroups = getNewlyCreatedGroups(idealGroups, oldToNewIdeals);
								Collection<RecommendedEvolution<Integer>> evolutionRecommendations =
										evolutionRecommender.generateRecommendations(oldGraph, graph, oldToNewIdeals.keySet());
								
								for (GroupDistanceMetric<Integer> distanceMetric : distanceMetrics) {

									EvolutionGroupRecommendationAcceptanceModeler<Integer> modeler = new EvolutionGroupRecommendationAcceptanceModeler<Integer>(
											newMembers, distanceMetric, evolutionRecommendations, oldToNewIdeals, newlyCreatedIdealGroups, metrics);
									
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
		}
		
	}

}
