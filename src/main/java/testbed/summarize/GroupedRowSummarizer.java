package testbed.summarize;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class GroupedRowSummarizer implements Summarizer {

	private final File resultsFile;
	private final String ungroupedPrefixColumn;
	private final String lastPrefixColumn;
	private final int offset;

	public GroupedRowSummarizer(File resultsFile, String ungroupedPrefixColumn,
			String lastPrefixColumn) {
		this.resultsFile = resultsFile;
		this.ungroupedPrefixColumn = ungroupedPrefixColumn;
		this.lastPrefixColumn = lastPrefixColumn;
		this.offset = 0;
	}

	public GroupedRowSummarizer(File resultsFile, String ungroupedPrefixColumn,
			String lastPrefixColumn, int offset) {
		this.resultsFile = resultsFile;
		this.ungroupedPrefixColumn = ungroupedPrefixColumn;
		this.lastPrefixColumn = lastPrefixColumn;
		this.offset = offset;
	}

	private int columnOfLabel(String header, String headerLabel) {
		String[] columns = header.split(",");
		for (int i = 0; i < columns.length; i++) {
			if (columns[i].equals(headerLabel)) {
				return i;
			}
		}
		return -1;
	}

	private String getPrefix(String[] columns, int ignoredColumn,
			int lastPrefixColumnPos) {
		String prefix = "";
		for (int i = 0; i < columns.length; i++) {
			if (i != ignoredColumn) {
				prefix += columns[i] + ",";
			}
			if (i == lastPrefixColumnPos) {
				break;
			}
		}
		return prefix;
	}

	protected Map<String, Collection<String[]>> groupRows(int ignoredColumn,
			int lastPrefixColumnPos, List<String> lines) {

		Map<String, Collection<String[]>> retVal = new TreeMap<>();

		int i=1;
		if (offset > 0) {
			i++;
		}
		for (; i < lines.size(); i++) {
			String line = lines.get(i);
			String[] lineColumns = line.split(",");
			String prefix = getPrefix(lineColumns, ignoredColumn,
					lastPrefixColumnPos);

			Collection<String[]> prefixRows = retVal.get(prefix);
			if (prefixRows == null) {
				prefixRows = new ArrayList<String[]>();
				retVal.put(prefix, prefixRows);
			}
			prefixRows.add(lineColumns);
		}

		return retVal;
	}

	private ArrayList<String> getCleanedHeader(String[] header,
			int lastPrefixColumnPos) {
		ArrayList<String> cleanedHeader = new ArrayList<>();

		for (int i = lastPrefixColumnPos + 1; i < header.length; i++) {
			String headerLabel = header[i];
			if (headerLabel.startsWith("stdev-")) {
				headerLabel = null;
			} else if (headerLabel.startsWith("avg-")) {
				headerLabel = headerLabel
						.substring(headerLabel.indexOf("-") + 1);
			}
			if(headerLabel != null && headerLabel.length() > 0) {
				cleanedHeader.add(headerLabel);
			}
		}

		return cleanedHeader;
	}

	private Map<String, Map<Integer, DescriptiveStatistics>> calculateStats(
			int ignoredColumn, ArrayList<String> cleanedHeaders,
			Map<String, Collection<String[]>> groupedRows) {

		Map<String, Map<Integer, DescriptiveStatistics>> retVal = new TreeMap<>();
		for (String rowPrefix : groupedRows.keySet()) {
			Map<Integer, DescriptiveStatistics> columnStats = new TreeMap<>();
			retVal.put(rowPrefix, columnStats);
			for (int i = 0; i < cleanedHeaders.size(); i++) {
				String columnLabel = cleanedHeaders.get(i);
				if (columnLabel == null) {
					continue;
				}

				DescriptiveStatistics stats = new DescriptiveStatistics();
				for (String[] row : groupedRows.get(rowPrefix)) {
					String dataPoint = row[i + ignoredColumn + 1 + offset];
					if (!dataPoint.equalsIgnoreCase("nan")) {
						stats.addValue(Double.parseDouble(dataPoint));
					}
				}
				columnStats.put(i, stats);
			}
		}
		return retVal;
	}

	private void printStats(File outputFile, String[] headers,
			ArrayList<String> cleanedHeaders,
			Map<String, Map<Integer, DescriptiveStatistics>> rowsStats)
			throws IOException {

		String header = "";
		for (String headerPrefixLabel : headers) {
			if (!headerPrefixLabel.equals(ungroupedPrefixColumn)) {
				header += headerPrefixLabel + ",";
			}
			if (headerPrefixLabel.equals(lastPrefixColumn)) {
				break;
			}
		}

		for (String cleanedHeader : cleanedHeaders) {
			if (cleanedHeader != null) {
				header += cleanedHeader + ",,,";
			}
		}

		header += "\n";

		for (String headerPrefixLabel : headers) {
			if (!headerPrefixLabel.equals(ungroupedPrefixColumn)) {
				header += ",";
			}
			if (headerPrefixLabel.equals(lastPrefixColumn)) {
				break;
			}
		}
		for (String cleanedHeader : cleanedHeaders) {
			if (cleanedHeader != null) {
				header += "n,mean,stdev,";
			}
		}
		FileUtils.write(outputFile, header);

		for (String rowPrefix : rowsStats.keySet()) {
			String row = "\n" + rowPrefix;
			for (int col = 0; col < cleanedHeaders.size(); col++) {
				if (cleanedHeaders.get(col) != null) {
					DescriptiveStatistics colStats = rowsStats.get(rowPrefix)
							.get(col);
					if (colStats != null) {
						row += colStats.getN() + "," + colStats.getMean() + ","
								+ colStats.getStandardDeviation() + ",";
					} else {
						row += ",,,";
					}
				}
			}
			FileUtils.write(outputFile, row, true);
		}
	}

	@Override
	public void summarize(File outputFile) throws IOException {
		List<String> lines = FileUtils.readLines(resultsFile);

		String header = lines.get(0);
		int ignoredColumn = columnOfLabel(header, ungroupedPrefixColumn);
		int lastPrefixColumnPos = columnOfLabel(header, lastPrefixColumn);
		Map<String, Collection<String[]>> groupedRows = groupRows(
				ignoredColumn, lastPrefixColumnPos, lines);

		ArrayList<String> cleanedHeaders = getCleanedHeader(header.split(","),
				lastPrefixColumnPos);
		Map<String, Map<Integer, DescriptiveStatistics>> rowsStats = calculateStats(
				ignoredColumn, cleanedHeaders, groupedRows);

		printStats(outputFile, header.split(","), cleanedHeaders, rowsStats);
	}

	public static void main(String[] args) throws IOException {
		File inputFile = new File(
				"/home/bartizzi/Workspaces/SharingPredictionEvaluation/data/"
						+ "Enron/metric statistics/recipient recommendation results.csv");
		File outputFile = new File(
				"/home/bartizzi/Workspaces/SharingPredictionEvaluation/data/"
						+ "Enron/metric statistics/summarized - recipient recommendation results.csv");

		GroupedRowSummarizer summarizer = new GroupedRowSummarizer(inputFile,
				"account", "account");
		summarizer.summarize(outputFile);
	}
}
