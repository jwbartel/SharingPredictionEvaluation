package metrics.groups.actionbased.bursty;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import metrics.DoubleResult;
import metrics.MetricResult;
import data.representation.actionbased.CollaborativeAction;

public class MessagesTriggeringGroupCreationMetric<Collaborator, Action extends CollaborativeAction<Collaborator>>
		implements BurstyGroupMetric<Collaborator, Action> {

	int totalMessages = 0;
	int totalMessagesWithRecommendations;
	
	public static <Collaborator, Action extends CollaborativeAction<Collaborator>> BurstyGroupMetricFactory<Collaborator, Action> factory() {
		return new BurstyGroupMetricFactory<Collaborator, Action>() {

			@Override
			public BurstyGroupMetric<Collaborator, Action> create() {
				return new MessagesTriggeringGroupCreationMetric<>();
			}
		};
	}
	
	@Override
	public MetricResult evaluate(Collection<Set<Collaborator>> recommendations,
			Collection<Set<Collaborator>> ideals,
			Collection<Action> testActions,
			Map<Set<Collaborator>, Set<Collaborator>> recommendationsToIdeals,
			Map<Set<Collaborator>, Action> recommendationsToTestActions,
			Map<Action, Set<Collaborator>> testActionsToRecommendations) {

		double percentage = ((double) totalMessagesWithRecommendations)
				/ ((double) totalMessages);
		return new DoubleResult(percentage);
	}

	@Override
	public String getHeader() {
		return "messages triggering group recommendation";
	}

	@Override
	public void recordBurstyRecommendation(Action currAction,
			Collection<Set<Collaborator>> recommendations) {
		totalMessages++;
		if (recommendations != null && recommendations.size() > 0) {
			totalMessagesWithRecommendations++;
		}
	}

}
