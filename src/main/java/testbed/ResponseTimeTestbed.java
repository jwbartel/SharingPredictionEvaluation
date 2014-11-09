package testbed;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
import metrics.response.time.RootMeanSquareErrorMetric;
import metrics.response.time.ScaleDifferenceMetric;
import model.prediction.responsetime.ResponseTimePredictionEvaluationModeler;

import org.apache.commons.io.FileUtils;

import prediction.features.messages.FeatureRuleFactory;
import prediction.features.messages.MessageCollaboratorNumRule;
import prediction.features.messages.MessageCollaboratorsIdSetRule;
import prediction.features.messages.MessageCollaboratorsIdsRule;
import prediction.features.messages.MessageCreatorIdRule;
import prediction.features.messages.MessageCreatorIdSetRule;
import prediction.features.messages.MessageTitleLengthRule;
import prediction.features.messages.MessageTitleWordIdSetRule;
import prediction.features.messages.MessageTitleWordIdsRule;
import prediction.features.messages.ThreadSetProperties;
import prediction.response.time.InverseGaussianDistribution;
import prediction.response.time.LogNormalDistribution;
import prediction.response.time.message.ALSWRCollaborativeFilterResponseTimePredictor;
import prediction.response.time.message.ConstantMessageResponseTimePredictor;
import prediction.response.time.message.DistributionBasedMessageResponseTimePredictor;
import prediction.response.time.message.MahoutCollaborativeFilteringResponseTimePredictor.MahoutCollaborativeFilteringPredictorFactory;
import prediction.response.time.message.MessageResponseTimePredictor;
import prediction.response.time.message.MessageResponseTimePredictorFactory;
import prediction.response.time.message.SigmoidWeightedKmeansMessageResponseTimePredictor;
import prediction.response.time.message.SlopeOneResponseTimePredictor;
import prediction.response.time.message.UserBasedCollaborativeFilterResponseTimePredictor;
import prediction.response.time.message.WekaClusteringMessageResponseTimePredictor;
import prediction.response.time.message.WekaRegressionMessageResponseTimePredictor;
import snml.rule.basicfeature.IBasicFeatureRule;
import snml.rule.superfeature.model.mahout.SimilarityMeasure;
import snml.rule.superfeature.model.weka.WekaKmeansModelRule;
import snml.rule.superfeature.model.weka.WekaLinearRegressionModelRule;
import testbed.dataset.actions.ActionsDataSet.ThreadFold;
import testbed.dataset.actions.messages.MessageDataset;
import testbed.dataset.actions.messages.email.ResponseTimeStudyDataSet;
import data.representation.actionbased.messages.MessageThread;
import data.representation.actionbased.messages.SingleMessage;
import data.representation.actionbased.messages.email.EmailMessage;
import data.representation.actionbased.messages.email.EmailThread;

public class ResponseTimeTestbed <Id, Collaborator extends Comparable<Collaborator>, Message extends SingleMessage<Collaborator>, MsgThread extends MessageThread<Collaborator, Message>> {
	
	static class UserItemFeatureFactories<Collaborator, Message extends SingleMessage<Collaborator>, ThreadType extends MessageThread<Collaborator, Message>> {
		FeatureRuleFactory<Collaborator,Message,ThreadType> userFactory;
		FeatureRuleFactory<Collaborator,Message,ThreadType> itemFactory;
		
		public UserItemFeatureFactories(
				FeatureRuleFactory<Collaborator,Message,ThreadType> userFactory,
				FeatureRuleFactory<Collaborator,Message,ThreadType> itemFactory) {
			
			this.userFactory = userFactory;
			this.itemFactory = itemFactory;
			
		}

	}
	
	public static int NUMBER_OF_FOLDS = 10;

	Collection<MessageDataset<Id, Collaborator, Message, MsgThread>> datasets;
	
	Collection<MessageResponseTimePredictorFactory<Collaborator, Message, MsgThread>> predictorFactories;
	Collection<FeatureRuleFactory<Collaborator, Message, MsgThread>> featureFactories;
	Collection<UserItemFeatureFactories<Collaborator, Message, MsgThread>> userItemFeatures;
	
	Collection<ResponseTimeMetricFactory> metricFactories;

	
	public ResponseTimeTestbed(
			Collection<MessageDataset<Id, Collaborator, Message, MsgThread>> datasets,
			InverseGaussianDistribution inverseGaussianDistribution,
			LogNormalDistribution lognormalDistribution,
			Class<Collaborator> collaboratorClass,
			Class<Message> messageClass,
			Class<MsgThread> threadClass) {
		
		this.datasets = datasets;
		init(inverseGaussianDistribution, lognormalDistribution, collaboratorClass, messageClass, threadClass);
	}
	
	private void init(InverseGaussianDistribution inverseGaussianDistribution,
			LogNormalDistribution lognormalDistribution,
			Class<Collaborator> collaboratorClass,
			Class<Message> messageClass,
			Class<MsgThread> threadClass) {
		
		
		// Add prediction generators
		predictorFactories = new ArrayList<>();

		predictorFactories.add(ConstantMessageResponseTimePredictor.factory(collaboratorClass, messageClass, threadClass, "mean", 98838.195));
		predictorFactories.add(ConstantMessageResponseTimePredictor.factory(collaboratorClass, messageClass, threadClass, "median", 4286.0));
		predictorFactories.add(ConstantMessageResponseTimePredictor.factory(collaboratorClass, messageClass, threadClass, "1 minute", 60.0));
		predictorFactories.add(ConstantMessageResponseTimePredictor.factory(collaboratorClass, messageClass, threadClass, "3 minutes", 3*60.0));
		predictorFactories.add(ConstantMessageResponseTimePredictor.factory(collaboratorClass, messageClass, threadClass, "5 minutes", 5*60.0));
		predictorFactories.add(ConstantMessageResponseTimePredictor.factory(collaboratorClass, messageClass, threadClass, "10 minutes", 10*60.0));
		predictorFactories.add(ConstantMessageResponseTimePredictor.factory(collaboratorClass, messageClass, threadClass, "20 minutes", 20*60.0));
//		predictorFactories.add(DistributionBasedMessageResponseTimePredictor.factory(new InverseGaussianDistribution(867.482, 571.108), collaboratorClass, messageClass, threadClass));
//		predictorFactories.add(DistributionBasedMessageResponseTimePredictor.factory(new LogNormalDistribution(6.35702, 0.927127), collaboratorClass, messageClass, threadClass));
		predictorFactories.add(DistributionBasedMessageResponseTimePredictor.factory(new InverseGaussianDistribution(50132.4, 113.647), collaboratorClass, messageClass, threadClass));
		predictorFactories.add(DistributionBasedMessageResponseTimePredictor.factory(new LogNormalDistribution(8.407, 2.87785), collaboratorClass, messageClass, threadClass));
		if (inverseGaussianDistribution != null) {
			predictorFactories.add(DistributionBasedMessageResponseTimePredictor.factory(inverseGaussianDistribution, collaboratorClass, messageClass, threadClass));
		}
		if (lognormalDistribution != null) {
			predictorFactories.add(DistributionBasedMessageResponseTimePredictor.factory(lognormalDistribution, collaboratorClass, messageClass, threadClass));
		}
//		predictorFactories.add(WekaRegressionMessageResponseTimePredictor.factory("sgd - num collaborators and title length", new WekaSGDRegressionModelRule("responseTime"), collaboratorClass, messageClass, threadClass));
//		predictorFactories.add(WekaRegressionMessageResponseTimePredictor.factory("linear regression - num collaborators and title length", new WekaLinearRegressionModelRule("responseTime"), collaboratorClass, messageClass, threadClass));
		for (int k = 2; k <= 25; k++) {
			predictorFactories.add(WekaClusteringMessageResponseTimePredictor.factory("k-means_k"+k, new WekaKmeansModelRule("responseTime",k), collaboratorClass, messageClass, threadClass));
		}
		for (int k = 2; k <= 25; k++) {
			predictorFactories.add(SigmoidWeightedKmeansMessageResponseTimePredictor.factory("weighted k-means_k"+k, new WekaKmeansModelRule("responseTime",k), collaboratorClass, messageClass, threadClass));
		}
		
		predictorFactories.add(SlopeOneResponseTimePredictor.factory("slope one", collaboratorClass, messageClass, threadClass));
		predictorFactories.add(UserBasedCollaborativeFilterResponseTimePredictor.factory("user-based-euclidean", SimilarityMeasure.EuclideanDistance, collaboratorClass, messageClass, threadClass));
		predictorFactories.add(UserBasedCollaborativeFilterResponseTimePredictor.factory("user-based-cosine", SimilarityMeasure.CosineSimilarity, collaboratorClass, messageClass, threadClass));
		predictorFactories.add(UserBasedCollaborativeFilterResponseTimePredictor.factory("user-based-pearson", SimilarityMeasure.PearsonCorrelation, collaboratorClass, messageClass, threadClass));
		predictorFactories.add(UserBasedCollaborativeFilterResponseTimePredictor.factory("user-based-spearman", SimilarityMeasure.SpearmanCorrelation, collaboratorClass, messageClass, threadClass));
		
		int[] numFeaturesForALSWR = {50, 150, 300, 400, 500, 1000};
		double[] lambdasForALSWR = {0.03, 0.04, 0.05, 0.06, 0.065, 0.075};
		predictorFactories.add(ALSWRCollaborativeFilterResponseTimePredictor.factory("ALS-WR", numFeaturesForALSWR, lambdasForALSWR, 10, collaboratorClass, messageClass, threadClass));
		
		
		// Add feature retrievers
		featureFactories = new ArrayList<>();
		featureFactories.add(MessageCollaboratorNumRule.factory(collaboratorClass, messageClass, threadClass, "numCollaborators"));
		featureFactories.add(MessageCollaboratorsIdsRule.factory(collaboratorClass, messageClass, threadClass, "collaborators"));
		featureFactories.add(MessageCreatorIdRule.factory(collaboratorClass, messageClass, threadClass, "creators"));
		featureFactories.add(MessageTitleLengthRule.factory(collaboratorClass, messageClass, threadClass, "titleLength"));
		featureFactories.add(MessageTitleWordIdsRule.factory(collaboratorClass, messageClass, threadClass, "titleWords"));
		
		// Add feature retrievers for collaborative filtering
		userItemFeatures = new ArrayList<>();
		userItemFeatures.add(new UserItemFeatureFactories<Collaborator, Message, MsgThread>(
				MessageCreatorIdSetRule.factory(collaboratorClass, messageClass, threadClass, "creators"),
				MessageCollaboratorsIdSetRule.factory(collaboratorClass, messageClass, threadClass, "collaborators")));
		userItemFeatures.add(new UserItemFeatureFactories<Collaborator, Message, MsgThread>(
				MessageCreatorIdSetRule.factory(collaboratorClass, messageClass, threadClass, "creators"),
				MessageTitleWordIdSetRule.factory(collaboratorClass, messageClass, threadClass, "titleWords")));
		userItemFeatures.add(new UserItemFeatureFactories<Collaborator, Message, MsgThread>(
				MessageCollaboratorsIdSetRule.factory(collaboratorClass, messageClass, threadClass, "collaborators"),
				MessageTitleWordIdSetRule.factory(collaboratorClass, messageClass, threadClass, "titleWords")));
		
		// Add metrics
		metricFactories = new ArrayList<>();
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
		metricFactories.add(RootMeanSquareErrorMetric.factory(MinOrMaxType.Minimum));
		metricFactories.add(PercentWithinErrorThresholdMetric.factory(MinOrMaxType.Minimum, "1 minute", 60.0));
		metricFactories.add(PercentWithinErrorThresholdMetric.factory(MinOrMaxType.Minimum, "3 minutes", 60*3.0));
		metricFactories.add(PercentWithinErrorThresholdMetric.factory(MinOrMaxType.Minimum, "5 minutes", 60*5.0));
		metricFactories.add(PercentWithinErrorThresholdMetric.factory(MinOrMaxType.Minimum, "10 minutes", 60*10.0));
		metricFactories.add(PercentWithinErrorThresholdMetric.factory(MinOrMaxType.Minimum, "20 minutes", 60*20.0));
		metricFactories.add(PercentWithinErrorThresholdMetric.factory(MinOrMaxType.Minimum, "1 hour", 3600.0));
		metricFactories.add(PercentWithinErrorThresholdMetric.factory(MinOrMaxType.Minimum, "1 day", 3600.0*24));
		metricFactories.add(PercentWithinErrorThresholdMetric.factory(MinOrMaxType.Minimum, "1 week", 3600.0*24*7));
	}
	
	private void runCollaborativeFilteringTests(
			MessageDataset<Id, Collaborator, Message, MsgThread> dataset,
			MetricResultCollection<Id> resultCollection,
			Id account,
			MessageResponseTimePredictorFactory<Collaborator, Message, MsgThread> predictorFactory,
			Integer foldId,
			Collection<MsgThread> trainThreads,
			Collection<MsgThread> validationThreads,
			Collection<MsgThread> testThreads,
			ThreadSetProperties<Collaborator, Message, MsgThread> threadsProperties
			) throws Exception {
		
		for (UserItemFeatureFactories<Collaborator, Message, MsgThread> userItemFeatureFactory : userItemFeatures) {
			
			IBasicFeatureRule userFeature = userItemFeatureFactory.userFactory.create(threadsProperties);
			IBasicFeatureRule itemFeature = userItemFeatureFactory.itemFactory.create(threadsProperties);
			List<IBasicFeatureRule> features = new ArrayList<>();
			features.add(userFeature);
			features.add(itemFeature);
			
			MessageResponseTimePredictor<Collaborator, Message, MsgThread> predictor = predictorFactory.create(features, threadsProperties);
	
			for (MsgThread thread : trainThreads) {
				predictor.addPastThread(thread);
			}
			predictor.validate(validationThreads);
			
			Collection<ResponseTimeMetric> metrics = new ArrayList<>();
			for (ResponseTimeMetricFactory metricFactory : metricFactories) {
				metrics.add(metricFactory.create());
			}

			String label = predictor.getTitle() + ","
					+ userFeature.getDestFeatureName() + ","
					+ itemFeature.getDestFeatureName() + "," + foldId;
			System.out.println(label);
			
			FileUtils.write(dataset.getResponseTimeModelsFile(predictor.getTitle(), foldId), predictor.getModelInfo());
			
			ResponseTimePredictionEvaluationModeler<Collaborator, Message, MsgThread> modeler =
					new ResponseTimePredictionEvaluationModeler<Collaborator, Message, MsgThread>(testThreads, predictor, metrics);
			Collection<MetricResult> results = modeler.modelPredictionEvaluation();
			resultCollection.addResults(label, account, results);
		}
	}
	
	private void runNonCollaborativeFilteringTests(
			MessageDataset<Id, Collaborator, Message, MsgThread> dataset,
			MetricResultCollection<Id> resultCollection,
			Id account,
			MessageResponseTimePredictorFactory<Collaborator, Message, MsgThread> predictorFactory,
			Integer foldId,
			Collection<MsgThread> trainThreads,
			Collection<MsgThread> validationThreads,
			Collection<MsgThread> testThreads,
			ThreadSetProperties<Collaborator, Message, MsgThread> threadsProperties
			) throws Exception {
		
		List<IBasicFeatureRule> features = new ArrayList<>();
		for (FeatureRuleFactory<Collaborator, Message, MsgThread> featureFactory : featureFactories) {
			features.add(featureFactory.create(threadsProperties));
		}

		MessageResponseTimePredictor<Collaborator, Message, MsgThread> predictor = predictorFactory.create(features, threadsProperties);

		for (MsgThread thread : trainThreads) {
			predictor.addPastThread(thread);
		}
		predictor.validate(validationThreads);
		
		Collection<ResponseTimeMetric> metrics = new ArrayList<>();
		for (ResponseTimeMetricFactory metricFactory : metricFactories) {
			metrics.add(metricFactory.create());
		}
		
		String label = predictor.getTitle() + ",N/A,N/A," + foldId;
		System.out.println(label);
		
		FileUtils.write(dataset.getResponseTimeModelsFile(predictor.getTitle(), foldId), predictor.getModelInfo());
		
		ResponseTimePredictionEvaluationModeler<Collaborator, Message, MsgThread> modeler =
				new ResponseTimePredictionEvaluationModeler<Collaborator, Message, MsgThread>(testThreads, predictor, metrics);
		Collection<MetricResult> results = modeler.modelPredictionEvaluation();
		resultCollection.addResults(label, account, results);
	}
	
	
	public void runTestbed() throws Exception {
	
		Set<String> stopWords = new TreeSet<>(FileUtils.readLines(new File("specs/stopwords.txt")));
		
		for (MessageDataset<Id, Collaborator, Message, MsgThread> dataset : datasets) {

			Collection<Metric> unusedMetrics = new ArrayList<>();
			for (ResponseTimeMetricFactory metricFactory : metricFactories) {
				unusedMetrics.add(metricFactory.create());
			}
			String headerPrefix = "type,user type,item type,fold,account";
			MetricResultCollection<Id> resultCollection = new MetricResultCollection<Id>(
					headerPrefix, unusedMetrics,
					dataset.getResponseTimeMetricsFile());
			
			for (Id account : dataset.getAccountIds()) {
				Map<Integer,ThreadFold<Collaborator, Message, MsgThread>> folds = dataset.getThreadFolds(account, NUMBER_OF_FOLDS);
				if (folds == null || folds.size() == 0) {
					continue;
				}

				for (MessageResponseTimePredictorFactory<Collaborator, Message, MsgThread> predictorFactory : predictorFactories) {

					for (Integer foldId :  folds.keySet()) {
				
						ThreadFold<Collaborator, Message, MsgThread> fold = folds.get(foldId);
						Collection<MsgThread> trainThreads = fold.trainThreads;
						Collection<MsgThread> validationThreads = fold.validationThreads;
						Collection<MsgThread> testThreads = fold.testThreads;

						ThreadSetProperties<Collaborator, Message, MsgThread> threadsProperties = 
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
					
						try {
							if (predictorFactory instanceof MahoutCollaborativeFilteringPredictorFactory) {
								runCollaborativeFilteringTests(dataset, resultCollection, account, predictorFactory, foldId, trainThreads, validationThreads, testThreads, threadsProperties);
							} else {
								runNonCollaborativeFilteringTests(dataset, resultCollection, account, predictorFactory, foldId, trainThreads, validationThreads, testThreads, threadsProperties);
							} 
						} catch (Exception e) {
							String message = e.getLocalizedMessage();
							if (message != null &&
									(message.contains("dataModel has no items") || 
											message.contains("Not enough training instances") ||
											message.contains("n must be at least 1"))) {
								e.printStackTrace();
								continue;
							} else {
								throw e;
							}
						}
						
					}
				}
			}
		}
	}
	
	public static void main(String[] args) throws Exception {
		
		EmailMessage<String> tempMessage = new EmailMessage<>(null);
		EmailThread<String, EmailMessage<String>> tempThread = new EmailThread<>();
		
		Collection<MessageDataset<String, String, EmailMessage<String>, EmailThread<String, EmailMessage<String>>>> datasets = new ArrayList<>();
		datasets.add(new ResponseTimeStudyDataSet("response time", new File(
				"data/Email Response Study")));
		ResponseTimeTestbed<String, String, EmailMessage<String>, EmailThread<String, EmailMessage<String>>> testbed = new ResponseTimeTestbed<String, String, EmailMessage<String>, EmailThread<String, EmailMessage<String>>>(
				datasets,
				new InverseGaussianDistribution(50132.4, 113.647),
				new LogNormalDistribution(8.407, 2.87785),
				String.class,
				(Class<EmailMessage<String>>) tempMessage.getClass(),
				(Class<EmailThread<String, EmailMessage<String>>>) tempThread
						.getClass());
		testbed.runTestbed();
	}
}
