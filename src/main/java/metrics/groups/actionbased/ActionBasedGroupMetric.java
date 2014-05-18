package metrics.groups.actionbased;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import data.representation.actionbased.CollaborativeAction;
import metrics.Metric;
import metrics.MetricResult;

public interface ActionBasedGroupMetric<CollaboratorType, ActionType extends CollaborativeAction<CollaboratorType>>
		extends Metric {
	
	public MetricResult evaluate(
			Collection<Set<CollaboratorType>> recommendations,
			Collection<Set<CollaboratorType>> ideals,
			Collection<ActionType> testActions,
			Map<Set<CollaboratorType>, Set<CollaboratorType>> recommendationsToIdeals,
			Map<Set<CollaboratorType>, ActionType> recommendationsToTestActions,
			Map<ActionType, Set<CollaboratorType>> testActionsToRecommendations);
	
}
