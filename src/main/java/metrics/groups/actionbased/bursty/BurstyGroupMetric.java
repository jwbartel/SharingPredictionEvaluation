package metrics.groups.actionbased.bursty;

import java.util.Collection;
import java.util.Set;

import metrics.groups.actionbased.ActionBasedGroupMetric;
import data.representation.actionbased.CollaborativeAction;

public interface BurstyGroupMetric<Collaborator, Action extends CollaborativeAction<Collaborator>>
		extends ActionBasedGroupMetric<Collaborator, Action> {

	public void recordBurstyRecommendation(Action currAction,
			Collection<Set<Collaborator>> recommendations);

}
