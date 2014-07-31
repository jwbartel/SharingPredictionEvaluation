package testbed;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import prediction.response.time.InverseGaussianDistribution;
import prediction.response.time.LogNormalDistribution;
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
import testbed.dataset.actions.messages.stackoverflow.SampledStackOverflowDataset;
import testbed.dataset.actions.messages.stackoverflow.StackOverflowDataset;
import testbed.previousresults.CollaborativeFilteringResponseTimeEvaluator;
import testbed.previousresults.ConstantPredictionResponseTimeEvaluator;
import testbed.previousresults.DistributionResponseTimeEvaluator;
import testbed.previousresults.GradientAscentResponseTimeEvaluator;
import testbed.previousresults.KmeansResponseTimeEvaluator;
import testbed.previousresults.ResponseTimeEvaluator;
import testbed.previousresults.SigmoidWeightedKmeansResponseTimeEvaluator;
import testbed.previousresults.ResponseTimeEvaluator.ResponseTimeEvaluatorFactory;
import data.representation.actionbased.messages.stackoverflow.StackOverflowMessage;
import data.representation.actionbased.messages.stackoverflow.StackOverflowThread;

public class StackOverflowPreviousResultsResponseTimeTestBed {

	static Collection<StackOverflowDataset<String, StackOverflowMessage<String>, StackOverflowThread<String, StackOverflowMessage<String>>>> dataSets = new ArrayList<>();
	static Collection<ResponseTimeMetricFactory> metricFactories = new ArrayList<>();
	static Collection<ResponseTimeEvaluatorFactory<Long, String, StackOverflowMessage<String>, StackOverflowThread<String, StackOverflowMessage<String>>>> evaluatorFactories = new ArrayList<>();
	

	static {
		try {
			dataSets.add(new SampledStackOverflowDataset("Sampled StackOverflow", new File(
								"data/Stack Overflow/10000 Random Questions"), false));
		} catch (IOException | ParseException e) {
			e.printStackTrace();
			System.exit(0);
		}
		
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
		
		evaluatorFactories.add(ConstantPredictionResponseTimeEvaluator.factory(Long.class, String.class, StackOverflowMessage.class, StackOverflowThread.class, "1 minute", 60.0));
		evaluatorFactories.add(ConstantPredictionResponseTimeEvaluator.factory(Long.class, String.class, StackOverflowMessage.class, StackOverflowThread.class, "3 minutes", 3*60.0));
		evaluatorFactories.add(ConstantPredictionResponseTimeEvaluator.factory(Long.class, String.class, StackOverflowMessage.class, StackOverflowThread.class, "5 minutes", 5*60.0));
		evaluatorFactories.add(ConstantPredictionResponseTimeEvaluator.factory(Long.class, String.class, StackOverflowMessage.class, StackOverflowThread.class, "10 minutes", 10*60.0));
		evaluatorFactories.add(ConstantPredictionResponseTimeEvaluator.factory(Long.class, String.class, StackOverflowMessage.class, StackOverflowThread.class, "20 minutes", 20*60.0));
		evaluatorFactories.add(DistributionResponseTimeEvaluator.factory(Long.class, String.class, StackOverflowMessage.class, StackOverflowThread.class, new InverseGaussianDistribution(867.482, 571.108)));
		evaluatorFactories.add(DistributionResponseTimeEvaluator.factory(Long.class, String.class, StackOverflowMessage.class, StackOverflowThread.class, new LogNormalDistribution(6.35702, 0.927127)));
		
		
		evaluatorFactories.add(GradientAscentResponseTimeEvaluator.factory(Long.class, String.class, StackOverflowMessage.class, StackOverflowThread.class));
		for (int k=2; k<=25; k++) {
			evaluatorFactories.add(KmeansResponseTimeEvaluator.factory(Long.class, String.class, StackOverflowMessage.class, StackOverflowThread.class, k));
		}
		for (int k=2; k<=25; k++) {
			evaluatorFactories.add(SigmoidWeightedKmeansResponseTimeEvaluator.factory(Long.class, String.class, StackOverflowMessage.class, StackOverflowThread.class, k));
		}
		String[] collaborativeFilteringFeatureTypes = {"owner and tag", "owner and word", "owner and tag_word pair", "tag and word"};
		String[] collaborativeFilteringTypes = {"Random", "Euclidean", "PearsonCorrelation", "SlopeOne"};
		for (String featureType : collaborativeFilteringFeatureTypes) {
			for (String algorithmType : collaborativeFilteringTypes) {
				evaluatorFactories.add(CollaborativeFilteringResponseTimeEvaluator.factory(Long.class, String.class, StackOverflowMessage.class, StackOverflowThread.class, featureType, algorithmType));
			}
		}
	}
	
	public static void main(String[] args) throws IOException {
		
		for (StackOverflowDataset<String, StackOverflowMessage<String>, StackOverflowThread<String, StackOverflowMessage<String>>> dataset : dataSets) {
			
			Collection<Metric> unusedMetrics = new ArrayList<>();
			for (ResponseTimeMetricFactory metricFactory : metricFactories) {
				unusedMetrics.add(metricFactory.create());
			}
			String headerPrefix = "type,k,test,account";
			MetricResultCollection<Long> resultCollection = new MetricResultCollection<Long>(
					headerPrefix, unusedMetrics,
					dataset.getPreviousResultsResponseTimeMetricsFile());
			for (ResponseTimeEvaluatorFactory<Long, String, StackOverflowMessage<String>, StackOverflowThread<String, StackOverflowMessage<String>>> evaluatorFactory : evaluatorFactories) {
				Collection<ResponseTimeMetric> metrics = new ArrayList<>();
				for (ResponseTimeMetricFactory metricFactory : metricFactories) {
					metrics.add(metricFactory.create());
				}
				ResponseTimeEvaluator<Long, String, StackOverflowMessage<String>, StackOverflowThread<String, StackOverflowMessage<String>>> evaluator = 
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
