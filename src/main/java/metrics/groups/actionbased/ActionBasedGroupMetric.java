package metrics.groups.actionbased;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import recommendation.general.actionbased.CollaborativeAction;
import metrics.MetricResult;

public interface ActionBasedGroupMetric<V, ActionType extends CollaborativeAction<V>> {

	public MetricResult evaluate(
			Map<Set<V>, Set<V>> recommendationsToIdeals,
			Collection<Set<V>> unusedRecommendations,
			Collection<Set<V>> unusedIdeals,
			Collection<ActionType> testActions,
			Map<ActionType, Set<V>> testActionsToRecommendations);
	
}
