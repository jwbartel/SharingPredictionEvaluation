package testbed.analysis;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;

import javax.mail.MessagingException;

import testbed.dataset.actions.messages.MessageDataSet;
import testbed.dataset.actions.messages.email.ResponseTimeStudyDataSet;
import testbed.dataset.actions.messages.newsgroups.Newsgroups20Dataset;

public class ResponseTimesAnalysis {
	
	@SuppressWarnings("rawtypes")
	public static void main(String[] args) throws IOException, ParseException, MessagingException {
		Collection<MessageDataSet> datasets = new ArrayList<>();
		
		//Add datasets
		datasets.add(new ResponseTimeStudyDataSet("response time", new File(
				"data/Email Response Study")));
//		datasets.add(new Newsgroups20Dataset("20Newsgroups", new File(
//				"data/20 Newsgroups"), true));

		for (MessageDataSet dataset : datasets) {
			dataset.writeAllResponseTimes(0.8);
		}
	}

}
