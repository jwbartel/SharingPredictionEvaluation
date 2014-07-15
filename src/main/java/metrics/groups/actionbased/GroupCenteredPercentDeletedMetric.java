package metrics.groups.actionbased;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import metrics.MetricResult;
import metrics.StatisticsResult;
import metrics.groups.GroupMetric;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import data.representation.actionbased.CollaborativeAction;

public class GroupCenteredPercentDeletedMetric<CollaboratorType, ActionType extends CollaborativeAction<CollaboratorType>>
		implements ActionBasedGroupMetric<CollaboratorType, ActionType> {
	
	@Override
	public MetricResult evaluate(
			Collection<Set<CollaboratorType>> recommendations,
			Collection<Set<CollaboratorType>> ideals,
			Collection<ActionType> testActions,
			Map<Set<CollaboratorType>, Set<CollaboratorType>> recommendationsToIdeals,
			Map<Set<CollaboratorType>, ActionType> recommendationsToTestActions,
			Map<ActionType, Set<CollaboratorType>> testActionsToRecommendations) {

		DescriptiveStatistics stats = new DescriptiveStatistics();
		for (Set<CollaboratorType> recommendation : recommendations) {

			ActionType action = recommendationsToTestActions.get(recommendation);
			
			if (action != null) {

				Set<CollaboratorType> collaborators = new HashSet<>(
						action.getCollaborators());
				stats.addValue(GroupMetric.relativeRequiredDeletions(
						collaborators, recommendation));
			}
		}
		
		return new StatisticsResult(stats);
	}

	@Override
	public String getHeader() {
		return "avg-group-centered percent deleted,stdev-group-centered percent deleted";
	}

}
