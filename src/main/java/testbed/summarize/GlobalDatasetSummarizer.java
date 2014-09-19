package testbed.summarize;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

import data.representation.actionbased.messages.email.EmailMessage;
import data.representation.actionbased.messages.email.EmailThread;
import testbed.dataset.actions.messages.email.EmailDataSet;
import testbed.dataset.actions.messages.email.EnronEmailDataSet;
import testbed.dataset.actions.messages.email.ResponseTimeStudyDataSet;
import testbed.dataset.actions.messages.newsgroups.Newsgroups20Dataset;
import testbed.dataset.actions.messages.stackoverflow.SampledStackOverflowDataset;
import testbed.dataset.group.MixedInitiativeDataSet;
import testbed.dataset.group.SnapGroupDataSet;

public class GlobalDatasetSummarizer {

	public static void summarize() throws IOException, ParseException {

		//Newsgroups 20
		Newsgroups20Dataset newsgroupDataset = new Newsgroups20Dataset(
				"20Newsgroups", new File("data/20 Newsgroups"), false);
		MessageDatasetSummarizer.create(newsgroupDataset).summarize();

		//Enron email corpus
		EnronEmailDataSet enronDataset = new EnronEmailDataSet("enron",
				EnronEmailDataSet.DEFAULT_ACCOUNTS, new File("data/Enron"));
		MessageDatasetSummarizer.create(enronDataset).summarize();

		//Response time email data set
		EmailDataSet<String, String, EmailMessage<String>, EmailThread<String, EmailMessage<String>>> responseTimeDataset = new ResponseTimeStudyDataSet(
				"response time", new File("data/Email Response Study"));
		MessageDatasetSummarizer.create(responseTimeDataset).summarize();

		//Stack Overflow public data dump
		SampledStackOverflowDataset stackOverflowDataset = new SampledStackOverflowDataset(
				"Sampled StackOverflow", new File(
						"data/Stack Overflow/10000 Random Questions"), false);
		MessageDatasetSummarizer.create(stackOverflowDataset).summarize();
		
		//Mixed initiative Facebook study
		MixedInitiativeDataSet mixedInitiativeDataset = new MixedInitiativeDataSet(
				"mixed_initiative", MixedInitiativeDataSet.DEFAULT_ACCOUNT_SET, new File("data/kelli"));
		GroupDatasetSummarizer.create(mixedInitiativeDataset).summarize();
		
		//Stanford SNAP Facebook study
		SnapGroupDataSet snapDataset = new SnapGroupDataSet("snap_facebook",
				SnapGroupDataSet.DEFAULT_ACCOUNT_SET, new File("data/Stanford_snap/facebook"));
		GroupDatasetSummarizer.create(snapDataset).summarize();

	}

	public static void main(String[] args) throws IOException, ParseException {
		summarize();
	}
}
