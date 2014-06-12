package testbed.dataset.actions.messages.stackoverflow.evaluation;

import java.util.List;

import metrics.MetricResult;

public interface Evaluator {

	public String getType();
	
	public List<MetricResult> evaluate();
}
