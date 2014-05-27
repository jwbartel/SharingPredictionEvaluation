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

public class CrossAccountSummarizer {

	private final File resultsFile;

	public CrossAccountSummarizer(File resultsFile) {
		this.resultsFile = resultsFile;
	}

	private int columnOfAccount(String header) {
		String[] columns = header.split(",");
		for (int i = 0; i < columns.length; i++) {
			if (columns[i].equals("account")) {
				return i;
			}
		}
		return -1;
	}

	private String getPrefix(String[] columns, int accountColumn) {
		String prefix = "";
		for (int i = 0; i < columns.length; i++) {
			prefix += columns[i];
		}
		return prefix;
	}

	private Map<String, Collection<String[]>> groupRows(int accountColumn,
			List<String> lines) {

		Map<String, Collection<String[]>> retVal = new HashMap<>();

		for (int i = 1; i < lines.size(); i++) {
			String line = lines.get(i);
			String[] lineColumns = line.split(",");
			String prefix = getPrefix(lineColumns, accountColumn);

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
			int accountColumn) {
		ArrayList<String> cleanedHeader = new ArrayList<>();

		for (int i = accountColumn + 1; i < header.length; i++) {
			String headerLabel = header[i];
			if (headerLabel.startsWith("stdev-")) {
				headerLabel = null;
			} else if (headerLabel.startsWith("avg-")) {
				headerLabel = headerLabel
						.substring(headerLabel.indexOf("-") + 1);
			}
			cleanedHeader.add(headerLabel);
		}

		return cleanedHeader;
	}

	private Map<String, Map<Integer, DescriptiveStatistics>> calculateStats(
			int accountColumn, ArrayList<String> cleanedHeaders,
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
					stats.addValue(Double.parseDouble(row[i]));
				}
				columnStats.put(i, stats);
			}
		}
		return retVal;
	}

	private void printStats(File outputFile, ArrayList<String> cleanedHeaders,
			Map<String, Map<Integer, DescriptiveStatistics>> rowsStats)
			throws IOException {

		String header = "";
		for (String cleanedHeader : cleanedHeaders) {
			if (cleanedHeader != null) {
				header += cleanedHeader + ",,,";
			}
		}
		header += "\n";
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
						row += "," + colStats.getN() + "," + colStats.getMean()
								+ "," + colStats.getStandardDeviation();
					}
				}
			}
			FileUtils.write(outputFile, row, true);
		}
	}

	public void summarize(File outputFile) throws IOException {
		List<String> lines = FileUtils.readLines(resultsFile);

		String header = lines.get(0);
		int accountColumn = columnOfAccount(header);
		Map<String, Collection<String[]>> groupedRows = groupRows(
				accountColumn, lines);

		ArrayList<String> cleanedHeaders = getCleanedHeader(header.split(","),
				accountColumn);
		Map<String, Map<Integer, DescriptiveStatistics>> rowsStats = calculateStats(
				accountColumn, cleanedHeaders, groupedRows);
		
		printStats(outputFile, cleanedHeaders, rowsStats);
	}
}
