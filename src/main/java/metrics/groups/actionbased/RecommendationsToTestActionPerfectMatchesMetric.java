package metrics.groups.actionbased;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import metrics.DoubleResult;
import metrics.MetricResult;
import recommendation.general.actionbased.CollaborativeAction;

public class RecommendationsToTestActionPerfectMatchesMetric<CollaboratorType, ActionType extends CollaborativeAction<CollaboratorType>>
		implements ActionBasedGroupMetric<CollaboratorType, ActionType> {
	
	@Override
	public MetricResult evaluate(
			Collection<Set<CollaboratorType>> recommendations,
			Collection<Set<CollaboratorType>> ideals,
			Collection<ActionType> testActions,
			Map<Set<CollaboratorType>, Set<CollaboratorType>> recommendationsToIdeals,
			Map<Set<CollaboratorType>, ActionType> recommendationsToTestActions,
			Map<ActionType, Set<CollaboratorType>> testActionsToRecommendations) {

		int numPerfectMatches = 0;

		for (Set<CollaboratorType> recommendation : recommendations) {
			for (ActionType testAction : testActions) {

				Collection<CollaboratorType> collaborators = testAction
						.getCollaborators();
				if (collaborators.containsAll(recommendation)
						&& recommendation.containsAll(collaborators)) {
					numPerfectMatches++;
					break;
				}
			}
		}
		
		return new DoubleResult(((double) numPerfectMatches)
				/ testActions.size());
	}

	@Override
	public String getHeader() {
		return "recommendations perfectly matched to test actions";
	}

}
