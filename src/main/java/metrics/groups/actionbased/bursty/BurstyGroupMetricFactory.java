package metrics.groups.actionbased.bursty;

import data.representation.actionbased.CollaborativeAction;

public interface BurstyGroupMetricFactory<Collaborator, Action extends CollaborativeAction<Collaborator>>{

	public BurstyGroupMetric<Collaborator, Action> create();

}
