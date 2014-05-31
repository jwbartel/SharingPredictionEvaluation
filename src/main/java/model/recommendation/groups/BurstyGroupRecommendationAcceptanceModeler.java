package model.recommendation.groups;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import metrics.MetricResult;
import metrics.groups.actionbased.ActionBasedGroupMetric;
import metrics.groups.actionbased.bursty.BurstyGroupMetric;
import recommendation.groups.seedless.actionbased.bursty.BurstyGroupRecommender;
import data.representation.actionbased.CollaborativeAction;

public class BurstyGroupRecommendationAcceptanceModeler<Collaborator extends Comparable<Collaborator>, Action extends CollaborativeAction<Collaborator>>
		extends
		ActionBasedSeedlessGroupRecommendationAcceptanceModeler<Collaborator, Action> {

	BurstyGroupRecommender<Collaborator> recommender;
	Collection<Action> trainingActions;
	Collection<Action> testActions;
	Collection<Set<Collaborator>> idealGroups;
	Collection<BurstyGroupMetric<Collaborator, Action>> metrics;

	public BurstyGroupRecommendationAcceptanceModeler(
			BurstyGroupRecommender<Collaborator> recommender,
			Collection<Action> trainingActions, Collection<Action> testActions,
			Collection<Set<Collaborator>> idealGroups,
			Collection<BurstyGroupMetric<Collaborator, Action>> metrics) {
		super(new ArrayList<Set<Collaborator>>(0), idealGroups, testActions,
				new ArrayList<ActionBasedGroupMetric<Collaborator, Action>>(0));
		this.recommender = recommender;
		this.trainingActions = trainingActions;
		this.testActions = testActions;
		this.idealGroups = idealGroups;
		this.metrics = metrics;
	}

	@Override
	public Collection<MetricResult> modelRecommendationAcceptance() {

		Collection<Set<Collaborator>> allRecommendations = new HashSet<>();
		Map<Set<Collaborator>, Set<Collaborator>> recommendationsToIdeals = new HashMap<>();
		Map<Action, Set<Collaborator>> actionsToRecommendations = new HashMap<>();
		Map<Set<Collaborator>, Action> recommendationsToAction = new HashMap<>();

		for (Action trainingAction : trainingActions) {
			recommender.addPastAction(trainingAction);
		}

		Collection<Action> futureActions = new HashSet<>(testActions);
		for (Action testAction : testActions) {
			recommender.addPastAction(testAction);
			Collection<Set<Collaborator>> recommendations = recommender
					.getRecommendations();
			allRecommendations.addAll(recommendations);
			for (BurstyGroupMetric<Collaborator, Action> metric : metrics) {
				metric.recordBurstyRecommendation(testAction, recommendations);
			}
			futureActions.remove(testAction);
			for (Set<Collaborator> recommendation : recommendations) {

				// Match recommendation to ideal
				Set<Collaborator> bestIdeal = matchRecommendationToIdeal(
						recommendation, new TreeSet<Set<Collaborator>>());
				if (bestIdeal != null) {
					recommendationsToIdeals.put(recommendation, bestIdeal);
				}

				// Match recommendation to testMessage
				Action bestAction = matchRecommendationToAction(recommendation,
						futureActions);
				if (bestAction != null) {
					recommendationsToAction.put(recommendation, bestAction);
				}

				for (Action futureAction : futureActions) {
					Set<Collaborator> pastBestRecommendation = actionsToRecommendations
							.get(futureAction);
					Set<Collaborator> bestRecommendation = matchActionToRecommendation(
							futureAction, pastBestRecommendation,
							recommendation);
					if (bestRecommendation != null
							&& !bestRecommendation
									.equals(pastBestRecommendation)) {
						actionsToRecommendations.put(futureAction,
								bestRecommendation);
					}
				}
			}
		}

		Collection<MetricResult> results = new ArrayList<MetricResult>();
		for (BurstyGroupMetric<Collaborator, Action> metric : metrics) {
			results.add(metric.evaluate(allRecommendations, idealGroups,
					testActions, recommendationsToIdeals,
					recommendationsToAction, actionsToRecommendations));
		}
		return results;
	}
}
