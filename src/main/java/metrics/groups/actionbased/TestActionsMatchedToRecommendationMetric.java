package metrics.groups.actionbased;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import data.representation.actionbased.CollaborativeAction;
import metrics.DoubleResult;
import metrics.MetricResult;

public class TestActionsMatchedToRecommendationMetric<CollaboratorType, ActionType extends CollaborativeAction<CollaboratorType>>
		implements ActionBasedGroupMetric<CollaboratorType, ActionType> {
	
	@Override
	public MetricResult evaluate(
			Collection<Set<CollaboratorType>> recommendations,
			Collection<Set<CollaboratorType>> ideals,
			Collection<ActionType> testActions,
			Map<Set<CollaboratorType>, Set<CollaboratorType>> recommendationsToIdeals,
			Map<Set<CollaboratorType>, ActionType> recommendationsToTestActions,
			Map<ActionType, Set<CollaboratorType>> testActionsToRecommendations) {

		return new DoubleResult(((double) testActionsToRecommendations.keySet()
				.size()) / testActions.size());
	}

	@Override
	public String getHeader() {
		return "test actions matched to recommendations";
	}

}
