package testbed;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

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
import recommendation.groups.evolution.GroupEvolutionRecommender;
import recommendation.groups.evolution.GroupEvolutionRecommenderFactory;
import recommendation.groups.evolution.composed.ComposedGroupEvolutionRecommenderFactory;
import recommendation.groups.evolution.fullrecommendation.FullRecommendationGroupEvolutionRecommenderFactory;
import recommendation.groups.evolution.recommendations.RecommendedEvolution;
import recommendation.groups.seedless.SeedlessGroupRecommender;
import recommendation.groups.seedless.SeedlessGroupRecommenderFactory;
import recommendation.groups.seedless.fellows.FellowsRecommenderFactory;
import recommendation.groups.seedless.hybrid.HybridRecommenderFactory;
import testbed.dataset.group.GroupDataSet;

public class GroupEvolutionTestbed<Collaborator extends Comparable<Collaborator>> {

	double[] growthRates = {0.01, 0.02, 0.03, 0.04, 0.05, 0.06, 0.07, 0.08, 0.09, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9};
	int[] testIds = {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19};
	
	Collection<GroupDataSet<Collaborator>> datasets;
	
	Collection<GroupDistanceMetric<Collaborator>> distanceMetrics = new ArrayList<>();
	Collection<GroupEvolutionRecommenderFactory<Collaborator>> evolutionRecommenderFactories = new ArrayList<>();
	Collection<SeedlessGroupRecommenderFactory<Collaborator>> seedlessRecommenderFactories = new ArrayList<>();
	
	Collection<GroupEvolutionMetric<Collaborator>> metrics = new ArrayList<>();
	
	public GroupEvolutionTestbed(Collection<GroupDataSet<Collaborator>> datasets) {
		this.datasets = datasets;
		init();
	}
	
	private void init() {
		//Add similarity metrics
		distanceMetrics.add(new GoodnessGroupDistance<Collaborator>());
		distanceMetrics.add(new AddsAndDeletesGroupDistance<Collaborator>());
		distanceMetrics.add(new JaccardGroupDistance<Collaborator>());

		//Add evolution recommender factories
		evolutionRecommenderFactories.add(new ComposedGroupEvolutionRecommenderFactory<Collaborator>());
		evolutionRecommenderFactories.add(new FullRecommendationGroupEvolutionRecommenderFactory<Collaborator>());

		//Add seedless recommender factories
		seedlessRecommenderFactories.add(new HybridRecommenderFactory<Collaborator>());
		seedlessRecommenderFactories.add(new FellowsRecommenderFactory<Collaborator>());

		//Add metrics
		metrics.add(new NumNewMembers<Collaborator>());
		metrics.add(new NumIdeals<Collaborator>());
		metrics.add(new PercentUnchangedIdeals<Collaborator>());
		metrics.add(new PercentNewlyCreatedIdeals<Collaborator>());
		metrics.add(new PercentEvolvedIdeals<Collaborator>());
		metrics.add(new ManualAdditions<Collaborator>());
		metrics.add(new ManualDeletions<Collaborator>());
		metrics.add(new ManualAdditionsToCreateGroups<Collaborator>());
		metrics.add(new Additions<Collaborator>());
		metrics.add(new Deletions<Collaborator>());
		metrics.add(new AdditionsRelativeToManual<Collaborator>());
		metrics.add(new DeletionsRelativeToManual<Collaborator>());
		metrics.add(new EvolutionAdditions<Collaborator>());
		metrics.add(new EvolutionDeletions<Collaborator>());
		metrics.add(new EvolutionAdditionsRelativeToManual<Collaborator>());
		metrics.add(new EvolutionDeletionsRelativeToManual<Collaborator>());
		metrics.add(new PercentMissedIdeals<Collaborator>());
		metrics.add(new MissedIdealSizes<Collaborator>());
		metrics.add(new PercentMissedUnchangedIdeals<Collaborator>());
		metrics.add(new PercentMissedEvolvedIdeals<Collaborator>());
		metrics.add(new PercentMissedCreatedIdeals<Collaborator>());
		metrics.add(new PercentUnusedRecommendations<Collaborator>());
		metrics.add(new PercentUnusedChangeRecommendations<Collaborator>());
		metrics.add(new PercentUnusedCreationRecommendations<Collaborator>());
	}
	
	private Collection<Set<Collaborator>> getNewlyCreatedGroups(
			Collection<Set<Collaborator>> idealGroups,
			Map<Set<Collaborator>, Collection<Set<Collaborator>>> oldToNewIdeals) {
		
		Collection<Set<Collaborator>> newlyCreatedGroups = new HashSet<>(idealGroups);
		Collection<Set<Collaborator>> evolvedGroups = new HashSet<>();
		Collection<Set<Collaborator>> emptyOldGroups = new ArrayList<>();
		for(Entry<Set<Collaborator>,Collection<Set<Collaborator>>> entry : oldToNewIdeals.entrySet()) {
			Set<Collaborator> oldGroup = entry.getKey();
			if (oldGroup.size() == 0) {
				emptyOldGroups.add(oldGroup);
			} else {
				evolvedGroups.addAll(entry.getValue());
			}
		}
		newlyCreatedGroups.removeAll(evolvedGroups);
		for (Set<Collaborator> emptyOldGroup : emptyOldGroups) {
			oldToNewIdeals.remove(emptyOldGroup);
		}
		return newlyCreatedGroups;
	}
	
	private UndirectedGraph<Collaborator, DefaultEdge> createOldGraph(
			UndirectedGraph<Collaborator, DefaultEdge> graph, Set<Collaborator> newMembers) {

		UndirectedGraph<Collaborator, DefaultEdge> oldGraph = new SimpleGraph<>(DefaultEdge.class);
		for(Collaborator vertex : graph.vertexSet()) {
			if (!newMembers.contains(vertex)) {
				oldGraph.addVertex(vertex);
			}
		}
		for (DefaultEdge edge : graph.edgeSet()) {
			Collaborator source = graph.getEdgeSource(edge);
			Collaborator target = graph.getEdgeTarget(edge);
			if (!newMembers.contains(source) && !newMembers.contains(target)) {
				oldGraph.addEdge(source, target);
			}
		}
		return oldGraph;
	}
	
	
	public void runTestbed() throws IOException {
		
		for (GroupDataSet<Collaborator> dataset : datasets) {
			String headerPrefix = "evolution-type,distance measure,growth rate,test,account";
			MetricResultCollection<Collaborator> resultCollection = new MetricResultCollection<Collaborator>(
					headerPrefix, new ArrayList<Metric>(metrics),dataset.getEvolutionMetricsFile());
			for (Collaborator accountId : dataset.getAccountIds()) {
				
				UndirectedGraph<Collaborator, DefaultEdge> graph = dataset.getGraph(accountId);
				Collection<Set<Collaborator>> idealGroups = dataset.getIdealGroups(accountId);
				Collection<Set<Collaborator>> maximalCliques = dataset.getMaximalCliques(accountId);
				
				
				for(SeedlessGroupRecommenderFactory<Collaborator> recommenderFactory : seedlessRecommenderFactories) {
					SeedlessGroupRecommender<Collaborator> seedlessRecommender = recommenderFactory.create(graph, maximalCliques);
					Collection<Set<Collaborator>> seedlessRecommendations = seedlessRecommender.getRecommendations();
					
					for (GroupEvolutionRecommenderFactory<Collaborator> evolutionRecommenderFactory : evolutionRecommenderFactories) {

						GroupEvolutionRecommender<Collaborator> evolutionRecommender = evolutionRecommenderFactory.create(recommenderFactory, seedlessRecommendations);
						
						for(double growthRate : growthRates) {
							for (int test : testIds) {

								Set<Collaborator> newMembers = dataset.getNewMembers(accountId, growthRate, test);

								MembershipChangeFinder<Collaborator> changeFinder = new MembershipChangeFinder<>();
								Map<Set<Collaborator>,Collection<Set<Collaborator>>> oldToNewIdeals = changeFinder.getUnmaintainedToMaintainedGroups(idealGroups, newMembers);

								UndirectedGraph<Collaborator, DefaultEdge> oldGraph = createOldGraph(graph, newMembers);
								Collection<Set<Collaborator>> newlyCreatedIdealGroups = getNewlyCreatedGroups(idealGroups, oldToNewIdeals);
								Collection<RecommendedEvolution<Collaborator>> evolutionRecommendations =
										evolutionRecommender.generateRecommendations(oldGraph, graph, oldToNewIdeals.keySet());
								
								for (GroupDistanceMetric<Collaborator> distanceMetric : distanceMetrics) {

									EvolutionGroupRecommendationAcceptanceModeler<Collaborator> modeler = new EvolutionGroupRecommendationAcceptanceModeler<Collaborator>(
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
