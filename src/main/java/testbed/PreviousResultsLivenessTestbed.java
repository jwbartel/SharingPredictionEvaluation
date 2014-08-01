package testbed;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import metrics.Metric;
import metrics.MetricResult;
import metrics.MetricResultCollection;
import metrics.response.liveness.AccuracyMetric;
import metrics.response.liveness.FalseNegativeRateMetric;
import metrics.response.liveness.FalsePositiveRateMetric;
import metrics.response.liveness.NegativePredictiveValueMetric;
import metrics.response.liveness.PositivePredictiveValueMetric;
import metrics.response.liveness.ResponseLivenessMetric;
import metrics.response.liveness.ResponseLivenessMetricFactory;
import testbed.dataset.actions.messages.MessageDataset;
import testbed.previousresults.ConstantPredictionLivenessEvaluator;
import testbed.previousresults.GradientAscentLivenessEvaluator;
import testbed.previousresults.LivenessEvaluator;
import testbed.previousresults.LivenessEvaluator.LivenessEvaluatorFactory;
import testbed.previousresults.RandomPredictionLivenessEvaluator;
import data.representation.actionbased.messages.MessageThread;
import data.representation.actionbased.messages.SingleMessage;

public class PreviousResultsLivenessTestbed<Id, Collaborator extends Comparable<Collaborator>, Message extends SingleMessage<Collaborator>, MsgThread extends MessageThread<Collaborator, Message>> {

	Collection<MessageDataset<Id, Collaborator, Message, MsgThread>> datasets;
	Collection<ResponseLivenessMetricFactory> metricFactories = new ArrayList<>();
	Collection<LivenessEvaluatorFactory<Id,Collaborator, Message, MsgThread>> evaluatorFactories = new ArrayList<>();

	public PreviousResultsLivenessTestbed(
			Collection<MessageDataset<Id, Collaborator, Message, MsgThread>> datasets,
			Class<Id> idClass,
			Class<Collaborator> collaboratorClass,
			Class<Message> messageClass,
			Class<MsgThread> threadClass) {

		this.datasets = datasets;
		init(idClass, collaboratorClass, messageClass, threadClass);
	}
	
	private void init(
			Class<Id> idClass,
			Class<Collaborator> collaboratorClass,
			Class<Message> messageClass,
			Class<MsgThread> threadClass) {

		metricFactories.add(AccuracyMetric.factory());
		metricFactories.add(FalsePositiveRateMetric.factory());
		metricFactories.add(FalseNegativeRateMetric.factory());
		metricFactories.add(PositivePredictiveValueMetric.factory());
		metricFactories.add(NegativePredictiveValueMetric.factory());
		
		evaluatorFactories.add(GradientAscentLivenessEvaluator.factory(idClass, collaboratorClass, messageClass, threadClass));
		evaluatorFactories.add(ConstantPredictionLivenessEvaluator.factory(idClass, collaboratorClass, messageClass, threadClass, "all dead", 0.0));
		evaluatorFactories.add(ConstantPredictionLivenessEvaluator.factory(idClass, collaboratorClass, messageClass, threadClass, "all live", 1.0));
		evaluatorFactories.add(RandomPredictionLivenessEvaluator.factory(idClass, collaboratorClass, messageClass, threadClass, "random", 0.5));
		evaluatorFactories.add(RandomPredictionLivenessEvaluator.factory(idClass, collaboratorClass, messageClass, threadClass, "distribution based", 0.8575));

	}
	
	public void runTestbed() throws IOException {
		for (MessageDataset<Id, Collaborator, Message, MsgThread> dataset : datasets) {
			
			Collection<Metric> unusedMetrics = new ArrayList<>();
			for (ResponseLivenessMetricFactory metricFactory : metricFactories) {
				unusedMetrics.add(metricFactory.create());
			}
			String headerPrefix = "type,test,account";
			MetricResultCollection<Id> resultCollection = new MetricResultCollection<Id>(
					headerPrefix, unusedMetrics,
					dataset.getPreviousResultsLivenessMetricsFile());
			for (LivenessEvaluatorFactory<Id, Collaborator, Message, MsgThread> evaluatorFactory : evaluatorFactories) {
				Collection<ResponseLivenessMetric> metrics = new ArrayList<>();
				for (ResponseLivenessMetricFactory metricFactory : metricFactories) {
					metrics.add(metricFactory.create());
				}
				LivenessEvaluator<Id, Collaborator, Message, MsgThread> evaluator = 
						evaluatorFactory.create(dataset, metrics);
				
				for (Integer testId : evaluator.getTestIds()) {
					List<MetricResult> results = evaluator.evaluate(testId);
					String label = evaluator.getType()+","+testId;
					System.out.println(label);
					resultCollection.addResults(label, null, results);
				}
			}
		}
	}
}
