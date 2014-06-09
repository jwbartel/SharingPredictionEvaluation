package testbed.dataset.actions.messages.stackoverflow.evaluation;

import java.util.List;

import metrics.MetricResult;

public interface Evaluator {

	public List<MetricResult> evaluate();
}
