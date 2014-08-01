package testbed;

import java.io.File;
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
import testbed.dataset.actions.messages.MessageDataset;
import data.representation.actionbased.messages.MessageThread;
import data.representation.actionbased.messages.SingleMessage;

public class LivenessTestbed <Id, Collaborator extends Comparable<Collaborator>, Message extends SingleMessage<Collaborator>, MsgThread extends MessageThread<Collaborator, Message>> {
	
	public static int NUMBER_OF_FOLDS = 10;

	Collection<MessageDataset<Id, Collaborator, Message, MsgThread>> datasets;
	
	Collection<MessageLivenessPredictorFactory<Collaborator, Message, MsgThread>> predictorFactories = new ArrayList<>();
	Collection<FeatureRuleFactory<Collaborator, Message, MsgThread>> featureFactories = new ArrayList<>();
	Collection<ResponseLivenessMetricFactory> metricFactories = new ArrayList<>();

	
	public LivenessTestbed(
			Collection<MessageDataset<Id, Collaborator, Message, MsgThread>> datasets,
			Class<Collaborator> collaboratorClass,
			Class<Message> messageClass,
			Class<MsgThread> threadClass) {
		
		this.datasets = datasets;
		init(collaboratorClass, messageClass, threadClass);
	}
	
	private void init(Class<Collaborator> collaboratorClass,
			Class<Message> messageClass,
			Class<MsgThread> threadClass) {
		
		
		// Add prediction generators
		predictorFactories.add(ConstantMessageLivenessPredictor.factory(LivenessPrediction.Live, collaboratorClass, messageClass, threadClass));
		predictorFactories.add(ConstantMessageLivenessPredictor.factory(LivenessPrediction.Dead, collaboratorClass, messageClass, threadClass));
		predictorFactories.add(RandomMessageLivenessPredictor.factory(0.5, collaboratorClass, messageClass, threadClass));
		predictorFactories.add(TrainingRateMessageLivenessPredictor.factory(collaboratorClass, messageClass, threadClass));
		predictorFactories.add(WekaMessageLivenessPredictor.factory("decision_tree", new WekaDecisionTreeModelRule("hasResponse", 2), collaboratorClass, messageClass, threadClass));
		predictorFactories.add(WekaMessageLivenessPredictor.factory("logistic_regression", new WekaLogisticRegressionModelRule("hasResponse", 2), collaboratorClass, messageClass, threadClass));
		
		// Add feature retrievers
		featureFactories.add(MessageCollaboratorNumRule.factory(collaboratorClass, messageClass, threadClass, "numCollaborators"));
		featureFactories.add(MessageCollaboratorsIdsRule.factory(collaboratorClass, messageClass, threadClass, "collaborators"));
		featureFactories.add(MessageCreatorIdRule.factory(collaboratorClass, messageClass, threadClass, "creators"));
		featureFactories.add(MessageTitleLengthRule.factory(collaboratorClass, messageClass, threadClass, "titleLength"));
		featureFactories.add(MessageTitleWordIdsRule.factory(collaboratorClass, messageClass, threadClass, "titleWords"));
		
		// Add metrics
		metricFactories.add(AccuracyMetric.factory());
		metricFactories.add(FalsePositiveRateMetric.factory());
		metricFactories.add(FalseNegativeRateMetric.factory());
		metricFactories.add(PositivePredictiveValueMetric.factory());
		metricFactories.add(NegativePredictiveValueMetric.factory());
	}
	
	
	public void runTestbed() throws Exception {
	
		Set<String> stopWords = new TreeSet<>(FileUtils.readLines(new File("specs/stopwords.txt")));
		
		for (MessageDataset<Id, Collaborator, Message, MsgThread> dataset : datasets) {

			Collection<Metric> unusedMetrics = new ArrayList<>();
			for (ResponseLivenessMetricFactory metricFactory : metricFactories) {
				unusedMetrics.add(metricFactory.create());
			}
			String headerPrefix = "type,fold,account";
			MetricResultCollection<Id> resultCollection = new MetricResultCollection<Id>(
					headerPrefix, unusedMetrics,
					dataset.getLivenessMetricsFile());
			
			for (Id account : dataset.getAccountIds()) {
				Map<Integer,ThreadFold<Collaborator, Message, MsgThread>> folds = dataset.getThreadFolds(account, NUMBER_OF_FOLDS);

				for (MessageLivenessPredictorFactory<Collaborator, Message, MsgThread> predictorFactory : predictorFactories) {

					for (Integer foldId :  folds.keySet()) {
				
						ThreadFold<Collaborator, Message, MsgThread> fold = folds.get(foldId);
						Collection<MsgThread> trainThreads = fold.trainThreads;
						Collection<MsgThread> testThreads = fold.testThreads;

						ThreadSetProperties<Collaborator, Message, MsgThread> threadsProperties = 
								new ThreadSetProperties<>(trainThreads, testThreads, stopWords);

					
						Collection<IBasicFeatureRule> features = new ArrayList<>();
						for (FeatureRuleFactory<Collaborator, Message, MsgThread> featureFactory : featureFactories) {
							features.add(featureFactory.create(threadsProperties));
						}

						MessageLivenessPredictor<Collaborator, Message, MsgThread> predictor = predictorFactory.create(features, threadsProperties);

						for (MsgThread thread : trainThreads) {
							predictor.addPastThread(thread);
						}
						
						Collection<ResponseLivenessMetric> metrics = new ArrayList<>();
						for (ResponseLivenessMetricFactory metricFactory : metricFactories) {
							metrics.add(metricFactory.create());
						}
						
						String label = predictor.getTitle() + "," + foldId;
						System.out.println(label);
						
						FileUtils.write(dataset.getLivenessModelsFile(predictor.getTitle(), foldId), predictor.getModelInfo());
						
						LivenessPredictionEvaluationModeler<Collaborator, Message, MsgThread> modeler =
								new LivenessPredictionEvaluationModeler<>(testThreads, predictor, metrics);
						Collection<MetricResult> results = modeler.modelPredictionEvaluation();
						resultCollection.addResults(label, account, results);
					}
				}
			}
		}
	}
}
