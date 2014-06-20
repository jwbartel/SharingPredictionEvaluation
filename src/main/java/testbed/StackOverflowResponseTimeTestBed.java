package testbed;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

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
import model.prediction.responsetime.ResponseTimePredictionEvaluationModeler;

import org.apache.commons.io.FileUtils;

import prediction.features.messages.FeatureRuleFactory;
import prediction.features.messages.MessageCollaboratorNumRule;
import prediction.features.messages.MessageCollaboratorsIdsRule;
import prediction.features.messages.MessageTitleLengthRule;
import prediction.features.messages.MessageTitleWordIdsRule;
import prediction.features.messages.ThreadSetProperties;
import prediction.response.time.DistributionResponseTimePredictor;
import prediction.response.time.InverseGaussianDistribution;
import prediction.response.time.LogNormalDistribution;
import prediction.response.time.message.ConstantMessageResponseTimePredictor;
import prediction.response.time.message.DistributionBasedMessageResponseTimePredictor;
import prediction.response.time.message.MessageResponseTimePredictor;
import prediction.response.time.message.MessageResponseTimePredictorFactory;
import prediction.response.time.message.WekaClusteringMessageResponseTimePredictor;
import prediction.response.time.message.WekaRegressionMessageResponseTimePredictor;
import prediction.response.time.message.SigmoidWeightedKmeansMessageResponseTimePredictor;
import snml.rule.basicfeature.IBasicFeatureRule;
import snml.rule.superfeature.model.weka.WekaKmeansModelRule;
import snml.rule.superfeature.model.weka.WekaLinearRegressionModelRule;
import testbed.dataset.actions.ActionsDataSet.ThreadFold;
import testbed.dataset.actions.messages.stackoverflow.SampledStackOverflowDataset;
import testbed.dataset.actions.messages.stackoverflow.StackOverflowDataset;
import testbed.dataset.actions.messages.stackoverflow.evaluation.ConstantPredictionResponseTimeEvaluator;
import testbed.dataset.actions.messages.stackoverflow.evaluation.DistributionResponseTimeEvaluator;
import data.representation.actionbased.messages.stackoverflow.StackOverflowMessage;
import data.representation.actionbased.messages.stackoverflow.StackOverflowThread;

public class StackOverflowResponseTimeTestBed {
	
	static int numFolds = 10;
	
	static Collection<StackOverflowDataset<String, StackOverflowMessage<String>, StackOverflowThread<String, StackOverflowMessage<String>>>> dataSets = new ArrayList<>();
	static Collection<MessageResponseTimePredictorFactory<String, StackOverflowMessage<String>, StackOverflowThread<String, StackOverflowMessage<String>>>> predictorFactories = new ArrayList<>();
	static Collection<FeatureRuleFactory<String, StackOverflowMessage<String>, StackOverflowThread<String, StackOverflowMessage<String>>>> featureFactories = new ArrayList<>();
	static Collection<ResponseTimeMetricFactory> metricFactories = new ArrayList<>();

	static {
		try {
			dataSets.add(new SampledStackOverflowDataset("Sampled StackOverflow", new File(
								"data/Stack Overflow/100 Random Questions")));
		} catch (IOException | ParseException e) {
			e.printStackTrace();
			System.exit(0);
		}
		
		featureFactories.add(MessageCollaboratorNumRule.factory(String.class, StackOverflowMessage.class, StackOverflowThread.class, "numCollaborators"));
		featureFactories.add(MessageCollaboratorsIdsRule.factory(String.class, StackOverflowMessage.class, StackOverflowThread.class, "collaborators"));
//		featureFactories.add(MessageCreatorIdRule.factory(String.class, StackOverflowMessage.class, StackOverflowThread.class, "creators"));
		featureFactories.add(MessageTitleLengthRule.factory(String.class, StackOverflowMessage.class, StackOverflowThread.class, "titleLength"));
		featureFactories.add(MessageTitleWordIdsRule.factory(String.class, StackOverflowMessage.class, StackOverflowThread.class, "titleWords"));

		predictorFactories.add(ConstantMessageResponseTimePredictor.factory(String.class, StackOverflowMessage.class, StackOverflowThread.class, "1 minute", 60.0));
		predictorFactories.add(ConstantMessageResponseTimePredictor.factory(String.class, StackOverflowMessage.class, StackOverflowThread.class, "3 minutes", 3*60.0));
		predictorFactories.add(ConstantMessageResponseTimePredictor.factory(String.class, StackOverflowMessage.class, StackOverflowThread.class, "5 minutes", 5*60.0));
		predictorFactories.add(ConstantMessageResponseTimePredictor.factory(String.class, StackOverflowMessage.class, StackOverflowThread.class, "10 minutes", 10*60.0));
		predictorFactories.add(ConstantMessageResponseTimePredictor.factory(String.class, StackOverflowMessage.class, StackOverflowThread.class, "20 minutes", 20*60.0));
		predictorFactories.add(DistributionBasedMessageResponseTimePredictor.factory(new InverseGaussianDistribution(867.482, 571.108), String.class, StackOverflowMessage.class, StackOverflowThread.class));
		predictorFactories.add(DistributionBasedMessageResponseTimePredictor.factory(new LogNormalDistribution(6.35702, 0.927127), String.class, StackOverflowMessage.class, StackOverflowThread.class));
//		predictorFactories.add(MessageRandomLivenessPredictor.factory(0.5, String.class, StackOverflowMessage.class, StackOverflowThread.class));
//		predictorFactories.add(MessageTrainingRateLivenessPredictor.factory(String.class, StackOverflowMessage.class, StackOverflowThread.class));
//		predictorFactories.add(MessageWekaLivenessPredictor.factory("logistic_regression - num collaborators and title length", new WekaLogisticRegressionModelRule("hasResponse", 2), String.class, StackOverflowMessage.class, StackOverflowThread.class));
//		predictorFactories.add(MessageWekaRegressionResponseTimePredictor.factory("sgd - num collaborators and title length", new WekaSGDRegressionModelRule("responseTime"), String.class, StackOverflowMessage.class, StackOverflowThread.class));
//		predictorFactories.add(MessageWekaRegressionResponseTimePredictor.factory("linear regression - num collaborators and title length", new WekaLinearRegressionModelRule("responseTime"), String.class, StackOverflowMessage.class, StackOverflowThread.class));
//		for (int k = 2; k <= 25; k++) {
//			predictorFactories.add(MessageWekaClusteringResponseTimePredictor.factory("k-means_k"+k, new WekaKmeansModelRule("responseTime",k), String.class, StackOverflowMessage.class, StackOverflowThread.class));
//		}
		for (int k = 2; k <= 25; k++) {
			predictorFactories.add(SigmoidWeightedKmeansMessageResponseTimePredictor.factory("weighted k-means_k"+k, new WekaKmeansModelRule("responseTime",k), String.class, StackOverflowMessage.class, StackOverflowThread.class));
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
	}
	
	
	public static void main(String[] args) throws Exception {
	
		Set<String> stopWords = new TreeSet<>(FileUtils.readLines(new File("specs/stopwords.txt")));
		
		for (StackOverflowDataset<String, StackOverflowMessage<String>, StackOverflowThread<String, StackOverflowMessage<String>>> dataset : dataSets) {

			Collection<Metric> unusedMetrics = new ArrayList<>();
			for (ResponseTimeMetricFactory metricFactory : metricFactories) {
				unusedMetrics.add(metricFactory.create());
			}
			String headerPrefix = "type,fold,account";
			MetricResultCollection<Long> resultCollection = new MetricResultCollection<Long>(
					headerPrefix, unusedMetrics,
					dataset.getResponseTimeMetricsFile());
			
			for (Long account : dataset.getAccountIds()) {
				Map<Integer,ThreadFold<String, StackOverflowMessage<String>, StackOverflowThread<String, StackOverflowMessage<String>>>> folds = dataset.getThreadFolds(account, numFolds);

				for (MessageResponseTimePredictorFactory<String, StackOverflowMessage<String>, StackOverflowThread<String, StackOverflowMessage<String>>> predictorFactory : predictorFactories) {

					for (Integer foldId :  folds.keySet()) {
				
						ThreadFold<String, StackOverflowMessage<String>, StackOverflowThread<String, StackOverflowMessage<String>>> fold = folds.get(foldId);
						Collection<StackOverflowThread<String, StackOverflowMessage<String>>> trainThreads = fold.trainThreads;
						Collection<StackOverflowThread<String, StackOverflowMessage<String>>> testThreads = fold.testThreads;

						ThreadSetProperties<String, StackOverflowMessage<String>, StackOverflowThread<String, StackOverflowMessage<String>>> threadsProperties = 
								new ThreadSetProperties<>(trainThreads, testThreads, stopWords);

//						if (threadsProperties != null) {
//							System.out.println("Total creators:"+threadsProperties.getCreators().size());
//							System.out.println("Total collaborators:"+threadsProperties.getCollaborators().size());
//							System.out.println("Total words:" + threadsProperties.getTitleWords().size());
//							
//							CollectionIOAssist.writeCollection(new File(dataset.getRootFolder(), "creators.txt"), threadsProperties.getCreators());
//							CollectionIOAssist.writeCollection(new File(dataset.getRootFolder(), "collaborators.txt"), threadsProperties.getCollaborators());
//							CollectionIOAssist.writeCollection(new File(dataset.getRootFolder(), "title words.txt"), threadsProperties.getTitleWords());
//							System.exit(0);
//						}
					
						Collection<IBasicFeatureRule> features = new ArrayList<>();
						for (FeatureRuleFactory<String, StackOverflowMessage<String>, StackOverflowThread<String, StackOverflowMessage<String>>> featureFactory : featureFactories) {
							features.add(featureFactory.create(threadsProperties));
						}

						MessageResponseTimePredictor<String, StackOverflowMessage<String>, StackOverflowThread<String, StackOverflowMessage<String>>> predictor = predictorFactory.create(features, threadsProperties);

						for (StackOverflowThread<String, StackOverflowMessage<String>> thread : trainThreads) {
							predictor.addPastThread(thread);
						}
						
						Collection<ResponseTimeMetric> metrics = new ArrayList<>();
						for (ResponseTimeMetricFactory metricFactory : metricFactories) {
							metrics.add(metricFactory.create());
						}
						
						String label = predictor.getTitle() + "," + foldId;
						System.out.println(label);
						
						FileUtils.write(dataset.getResponseTimeModelsFile(predictor.getTitle(), foldId), predictor.getModelInfo());
						
						ResponseTimePredictionEvaluationModeler<String, StackOverflowMessage<String>, StackOverflowThread<String, StackOverflowMessage<String>>> modeler =
								new ResponseTimePredictionEvaluationModeler<String, StackOverflowMessage<String>, StackOverflowThread<String, StackOverflowMessage<String>>>(testThreads, predictor, metrics);
						Collection<MetricResult> results = modeler.modelPredictionEvaluation();
						resultCollection.addResults(label, account, results);
					}
				}
			}
		}
	}
}
