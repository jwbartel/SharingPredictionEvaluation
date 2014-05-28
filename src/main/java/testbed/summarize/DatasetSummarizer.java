package testbed.summarize;

import java.io.File;
import java.io.IOException;

public abstract class DatasetSummarizer<AccountType> {

	public abstract void summarize() throws IOException;
	public abstract GroupedRowSummarizer getGroupRowSummarizer(File resultsFile);
	public abstract BestColumnsSummarizer getBestColumnsSummarizer(File resultsFile);
	
	public void summarizeMetricResults(File resultsFile, AccountType[] accounts) throws IOException {
		if (accounts.length > 1) {
			File summarizedFile = new File(resultsFile.getParent(), "summarized - " + resultsFile.getName());
			getGroupRowSummarizer(resultsFile).summarize(summarizedFile);
			
			File bestFile = new File(resultsFile.getParent(), "best - "+summarizedFile.getName());
			getBestColumnsSummarizer(summarizedFile).summarize(bestFile);
		} else {
			File bestFile = new File(resultsFile.getParent(), "best - "+resultsFile.getName());
			getBestColumnsSummarizer(resultsFile).summarize(bestFile);
		}
	}
}
