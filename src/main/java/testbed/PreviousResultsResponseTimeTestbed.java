package testbed;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import metrics.Metric;
import metrics.MetricResult;
import metrics.MetricResultCollection;
import metrics.response.time.AccuracyMetric;
import metrics.response.time.MinOrMaxResponseTimeMetric.MinOrMaxType;
import metrics.response.time.PercentAboveMinimumMetric;
import metrics.response.time.PercentBelowMaximumMetric;
import metrics.response.time.PercentInfiniteMaximumMetric;
import metrics.response.time.PercentWithinErrorThresholdMetric;
import metrics.response.time.PercentWithinRangeMetric;
import metrics.response.time.PercentZeroMinimum;
import metrics.response.time.RecallMetric;
import metrics.response.time.RelativeErrorMetric;
import metrics.response.time.ResponseTimeMetric;
import metrics.response.time.ResponseTimeMetricFactory;
import metrics.response.time.ScaleDifferenceMetric;
import prediction.response.time.InverseGaussianDistribution;
import prediction.response.time.LogNormalDistribution;
import testbed.dataset.actions.messages.MessageDataset;
import testbed.dataset.actions.messages.stackoverflow.evaluation.CollaborativeFilteringResponseTimeEvaluator;
import testbed.dataset.actions.messages.stackoverflow.evaluation.ConstantPredictionResponseTimeEvaluator;
import testbed.dataset.actions.messages.stackoverflow.evaluation.DistributionResponseTimeEvaluator;
import testbed.dataset.actions.messages.stackoverflow.evaluation.GradientAscentResponseTimeEvaluator;
import testbed.dataset.actions.messages.stackoverflow.evaluation.KmeansResponseTimeEvaluator;
import testbed.dataset.actions.messages.stackoverflow.evaluation.ResponseTimeEvaluator;
import testbed.dataset.actions.messages.stackoverflow.evaluation.ResponseTimeEvaluator.ResponseTimeEvaluatorFactory;
import testbed.dataset.actions.messages.stackoverflow.evaluation.SigmoidWeightedKmeansResponseTimeEvaluator;
import data.representation.actionbased.messages.MessageThread;
import data.representation.actionbased.messages.SingleMessage;

public class PreviousResultsResponseTimeTestbed<Id, Collaborator extends Comparable<Collaborator>, Message extends SingleMessage<Collaborator>, MsgThread extends MessageThread<Collaborator, Message>> {

	Collection<MessageDataset<Id, Collaborator, Message, MsgThread>> datasets;
	Collection<ResponseTimeMetricFactory> metricFactories = new ArrayList<>();
	Collection<ResponseTimeEvaluatorFactory<Id, Collaborator, Message, MsgThread>> evaluatorFactories = new ArrayList<>();

	public PreviousResultsResponseTimeTestbed(
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
		metricFactories.add(RecallMetric.factory());
		metricFactories.add(PercentWithinRangeMetric.factory());
		metricFactories.add(PercentZeroMinimum.factory());
		metricFactories.add(PercentInfiniteMaximumMetric.factory());
		metricFactories.add(PercentAboveMinimumMetric.factory());
		metricFactories.add(PercentBelowMaximumMetric.factory());
		metricFactories.add(AccuracyMetric.factory(MinOrMaxType.Minimum));
		metricFactories.add(AccuracyMetric.factory(MinOrMaxType.Maximum));
		metricFactories.add(RelativeErrorMetric.factory(MinOrMaxType.Minimum));
		metricFactories.add(RelativeErrorMetric.factory(MinOrMaxType.Maximum));
		metricFactories.add(ScaleDifferenceMetric.factory(MinOrMaxType.Minimum));
		metricFactories.add(ScaleDifferenceMetric.factory(MinOrMaxType.Maximum));
		metricFactories.add(PercentWithinErrorThresholdMetric.factory(MinOrMaxType.Minimum, "1 minute", 60.0));
		metricFactories.add(PercentWithinErrorThresholdMetric.factory(MinOrMaxType.Minimum, "3 minutes", 60*3.0));
		metricFactories.add(PercentWithinErrorThresholdMetric.factory(MinOrMaxType.Minimum, "5 minutes", 60*5.0));
		metricFactories.add(PercentWithinErrorThresholdMetric.factory(MinOrMaxType.Minimum, "10 minutes", 60*10.0));
		metricFactories.add(PercentWithinErrorThresholdMetric.factory(MinOrMaxType.Minimum, "20 minutes", 60*20.0));
		metricFactories.add(PercentWithinErrorThresholdMetric.factory(MinOrMaxType.Minimum, "1 hour", 3600.0));
		metricFactories.add(PercentWithinErrorThresholdMetric.factory(MinOrMaxType.Minimum, "1 day", 3600.0*24));
		metricFactories.add(PercentWithinErrorThresholdMetric.factory(MinOrMaxType.Minimum, "1 week", 3600.0*24*7));
		
		evaluatorFactories.add(ConstantPredictionResponseTimeEvaluator.factory(idClass, collaboratorClass, messageClass, threadClass, "1 minute", 60.0));
		evaluatorFactories.add(ConstantPredictionResponseTimeEvaluator.factory(idClass, collaboratorClass, messageClass, threadClass, "3 minutes", 3*60.0));
		evaluatorFactories.add(ConstantPredictionResponseTimeEvaluator.factory(idClass, collaboratorClass, messageClass, threadClass, "5 minutes", 5*60.0));
		evaluatorFactories.add(ConstantPredictionResponseTimeEvaluator.factory(idClass, collaboratorClass, messageClass, threadClass, "10 minutes", 10*60.0));
		evaluatorFactories.add(ConstantPredictionResponseTimeEvaluator.factory(idClass, collaboratorClass, messageClass, threadClass, "20 minutes", 20*60.0));
		evaluatorFactories.add(DistributionResponseTimeEvaluator.factory(idClass, collaboratorClass, messageClass, threadClass, new InverseGaussianDistribution(867.482, 571.108)));
		evaluatorFactories.add(DistributionResponseTimeEvaluator.factory(idClass, collaboratorClass, messageClass, threadClass, new LogNormalDistribution(6.35702, 0.927127)));
		
		evaluatorFactories.add(GradientAscentResponseTimeEvaluator.factory(idClass, collaboratorClass, messageClass, threadClass));

		for (int k=2; k<=25; k++) {
			evaluatorFactories.add(KmeansResponseTimeEvaluator.factory(idClass, collaboratorClass, messageClass, threadClass, k));
		}
		for (int k=2; k<=25; k++) {
			evaluatorFactories.add(SigmoidWeightedKmeansResponseTimeEvaluator.factory(idClass, collaboratorClass, messageClass, threadClass, k));
		}
		String[] collaborativeFilteringFeatureTypes = {"owner and tag", "owner and word", "owner and tag_word pair", "tag and word"};
		String[] collaborativeFilteringTypes = {"Random", "Euclidean", "PearsonCorrelation", "SlopeOne"};
		for (String featureType : collaborativeFilteringFeatureTypes) {
			for (String algorithmType : collaborativeFilteringTypes) {
				evaluatorFactories.add(CollaborativeFilteringResponseTimeEvaluator.factory(idClass, collaboratorClass, messageClass, threadClass, featureType, algorithmType));
			}
		}
	}
	
	public void runTestbed() throws IOException {
		for (MessageDataset<Id, Collaborator, Message, MsgThread> dataset : datasets) {
			
			Collection<Metric> unusedMetrics = new ArrayList<>();
			for (ResponseTimeMetricFactory metricFactory : metricFactories) {
				unusedMetrics.add(metricFactory.create());
			}
			String headerPrefix = "type,k,test,account";
			MetricResultCollection<Long> resultCollection = new MetricResultCollection<Long>(
					headerPrefix, unusedMetrics,
					dataset.getPreviousResultsResponseTimeMetricsFile());
			for (ResponseTimeEvaluatorFactory<Id, Collaborator, Message, MsgThread> evaluatorFactory : evaluatorFactories) {
				Collection<ResponseTimeMetric> metrics = new ArrayList<>();
				for (ResponseTimeMetricFactory metricFactory : metricFactories) {
					metrics.add(metricFactory.create());
				}
				ResponseTimeEvaluator<Id, Collaborator, Message, MsgThread> evaluator = 
						evaluatorFactory.create(dataset, metrics);
				
				for (Integer testId : evaluator.getTestIds()) {
					List<MetricResult> results = evaluator.evaluate(testId);
					String label = evaluator.getType()+","+testId;
					System.out.println(evaluator.getType()+","+testId);
					resultCollection.addResults(label, null, results);
				}
			}
		}
	}
}
