package testbed;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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
import metrics.groups.actionbased.evolution.VertexGrowthRateMetric;
import recommendation.groups.evolution.GroupEvolutionRecommenderFactory;
import recommendation.groups.evolution.composed.ComposedGroupEvolutionRecommenderFactory;
import recommendation.groups.evolution.fullrecommendation.FullRecommendationGroupEvolutionRecommenderFactory;
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

	Collection<ActionsDataSet<Id,Collaborator, Action, ActionThread>> datasets;

	Collection<GroupEvolutionRecommenderFactory<Collaborator>> evolutionRecommenderFactories;
	Collection<SeedlessGroupRecommenderFactory<Collaborator>> seedlessGroupRecommenderFactories;
	Collection<ActionBasedGraphBuilderFactory<Collaborator, Action>> graphBuilderFactories;
	
	Map<Class<? extends ActionBasedGraphBuilder<Collaborator,Action>>, Collection<ConstantValues>> graphConstants = new HashMap<>();
	
	Collection<ActionBasedGroupEvolutionMetric<Collaborator, Action>> metrics;
	
	public ActionBasedEvolutionTestbed(Collection<ActionsDataSet<Id,Collaborator,Action,ActionThread>> datasets,
			Map<Class<? extends ActionBasedGraphBuilder<Collaborator,Action>>, Collection<ConstantValues>> graphConstants,
			Class<Collaborator> collaboratorClass,
			Class<Action> actionClass) {
		this.datasets = datasets;
		this.graphConstants = graphConstants;
		init(collaboratorClass, actionClass);
	}

	private void init(Class<Collaborator> collaboratorClass,
			Class<Action> actionClass) {
		
		evolutionRecommenderFactories = new ArrayList<>();
		evolutionRecommenderFactories.add(new FullRecommendationGroupEvolutionRecommenderFactory<Collaborator>());
		evolutionRecommenderFactories.add(new ComposedGroupEvolutionRecommenderFactory<Collaborator>());
		
		seedlessGroupRecommenderFactories = new ArrayList<>();
		seedlessGroupRecommenderFactories.add(new HybridRecommenderFactory<Collaborator>());
		
		graphBuilderFactories = new ArrayList<>();
		graphBuilderFactories.add(SimpleActionBasedGraphBuilder.factory(collaboratorClass, actionClass));
		graphBuilderFactories.add(TimeThresholdActionBasedGraphBuilder.factory(collaboratorClass, actionClass));
		graphBuilderFactories.add(InteractionRankWeightedActionBasedGraphBuilder.factory(collaboratorClass, actionClass));
		
		metrics = new ArrayList<>();
		metrics.add(new VertexGrowthRateMetric<Collaborator, Action>());
		metrics.add(new EdgeGrowthRateMetric<Collaborator, Action>());
		metrics.add(new EvolutionRepurposedActionBasedGroupMetric<>(new GroupCenteredPercentDeletedMetric<Collaborator,Action>()));
		metrics.add(new EvolutionRepurposedActionBasedGroupMetric<>(new GroupCenteredPercentAddedMetric<Collaborator,Action>()));
		metrics.add(new EvolutionRepurposedActionBasedGroupMetric<>(new MessageCenteredPercentDeletedMetric<Collaborator,Action>()));
		metrics.add(new EvolutionRepurposedActionBasedGroupMetric<>(new MessageCenteredPercentAddedMetric<Collaborator,Action>()));
		metrics.add(new EvolutionRepurposedActionBasedGroupMetric<>(new RecommendationsMatchedToTestActionMetric<Collaborator,Action>()));
		metrics.add(new EvolutionRepurposedActionBasedGroupMetric<>(new RecommendationsToTestActionPerfectMatchesMetric<Collaborator,Action>()));
		metrics.add(new EvolutionRepurposedActionBasedGroupMetric<>(new TestActionsMatchedToRecommendationMetric<Collaborator,Action>()));
		metrics.add(new EvolutionRepurposedActionBasedGroupMetric<>(new TestActionsToRecommendationPerfectMatchesMetric<Collaborator,Action>()));
	}
	
	// TODO: add parts for running each type of graph builder and each type of evolution recommender factory
}
