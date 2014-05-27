package testbed.summarize;

import java.io.File;
import java.io.IOException;

public interface Summarizer {

	public void summarize(File outputFile) throws IOException;

}
