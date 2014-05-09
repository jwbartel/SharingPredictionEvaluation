package testbed;

import general.actionbased.messages.SingleMessage;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import metrics.recipients.RecipientMetric;
import recipients.RecipientRecommender;
import testbed.dataset.messages.MessageDataSet;
import testbed.dataset.messages.email.EnronEmailDataSet;

public class RecipientRecommendationTestBed {

	static Collection<MessageDataSet<String,String,SingleMessage<String>>> dataSets = new ArrayList<>();
	static Collection<RecipientRecommender<String>> recommenderFactories = new ArrayList<>();

	static Collection<RecipientMetric<String>> metrics = new ArrayList<>();
	
	static {
		dataSets.add(new EnronEmailDataSet("enron", EnronEmailDataSet.DEFAULT_ACCOUNTS, new File("data/Enron")));
	}
	
	
	public static void main(String[] args) {
		
	}
	
}
