package metrics.groups.actionbased.bursty;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import metrics.MetricResult;
import metrics.StatisticsResult;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import data.representation.actionbased.CollaborativeAction;

public class RecommendationsCreatedPerBurstMetric<Collaborator, Action extends CollaborativeAction<Collaborator>>
		implements BurstyGroupMetric<Collaborator, Action> {

	DescriptiveStatistics stats = new DescriptiveStatistics();

	public static <Collaborator, Action extends CollaborativeAction<Collaborator>> BurstyGroupMetricFactory<Collaborator, Action> factory(
			Class<Collaborator> collaboratorClass, Class<Action> actionClass) {
		return new BurstyGroupMetricFactory<Collaborator, Action>() {

			@Override
			public BurstyGroupMetric<Collaborator, Action> create() {
				return new RecommendationsCreatedPerBurstMetric<>();
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

		return new StatisticsResult(stats);
	}

	@Override
	public String getHeader() {
		return "avg-recommendations per burst,stdev-recommendations per burst";
	}

	@Override
	public void recordBurstyRecommendation(Action currAction,
			Collection<Set<Collaborator>> recommendations) {
		if (recommendations != null && recommendations.size() > 0) {
			stats.addValue(recommendations.size());
		}
	}

}
