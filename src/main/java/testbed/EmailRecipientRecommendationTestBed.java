package testbed;

import general.actionbased.messages.email.EmailMessage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import metrics.Metric;
import metrics.MetricResult;
import metrics.MetricResultCollection;
import metrics.recipients.RecipientMetric;
import metrics.recipients.RecipientMetricFactory;
import model.recommendation.recipients.SingleRecipientRecommendationAcceptanceModeler;

import org.apache.commons.io.FileUtils;

import recipients.RecipientRecommender;
import recipients.RecipientRecommenderFactory;
import recipients.groupbased.google.GoogleGroupBasedRecipientRecommenderFactory;
import testbed.dataset.messages.email.EmailDataSet;
import testbed.dataset.messages.email.EnronEmailDataSet;

public class EmailRecipientRecommendationTestBed {

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

	public static void main(String[] args) throws IOException {

		for (EmailDataSet<String, String> dataset : dataSets) {

			Collection<Metric> unusedMetrics = new ArrayList<>();
			for (RecipientMetricFactory<String, EmailMessage<String>> metricFactory : metricFactories) {
				unusedMetrics.add(metricFactory.create());
			}
			String headerPrefix = "recommendationType,account";
			MetricResultCollection<String> resultCollection =
					new MetricResultCollection<String>(headerPrefix, unusedMetrics);

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

					SingleRecipientRecommendationAcceptanceModeler<String, EmailMessage<String>> modeler = new SingleRecipientRecommendationAcceptanceModeler<>(
							listSize, recommender, trainingMessages,
							testMessages, metrics);
					Collection<MetricResult> results = modeler
							.modelRecommendationAcceptance();

					resultCollection.addResults(
							recommender.getTypeOfRecommender(), account,
							results);

				}

			}
			FileUtils.write(dataset.getRecipientRecommendationMetricsFile(),
					resultCollection.toString());
		}

	}

}
