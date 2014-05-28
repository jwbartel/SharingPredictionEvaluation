package metrics.groups.actionbased.bursty;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import metrics.MetricResult;
import metrics.groups.actionbased.ActionBasedGroupMetric;
import data.representation.actionbased.CollaborativeAction;

public class RepurposedActionBasedGroupMetric<Collaborator, Action extends CollaborativeAction<Collaborator>>
		implements BurstyGroupMetric<Collaborator, Action> {

	private final ActionBasedGroupMetric<Collaborator, Action> actionBasedMetric;

	public static <Collaborator, Action extends CollaborativeAction<Collaborator>> BurstyGroupMetricFactory<Collaborator, Action> factory(
			final ActionBasedGroupMetric<Collaborator, Action> actionBasedMetric) {
		return new BurstyGroupMetricFactory<Collaborator, Action>() {

			@Override
			public BurstyGroupMetric<Collaborator, Action> create() {
				return new RepurposedActionBasedGroupMetric<>(actionBasedMetric);
			}
		};
	}

	public RepurposedActionBasedGroupMetric(
			ActionBasedGroupMetric<Collaborator, Action> actionBasedMetric) {
		this.actionBasedMetric = actionBasedMetric;
	}

	@Override
	public MetricResult evaluate(Collection<Set<Collaborator>> recommendations,
			Collection<Set<Collaborator>> ideals,
			Collection<Action> testActions,
			Map<Set<Collaborator>, Set<Collaborator>> recommendationsToIdeals,
			Map<Set<Collaborator>, Action> recommendationsToTestActions,
			Map<Action, Set<Collaborator>> testActionsToRecommendations) {
		return actionBasedMetric.evaluate(recommendations, ideals, testActions,
				recommendationsToIdeals, recommendationsToTestActions,
				testActionsToRecommendations);
	}

	@Override
	public String getHeader() {
		return actionBasedMetric.getHeader();
	}

	@Override
	public void recordBurstyRecommendation(Action currAction,
			Collection<Set<Collaborator>> recommendations) {
		// Do nothing
	}

}
