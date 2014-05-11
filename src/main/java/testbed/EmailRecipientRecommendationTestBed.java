package testbed;

import general.actionbased.messages.email.EmailMessage;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import metrics.Metric;
import metrics.MetricResult;
import metrics.MetricResultCollection;
import metrics.recipients.RecipientAddressingEvents;
import metrics.recipients.RecipientMetric;
import metrics.recipients.RecipientMetricFactory;
import recipients.RecipientRecommender;
import recipients.RecipientRecommenderFactory;
import recipients.groupbased.google.GoogleGroupBasedRecipientRecommenderFactory;
import testbed.dataset.messages.email.EmailDataSet;
import testbed.dataset.messages.email.EnronEmailDataSet;

public class EmailRecipientRecommendationTestBed extends
		RecipientRecommendationTestBed {

	static double percentTraining = 0.8;
	static int listSize = 4;

	static Collection<EmailDataSet<String, String>> dataSets = new ArrayList<>();
	static Collection<RecipientRecommenderFactory<String>> recommenderFactories = new ArrayList<>();

	static Collection<RecipientMetricFactory<String, EmailMessage<String>>> metricFactories = new ArrayList<>();

	static {

		// Add data sets
		dataSets.add(new EnronEmailDataSet("enron",
				EnronEmailDataSet.DEFAULT_ACCOUNTS, new File("data/Enron")));

		// Add recommender factories
		recommenderFactories
				.add(new GoogleGroupBasedRecipientRecommenderFactory<String>());
	}

	public static void main(String[] args) {

		for (EmailDataSet<String, String> dataset : dataSets) {

			Collection<RecipientMetric<String, EmailMessage<String>>> unusedMetrics = new ArrayList<>();
			for (RecipientMetricFactory<String, EmailMessage<String>> metricFactory : metricFactories) {
				unusedMetrics.add(metricFactory.create());
			}
			String headerPrefix = "recommendationType,account";
			MetricResultCollection<String> resultCollection = new MetricResultCollection<String>(
					headerPrefix, new ArrayList<Metric>(unusedMetrics));

			for (String account : dataset.getAccountIds()) {

				Collection<EmailMessage<String>> trainingMessages = dataset
						.getTrainMessages(account, percentTraining);
				Collection<EmailMessage<String>> testMessages = dataset
						.getTestMessages(account, percentTraining);

				for (RecipientRecommenderFactory<String> recommenderFactory : recommenderFactories) {
					RecipientRecommender<String> recommender = recommenderFactory
							.createRecommender();

					Collection<RecipientMetric<String, EmailMessage<String>>> metrics = new ArrayList<>();
					for (RecipientMetricFactory<String, EmailMessage<String>> metricFactory : metricFactories) {
						metrics.add(metricFactory.create());
					}

					for (EmailMessage<String> trainingMessage : trainingMessages) {
						recommender.addPastAction(trainingMessage);
					}

					for (EmailMessage<String> testMessage : testMessages) {
						Collection<RecipientAddressingEvents> events = modelSelection(
								testMessage, recommender, listSize);
						for (RecipientMetric<String, EmailMessage<String>> metric : metrics) {
							metric.addMessageResult(testMessage, events);
						}
					}

					Collection<MetricResult> results = new ArrayList<>();
					for (RecipientMetric<String, EmailMessage<String>> metric : metrics) {
						MetricResult result = metric.evaluate(trainingMessages,
								testMessages);
						results.add(result);
					}
					//TODO: add results to result collection

				}

			}
		}
	}

}
