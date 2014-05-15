package metrics.groups.actionbased;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import recommendation.general.actionbased.CollaborativeAction;
import metrics.MetricResult;

public interface ActionBasedGroupMetric<V, ActionType extends CollaborativeAction<V>> {

	public MetricResult evaluate(
			Collection<Set<V>> recommendations,
			Collection<Set<V>> ideals,
			Collection<ActionType> testActions,
			Map<Set<V>, Set<V>> recommendationsToIdeals,
			Map<Set<V>, ActionType> recommendationsToTestActions,
			Map<ActionType, Set<V>> testActionsToRecommendations);
	
}
