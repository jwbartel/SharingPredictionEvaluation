package testbed;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import metrics.response.liveness.AccuracyMetric;
import metrics.response.liveness.FalseNegativeRateMetric;
import metrics.response.liveness.FalsePositiveRateMetric;
import metrics.response.liveness.NegativePredictiveValueMetric;
import metrics.response.liveness.PositivePredictiveValueMetric;
import metrics.response.liveness.ResponseLivenessMetricFactory;

import org.apache.commons.io.FileUtils;

import prediction.features.messages.FeatureRuleFactory;
import prediction.features.messages.MessageCollaboratorNumRule;
import prediction.features.messages.MessageCollaboratorsIdsRule;
import prediction.features.messages.MessageCreatorIdRule;
import prediction.features.messages.MessageTitleLengthRule;
import prediction.features.messages.MessageTitleWordIdsRule;
import prediction.features.messages.ThreadSetProperties;
import prediction.response.liveness.message.MessageLivenessPredictor;
import prediction.response.liveness.message.MessageLivenessPredictorFactory;
import prediction.response.liveness.message.MessageRegressionLivenessPredictor;
import snml.rule.basicfeature.IBasicFeatureRule;
import snml.rule.superfeature.model.weka.WekaLinearRegressionModelRule;
import testbed.dataset.actions.messages.stackoverflow.SampledStackOverflowDataset;
import testbed.dataset.actions.messages.stackoverflow.StackOverflowDataset;
import data.representation.actionbased.messages.stackoverflow.StackOverflowMessage;
import data.representation.actionbased.messages.stackoverflow.StackOverflowThread;

public class StackOverflowLivenessTestBed {
	
	static Collection<StackOverflowDataset<String, StackOverflowMessage<String>, StackOverflowThread<String, StackOverflowMessage<String>>>> dataSets = new ArrayList<>();
	static Collection<MessageLivenessPredictorFactory<String, StackOverflowMessage<String>, StackOverflowThread<String, StackOverflowMessage<String>>>> predictorFactories = new ArrayList<>();
	static Collection<FeatureRuleFactory<String, StackOverflowMessage<String>, StackOverflowThread<String, StackOverflowMessage<String>>>> featureFactories = new ArrayList<>();
	static Collection<ResponseLivenessMetricFactory> metricFactories = new ArrayList<>();

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
		featureFactories.add(MessageCreatorIdRule.factory(String.class, StackOverflowMessage.class, StackOverflowThread.class, "creators"));
		featureFactories.add(MessageTitleLengthRule.factory(String.class, StackOverflowMessage.class, StackOverflowThread.class, "titleLength"));
		featureFactories.add(MessageTitleWordIdsRule.factory(String.class, StackOverflowMessage.class, StackOverflowThread.class, "titleWords"));
		
		predictorFactories.add(MessageRegressionLivenessPredictor.factory(new WekaLinearRegressionModelRule("hasResponse"), String.class, StackOverflowMessage.class, StackOverflowThread.class));
		
		metricFactories.add(AccuracyMetric.factory());
		metricFactories.add(FalsePositiveRateMetric.factory());
		metricFactories.add(FalseNegativeRateMetric.factory());
		metricFactories.add(PositivePredictiveValueMetric.factory());
		metricFactories.add(NegativePredictiveValueMetric.factory());
	}
	
	
	public static void main(String[] args) throws Exception {
	
		Set<String> stopWords = new TreeSet<>(FileUtils.readLines(new File("specs/stopwords.txt")));
		
		for (StackOverflowDataset<String, StackOverflowMessage<String>, StackOverflowThread<String, StackOverflowMessage<String>>> dataset : dataSets) {

			Collection<StackOverflowThread<String, StackOverflowMessage<String>>> trainThreads = dataset.getTrainThreads(null, 0.8);
			Collection<StackOverflowThread<String, StackOverflowMessage<String>>> testThreads = dataset.getTestThreads(null, 0.8);
			
			ThreadSetProperties<String, StackOverflowMessage<String>, StackOverflowThread<String, StackOverflowMessage<String>>> threadsProperties = 
					new ThreadSetProperties<>(trainThreads, testThreads, stopWords);
			
			for (MessageLivenessPredictorFactory<String, StackOverflowMessage<String>, StackOverflowThread<String, StackOverflowMessage<String>>> predictorFactory : predictorFactories) {
				
				Collection<IBasicFeatureRule> features = new ArrayList<>();
				for (FeatureRuleFactory<String, StackOverflowMessage<String>, StackOverflowThread<String, StackOverflowMessage<String>>> featureFactory : featureFactories) {
					features.add(featureFactory.create(threadsProperties));
				}
				
				MessageLivenessPredictor<String, StackOverflowMessage<String>, StackOverflowThread<String, StackOverflowMessage<String>>> predictor = predictorFactory.create(features, threadsProperties);
				
				for (StackOverflowThread<String, StackOverflowMessage<String>> thread : trainThreads) {
					predictor.addPastThread(thread);
				}
				
				predictor.evaluate(testThreads);
			}
		}
	}
}
