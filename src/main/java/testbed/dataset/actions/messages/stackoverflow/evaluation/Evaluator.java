package testbed.dataset.actions.messages.stackoverflow.evaluation;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import metrics.MetricResult;

public interface Evaluator {

	public String getType();

	
	public abstract Collection<Integer> getTestIds() throws IOException;
	public List<MetricResult> evaluate(Integer testIds);
}
