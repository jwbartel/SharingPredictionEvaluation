package testbed.datasetspecific.old;

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
import metrics.response.liveness.AccuracyMetric;
import metrics.response.liveness.FalseNegativeRateMetric;
import metrics.response.liveness.FalsePositiveRateMetric;
import metrics.response.liveness.NegativePredictiveValueMetric;
import metrics.response.liveness.PositivePredictiveValueMetric;
import metrics.response.liveness.ResponseLivenessMetric;
import metrics.response.liveness.ResponseLivenessMetricFactory;
import model.prediction.liveness.LivenessPredictionEvaluationModeler;

import org.apache.commons.io.FileUtils;

import prediction.features.messages.FeatureRuleFactory;
import prediction.features.messages.MessageCollaboratorNumRule;
import prediction.features.messages.MessageCollaboratorsIdsRule;
import prediction.features.messages.MessageCreatorIdRule;
import prediction.features.messages.MessageTitleLengthRule;
import prediction.features.messages.MessageTitleWordIdsRule;
import prediction.features.messages.ThreadSetProperties;
import prediction.response.liveness.message.ConstantMessageLivenessPredictor;
import prediction.response.liveness.message.ConstantMessageLivenessPredictor.LivenessPrediction;
import prediction.response.liveness.message.MessageLivenessPredictor;
import prediction.response.liveness.message.MessageLivenessPredictorFactory;
import prediction.response.liveness.message.RandomMessageLivenessPredictor;
import prediction.response.liveness.message.TrainingRateMessageLivenessPredictor;
import prediction.response.liveness.message.WekaMessageLivenessPredictor;
import snml.rule.basicfeature.IBasicFeatureRule;
import snml.rule.superfeature.model.weka.WekaDecisionTreeModelRule;
import snml.rule.superfeature.model.weka.WekaLogisticRegressionModelRule;
import testbed.dataset.actions.ActionsDataSet.ThreadFold;
import testbed.dataset.actions.messages.stackoverflow.SampledStackOverflowDataset;
import testbed.dataset.actions.messages.stackoverflow.StackOverflowDataset;
import data.representation.actionbased.messages.stackoverflow.StackOverflowMessage;
import data.representation.actionbased.messages.stackoverflow.StackOverflowThread;

public class StackOverflowLivenessTestBed {
	
	static int numFolds = 10;
	
	static Collection<StackOverflowDataset<String, StackOverflowMessage<String>, StackOverflowThread<String, StackOverflowMessage<String>>>> dataSets = new ArrayList<>();
	static Collection<MessageLivenessPredictorFactory<String, StackOverflowMessage<String>, StackOverflowThread<String, StackOverflowMessage<String>>>> predictorFactories = new ArrayList<>();
	static Collection<FeatureRuleFactory<String, StackOverflowMessage<String>, StackOverflowThread<String, StackOverflowMessage<String>>>> featureFactories = new ArrayList<>();
	static Collection<ResponseLivenessMetricFactory> metricFactories = new ArrayList<>();

	static {
		try {
			dataSets.add(new SampledStackOverflowDataset("Sampled StackOverflow", new File(
								"data/Stack Overflow/10000 Random Questions")));
		} catch (IOException | ParseException e) {
			e.printStackTrace();
			System.exit(0);
		}
		
		featureFactories.add(MessageCollaboratorNumRule.factory(String.class, StackOverflowMessage.class, StackOverflowThread.class, "numCollaborators"));
		featureFactories.add(MessageCollaboratorsIdsRule.factory(String.class, StackOverflowMessage.class, StackOverflowThread.class, "collaborators"));
		featureFactories.add(MessageCreatorIdRule.factory(String.class, StackOverflowMessage.class, StackOverflowThread.class, "creators"));
		featureFactories.add(MessageTitleLengthRule.factory(String.class, StackOverflowMessage.class, StackOverflowThread.class, "titleLength"));
		featureFactories.add(MessageTitleWordIdsRule.factory(String.class, StackOverflowMessage.class, StackOverflowThread.class, "titleWords"));
		
		predictorFactories.add(ConstantMessageLivenessPredictor.factory(LivenessPrediction.Live, String.class, StackOverflowMessage.class, StackOverflowThread.class));
		predictorFactories.add(ConstantMessageLivenessPredictor.factory(LivenessPrediction.Dead, String.class, StackOverflowMessage.class, StackOverflowThread.class));
		predictorFactories.add(RandomMessageLivenessPredictor.factory(0.5, String.class, StackOverflowMessage.class, StackOverflowThread.class));
		predictorFactories.add(TrainingRateMessageLivenessPredictor.factory(String.class, StackOverflowMessage.class, StackOverflowThread.class));
		predictorFactories.add(WekaMessageLivenessPredictor.factory("decision_tree", new WekaDecisionTreeModelRule("hasResponse", 2), String.class, StackOverflowMessage.class, StackOverflowThread.class));
		predictorFactories.add(WekaMessageLivenessPredictor.factory("logistic_regression", new WekaLogisticRegressionModelRule("hasResponse", 2), String.class, StackOverflowMessage.class, StackOverflowThread.class));
		
		
		metricFactories.add(AccuracyMetric.factory());
		metricFactories.add(FalsePositiveRateMetric.factory());
		metricFactories.add(FalseNegativeRateMetric.factory());
		metricFactories.add(PositivePredictiveValueMetric.factory());
		metricFactories.add(NegativePredictiveValueMetric.factory());
	}
	
	
	public static void main(String[] args) throws Exception {
	
		Set<String> stopWords = new TreeSet<>(FileUtils.readLines(new File("specs/stopwords.txt")));
		
		for (StackOverflowDataset<String, StackOverflowMessage<String>, StackOverflowThread<String, StackOverflowMessage<String>>> dataset : dataSets) {

			Collection<Metric> unusedMetrics = new ArrayList<>();
			for (ResponseLivenessMetricFactory metricFactory : metricFactories) {
				unusedMetrics.add(metricFactory.create());
			}
			String headerPrefix = "type,fold,account";
			MetricResultCollection<Long> resultCollection = new MetricResultCollection<Long>(
					headerPrefix, unusedMetrics,
					dataset.getLivenessMetricsFile());
			
			for (Long account : dataset.getAccountIds()) {
				Map<Integer,ThreadFold<String, StackOverflowMessage<String>, StackOverflowThread<String, StackOverflowMessage<String>>>> folds = dataset.getThreadFolds(account, numFolds);

				for (MessageLivenessPredictorFactory<String, StackOverflowMessage<String>, StackOverflowThread<String, StackOverflowMessage<String>>> predictorFactory : predictorFactories) {

					for (Integer foldId :  folds.keySet()) {
				
						ThreadFold<String, StackOverflowMessage<String>, StackOverflowThread<String, StackOverflowMessage<String>>> fold = folds.get(foldId);
						Collection<StackOverflowThread<String, StackOverflowMessage<String>>> trainThreads = fold.trainThreads;
						Collection<StackOverflowThread<String, StackOverflowMessage<String>>> testThreads = fold.testThreads;

						ThreadSetProperties<String, StackOverflowMessage<String>, StackOverflowThread<String, StackOverflowMessage<String>>> threadsProperties = 
								new ThreadSetProperties<>(trainThreads, testThreads, stopWords);

					
						Collection<IBasicFeatureRule> features = new ArrayList<>();
						for (FeatureRuleFactory<String, StackOverflowMessage<String>, StackOverflowThread<String, StackOverflowMessage<String>>> featureFactory : featureFactories) {
							features.add(featureFactory.create(threadsProperties));
						}

						MessageLivenessPredictor<String, StackOverflowMessage<String>, StackOverflowThread<String, StackOverflowMessage<String>>> predictor = predictorFactory.create(features, threadsProperties);

						for (StackOverflowThread<String, StackOverflowMessage<String>> thread : trainThreads) {
							predictor.addPastThread(thread);
						}
						
						Collection<ResponseLivenessMetric> metrics = new ArrayList<>();
						for (ResponseLivenessMetricFactory metricFactory : metricFactories) {
							metrics.add(metricFactory.create());
						}
						
						String label = predictor.getTitle() + "," + foldId;
						System.out.println(label);
						
						FileUtils.write(dataset.getLivenessModelsFile(predictor.getTitle(), foldId), predictor.getModelInfo());
						
						LivenessPredictionEvaluationModeler<String, StackOverflowMessage<String>, StackOverflowThread<String, StackOverflowMessage<String>>> modeler =
								new LivenessPredictionEvaluationModeler<String, StackOverflowMessage<String>, StackOverflowThread<String, StackOverflowMessage<String>>>(testThreads, predictor, metrics);
						Collection<MetricResult> results = modeler.modelPredictionEvaluation();
						resultCollection.addResults(label, account, results);
					}
				}
			}
		}
	}
}
