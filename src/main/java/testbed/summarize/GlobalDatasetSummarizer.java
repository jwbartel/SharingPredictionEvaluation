package testbed.summarize;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

import testbed.dataset.actions.messages.email.EnronEmailDataSet;
import testbed.dataset.actions.messages.newsgroups.Newsgroups20Dataset;
import testbed.dataset.actions.messages.stackoverflow.SampledStackOverflowDataset;
import testbed.dataset.group.MixedInitiativeDataSet;

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

		//Stack Overflow public data dump
		SampledStackOverflowDataset stackOverflowDataset = new SampledStackOverflowDataset(
				"Sampled StackOverflow", new File(
						"data/Stack Overflow/10000 Random Questions"), false);
		MessageDatasetSummarizer.create(stackOverflowDataset).summarize();
		
		//Mixed initiative Facebook study
		MixedInitiativeDataSet mixedInitiativeDataset = new MixedInitiativeDataSet(
				"mixed_initiative", MixedInitiativeDataSet.DEFAULT_ACCOUNT_SET, new File("data/kelli"));
		GroupDatasetSummarizer.create(mixedInitiativeDataset).summarize();

	}

	public static void main(String[] args) throws IOException, ParseException {
		summarize();
	}
}
