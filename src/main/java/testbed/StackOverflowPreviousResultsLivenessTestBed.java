package testbed;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
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
import testbed.dataset.actions.messages.stackoverflow.SampledStackOverflowDataset;
import testbed.previousresults.ConstantPredictionLivenessEvaluator;
import testbed.previousresults.GradientAscentLivenessEvaluator;
import testbed.previousresults.LivenessEvaluator;
import testbed.previousresults.LivenessEvaluator.LivenessEvaluatorFactory;
import testbed.previousresults.RandomPredictionLivenessEvaluator;
import data.representation.actionbased.messages.stackoverflow.StackOverflowMessage;
import data.representation.actionbased.messages.stackoverflow.StackOverflowThread;

public class StackOverflowPreviousResultsLivenessTestBed {

	static Collection<MessageDataset<Long, String, StackOverflowMessage<String>, StackOverflowThread<String, StackOverflowMessage<String>>>> dataSets = new ArrayList<>();
	static Collection<ResponseLivenessMetricFactory> metricFactories = new ArrayList<>();
	static Collection<LivenessEvaluatorFactory<Long, String, StackOverflowMessage<String>, StackOverflowThread<String, StackOverflowMessage<String>>>> evaluatorFactories = new ArrayList<>();
	
	
	static {
		try {
			dataSets.add(new SampledStackOverflowDataset("Sampled StackOverflow", new File(
								"data/Stack Overflow/10000 Random Questions"), false));
		} catch (IOException | ParseException e) {
			e.printStackTrace();
			System.exit(0);
		}
		
		metricFactories.add(AccuracyMetric.factory());
		metricFactories.add(FalsePositiveRateMetric.factory());
		metricFactories.add(FalseNegativeRateMetric.factory());
		metricFactories.add(PositivePredictiveValueMetric.factory());
		metricFactories.add(NegativePredictiveValueMetric.factory());
		
		evaluatorFactories.add(GradientAscentLivenessEvaluator.factory(Long.class, String.class, StackOverflowMessage.class, StackOverflowThread.class));
		evaluatorFactories.add(ConstantPredictionLivenessEvaluator.factory(Long.class, String.class, StackOverflowMessage.class, StackOverflowThread.class, "all dead", 0.0));
		evaluatorFactories.add(ConstantPredictionLivenessEvaluator.factory(Long.class, String.class, StackOverflowMessage.class, StackOverflowThread.class, "all live", 1.0));
		evaluatorFactories.add(RandomPredictionLivenessEvaluator.factory(Long.class, String.class, StackOverflowMessage.class, StackOverflowThread.class, "random", 0.5));
		evaluatorFactories.add(RandomPredictionLivenessEvaluator.factory(Long.class, String.class, StackOverflowMessage.class, StackOverflowThread.class, "distribution based", 0.8575));
	}
	
	public static void main(String[] args) throws IOException {
		
		for (MessageDataset<Long, String, StackOverflowMessage<String>, StackOverflowThread<String, StackOverflowMessage<String>>> dataset : dataSets) {
			
			Collection<Metric> unusedMetrics = new ArrayList<>();
			for (ResponseLivenessMetricFactory metricFactory : metricFactories) {
				unusedMetrics.add(metricFactory.create());
			}
			String headerPrefix = "type,test,account";
			MetricResultCollection<Long> resultCollection = new MetricResultCollection<Long>(
					headerPrefix, unusedMetrics,
					dataset.getPreviousResultsLivenessMetricsFile());
			for (LivenessEvaluatorFactory<Long, String, StackOverflowMessage<String>, StackOverflowThread<String, StackOverflowMessage<String>>> evaluatorFactory : evaluatorFactories) {
				Collection<ResponseLivenessMetric> metrics = new ArrayList<>();
				for (ResponseLivenessMetricFactory metricFactory : metricFactories) {
					metrics.add(metricFactory.create());
				}
				LivenessEvaluator<Long, String, StackOverflowMessage<String>, StackOverflowThread<String, StackOverflowMessage<String>>> evaluator = 
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
