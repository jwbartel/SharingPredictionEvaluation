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

public class MessageCenteredPercentDeletedMetric<CollaboratorType, ActionType extends CollaborativeAction<CollaboratorType>>
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
		
		for (ActionType action : testActions) {
			Set<CollaboratorType> collaborators = new HashSet<>(
					action.getCollaborators());
			Set<CollaboratorType> recommendation = testActionsToRecommendations
					.get(action);
			
			if (recommendation != null) {
				stats.addValue(GroupMetric.relativeRequiredDeletions(collaborators,
						recommendation));
			}
			
		}
		for (Entry<ActionType, Set<CollaboratorType>> entry : testActionsToRecommendations
				.entrySet()) {
			
			ActionType action = entry.getKey();
			Set<CollaboratorType> collaborators = new HashSet<>(
					action.getCollaborators());
			Set<CollaboratorType> recommendation = entry.getValue();

			stats.addValue(GroupMetric.relativeRequiredDeletions(collaborators,
					recommendation));
		}
		
		return new StatisticsResult(stats);
	}

	@Override
	public String getHeader() {
		return "avg-message-centered percent deleted,stdev-message-centered percent deleted";
	}

}
