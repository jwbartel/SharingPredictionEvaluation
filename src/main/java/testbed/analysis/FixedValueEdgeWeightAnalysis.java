package testbed.analysis;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;

import testbed.dataset.actions.ActionsDataSet;
import testbed.dataset.actions.messages.email.EnronEmailDataSet;

public class FixedValueEdgeWeightAnalysis {
	
	@SuppressWarnings("rawtypes")
	public static void main(String[] args) throws IOException, ParseException {
		Collection<ActionsDataSet> datasets = new ArrayList<>();
		
		//Add datasets
//		datasets.add(new EnronEmailDataSet("enron",
//				EnronEmailDataSet.DEFAULT_ACCOUNTS, new File("data/Enron")));
//		datasets.add(new ResponseTimeStudyDataSet("response time", new File(
//				"data/Email Response Study")));
		datasets.add(new Newsgroups20Dataset("20Newsgroups", new File(
				"data/20 Newsgroups"), false));
//		datasets.add(new SampledStackOverflowDataset("Sampled StackOverflow",
//				new File("data/Stack Overflow/10000 Random Questions"), false));

		String[] halfLives = { "1.0 minutes", "1.0 hours", "1.0 days",
				"1.0 weeks", "4.0 weeks", "0.5 years", "1.0 years", "2.0 years" };
		
		for (ActionsDataSet dataset : datasets) {
			System.out.println(dataset.getName());
			dataset.writeEdgeWeightsWithFixedWOut(5);
			for (String halfLife : halfLives) {
				System.out.println("\t" + halfLife);
				dataset.writeEdgeWeightsWithFixedHalfLife(halfLife,10);
			}
		}																																													
	}
}
