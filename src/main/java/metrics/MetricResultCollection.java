package metrics;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;

public class MetricResultCollection<V> {

	private final String headerPrefix;
	private final Collection<Metric> metrics;
	private final Map<String, Map<V, Collection<MetricResult>>> typeToAccountToResult = new TreeMap<String, Map<V, Collection<MetricResult>>>();

	private final File outputFile;
	private boolean wroteHeader = false;

	public MetricResultCollection(String headerPrefix,
			Collection<Metric> metrics) {
		this.headerPrefix = headerPrefix;
		this.metrics = metrics;
		this.outputFile = null;
	}

	public MetricResultCollection(String headerPrefix,
			Collection<Metric> metrics, File outputFile) {
		this.headerPrefix = headerPrefix;
		this.metrics = metrics;
		this.outputFile = outputFile;
	}

	public void addResults(String type, V account,
			Collection<MetricResult> results) throws IOException {
		Map<V, Collection<MetricResult>> accountToResult = typeToAccountToResult
				.get(type);
		if (accountToResult == null) {
			accountToResult = new HashMap<V, Collection<MetricResult>>();
			typeToAccountToResult.put(type, accountToResult);
		}
		if (outputFile != null) {
			write(type, account, results);
		} else {
			accountToResult.put(account, results);
		}
	}

	private void write(String type, V account, Collection<MetricResult> results)
			throws IOException {
		if (!wroteHeader) {
			FileUtils.write(outputFile, getHeader());
			wroteHeader = true;
		}
		FileUtils
				.write(outputFile, "\n" + getRow(type, account, results), true);
	}

	private String getHeader() {
		String header = headerPrefix;
		for (Metric metric : metrics) {
			header += "," + metric.getHeader();
		}
		return header;
	}

	private String getRow(String type, V account,
			Collection<MetricResult> results) {
		String row = type + "," + account;
		for (MetricResult result : results) {
			row += "," + result.toString();
		}
		return row;
	}

	public String toString() {
		String header = getHeader();

		String vals = "";
		for (Entry<String, Map<V, Collection<MetricResult>>> entry : typeToAccountToResult
				.entrySet()) {
			String type = entry.getKey();
			for (Entry<V, Collection<MetricResult>> subEntry : entry.getValue()
					.entrySet()) {
				V account = subEntry.getKey();

				vals += "\n" + getRow(type, account, subEntry.getValue());
			}
		}

		return header + vals;
	}

}
