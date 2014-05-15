package testbed;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import metrics.Metric;
import metrics.MetricResult;
import metrics.MetricResultCollection;
import metrics.groups.actionbased.ActionBasedGroupMetric;
import metrics.groups.actionbased.AdditionsToUseRecommendationsWithTestActionsMetric;
import metrics.groups.actionbased.DeletionsToUseRecommendationsWithTestActionsMetric;
import metrics.groups.actionbased.RecommendationsMatchedToTestActionMetric;
import metrics.groups.actionbased.RecommendationsToTestActionPerfectMatchesMetric;
import metrics.groups.actionbased.TestActionsMatchedToRecommendationMetric;
import metrics.groups.actionbased.TestActionsToRecommendationPerfectMatchesMetric;
import metrics.groups.actionbased.TotalRecommendedGroupsMetric;
import metrics.groups.actionbased.TotalTestActionsMetric;
import metrics.groups.distance.AddsAndDeletesGroupDistance;
import model.recommendation.groups.ActionBasedSeedlessGroupRecommendationAcceptanceModeler;
import recommendation.general.actionbased.CollaborativeAction;
import recommendation.general.actionbased.graphbuilder.ActionBasedGraphBuilder;
import recommendation.general.actionbased.graphbuilder.SimpleActionBasedGraphBuilder;
import recommendation.general.actionbased.messages.email.EmailMessage;
import recommendation.general.actionbased.messages.email.EmailThread;
import recommendation.groups.seedless.SeedlessGroupRecommenderFactory;
import recommendation.groups.seedless.actionbased.GraphFormingActionBasedSeedlessGroupRecommender;
import recommendation.groups.seedless.fellows.FellowsRecommenderFactory;
import recommendation.groups.seedless.hybrid.HybridRecommenderFactory;
import testbed.dataset.actions.ActionsDataSet;
import testbed.dataset.actions.messages.email.EnronEmailDataSet;

public class ActionBasedSeedlessGroupRecommendationTestBed {

	static double percentTraining = 0.8;

	static Collection<ActionsDataSet<String,String,EmailMessage<String>,EmailThread<String, EmailMessage<String>>>> dataSets = new ArrayList<>();
	
	static Collection<SeedlessGroupRecommenderFactory<String>> seedlessRecommenderFactories = new ArrayList<>();
	static Collection<ActionBasedGraphBuilder<String, CollaborativeAction<String>>> graphBuilders = new ArrayList<>();
	
	static Collection<Double> wOuts = new ArrayList<>();
	static Collection<Double> halfLives = new ArrayList<>();

	static Collection<ActionBasedGroupMetric<String, EmailMessage<String>>> metrics = new ArrayList<>();

	static {

		// Add data sets
		dataSets.add(new EnronEmailDataSet("enron",
				EnronEmailDataSet.DEFAULT_ACCOUNTS, new File("data/Enron")));
		
		// Add seedless recommender factories
		seedlessRecommenderFactories.add(new HybridRecommenderFactory<String>());
//		seedlessRecommender.add(new FellowsRecommenderFactory<String>());
		
		// Add graph builders
		graphBuilders.add(new SimpleActionBasedGraphBuilder<String,CollaborativeAction<String>>());
		//TODO: add remaining graph builders
		
		// Add w_outs
//		wOuts.add(0.25);
//		wOuts.add(0.5);
//		wOuts.add(1.0);
//		wOuts.add(2.0);
//		wOuts.add(4.0);
		
		// Add half lives
//		halfLives.add(1000.0*60); // 1 minute
//		halfLives.add(1000.0*60*60); // 1 hour
//		halfLives.add(1000.0*60*60*24); // 1 day
//		halfLives.add(1000.0*60*60*24*7); // 1 week
//		halfLives.add(1000.0*60*60*24*7*4); // 4 weeks
//		halfLives.add(1000.0*60*60*24*365/2); // 6 months
//		halfLives.add(1000.0*60*60*24*365); // 1 year
//		halfLives.add(1000.0*60*60*24*365*2); // 2 years
		
		// Add metrics
		metrics.add(new TotalTestActionsMetric<String, EmailMessage<String>>());
		metrics.add(new TotalRecommendedGroupsMetric<String, EmailMessage<String>>());
		metrics.add(new DeletionsToUseRecommendationsWithTestActionsMetric<String, EmailMessage<String>>());
		metrics.add(new AdditionsToUseRecommendationsWithTestActionsMetric<String, EmailMessage<String>>());
		metrics.add(new TestActionsMatchedToRecommendationMetric<String, EmailMessage<String>>());
		metrics.add(new TestActionsToRecommendationPerfectMatchesMetric<String, EmailMessage<String>>());
		metrics.add(new RecommendationsMatchedToTestActionMetric<String, EmailMessage<String>>());
		metrics.add(new RecommendationsToTestActionPerfectMatchesMetric<String, EmailMessage<String>>());
		
	}
	
	private static String getHalfLifeName(double halfLife) {
		if (halfLife < 1000) {
			return halfLife + " ms";
		}
		halfLife /= 1000;
		if (halfLife < 60) {
			return halfLife + " seconds";
		}
		halfLife /= 60;
		if (halfLife < 60){
			return halfLife + " minutes";
		}
		halfLife /= 60;
		if (halfLife < 24){
			return halfLife + " hours";
		}
		halfLife /= 24;
		if (halfLife < 7) {
			return halfLife + " days";
		}
		if (halfLife <= 28) {
			return halfLife/7 + " weeks";
		}
		halfLife /= 365;
		return halfLife + " years";
	}
	
	public static void main(String[] args) throws IOException {
		
		for (ActionsDataSet<String,String,EmailMessage<String>,EmailThread<String, EmailMessage<String>>> dataset : dataSets) {
			
			String headerPrefix = ",account";
			MetricResultCollection<String> resultCollection =
					new MetricResultCollection<String>(
							headerPrefix, new ArrayList<Metric>(metrics),
							dataset.getActionBasedSeedlessGroupsMetricsFile());
			
			for (String account : dataset.getAccountIds()) {
				System.out.println(account);
				
				Collection<EmailMessage<String>> trainMessages = dataset.getTrainMessages(account, percentTraining);
				Collection<EmailMessage<String>> testMessages = dataset.getTestMessages(account, percentTraining);
				
				for (SeedlessGroupRecommenderFactory<String> seedlessRecommenderFactory : seedlessRecommenderFactories) {
				for (ActionBasedGraphBuilder<String, CollaborativeAction<String>> graphBuilder : graphBuilders) {
					GraphFormingActionBasedSeedlessGroupRecommender<String> recommender =
							new GraphFormingActionBasedSeedlessGroupRecommender<String>(seedlessRecommenderFactory, graphBuilder);
					
					for (EmailMessage<String> pastAction : trainMessages) {
						recommender.addPastAction(pastAction);
					}

						Collection<Set<String>> recommendations = recommender
								.getRecommendations();
						ActionBasedSeedlessGroupRecommendationAcceptanceModeler<String, String, EmailMessage<String>, EmailThread<String, EmailMessage<String>>> modeler = new ActionBasedSeedlessGroupRecommendationAcceptanceModeler<String, String, EmailMessage<String>, EmailThread<String, EmailMessage<String>>>(
								new AddsAndDeletesGroupDistance<String>(),
								recommendations, new ArrayList<Set<String>>(),
								testMessages, metrics);

						Collection<MetricResult> results = modeler
								.modelRecommendationAcceptance();
						resultCollection.addResults("", account, results);
						
					}
				}
			}
		}
	}
}
