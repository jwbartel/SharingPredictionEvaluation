package testbed.summarize;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;

public class PerMessageMetricsMatcher {

	public static class LabelFinder {
		List<File> inputFiles;
		String commonPrefix;
		String commonSuffix;
		
		public LabelFinder(List<File> inputFiles) {
			this.inputFiles = inputFiles;
		}

		private String findCommonPrefix() {
			if (commonPrefix != null) {
				return commonPrefix;
			}
			for (File inputFile : inputFiles) {
				String inputFileStr = inputFile.toString();
				if (commonPrefix == null) {
					commonPrefix = inputFileStr;
				} else {
					while (!inputFileStr.startsWith(commonPrefix) && commonPrefix.length() > 0) {
						commonPrefix = commonPrefix.substring(0, commonPrefix.length() - 1);
					}
				}
			}
			return commonPrefix;
		}

		private String findCommonSuffix() {
			if (commonSuffix != null) {
				return commonSuffix;
			}
			String commonPrefix = findCommonPrefix();
			for (File inputFile : inputFiles) {
				String inputFileStr = inputFile.toString();
				inputFileStr = inputFileStr.substring(commonPrefix.length());
				if (commonSuffix == null) {
					commonSuffix = inputFileStr;
				} else {
					while (!inputFileStr.endsWith(commonSuffix) && commonSuffix.length() > 0) {
						commonSuffix = commonSuffix.substring(1);
					}
				}
			}
			return commonSuffix;
		}

		public String getLabel(File inputFile) {
			String inputFileStr = inputFile.toString();

			String prefix = findCommonPrefix();
			String suffix = findCommonSuffix();

			return inputFileStr.substring(prefix.length(), inputFileStr.length() - suffix.length());
		}
	}

	List<File> inputFiles = new ArrayList<>();
	LabelFinder labelFinder;

	public void addInputFolder(File inputFolder) {
		inputFiles.add(inputFolder);
	}

	public void addInputFolder(String inputFolder) {
		addInputFolder(new File(inputFolder));
	}

	private String getLabel(File inputFile) {
		if (labelFinder == null) {
			labelFinder = new LabelFinder(inputFiles);
		}
		return labelFinder.getLabel(inputFile);
	}

	private String getHeaders(File inputFile) throws IOException {
		String firstLine = FileUtils.readLines(inputFile).get(0);
		return firstLine.substring(firstLine.indexOf(',') + 1);
	}

	private Map<String, String> sortLinesByMessageId(File inputFile) throws IOException {
		Map<String, String> result = new TreeMap<>();
		List<String> lines = FileUtils.readLines(inputFile);
		for (int i = 1; i < lines.size(); i++) {
			String line = lines.get(i);

			int markerIndex = line.indexOf(',');
			String messageId = line.substring(0, markerIndex);
			String metricResults = line.substring(markerIndex + 1);
			result.put(messageId, metricResults);
		}
		return result;
	}

	private Map<String, Map<String, String>> sortLinesAcrossFiles() throws IOException {
		Map<String, Map<String, String>> sortedLinesAcrossFiles = new TreeMap<>();

		for (File inputFile : inputFiles) {
			String label = getLabel(inputFile);
			Map<String, String> sortedLines = sortLinesByMessageId(inputFile);
			sortedLinesAcrossFiles.put(label, sortedLines);
		}
		return sortedLinesAcrossFiles;
	}

	public void writeMatchedMetrics(File outputFile) throws IOException {
		String headers = getHeaders(inputFiles.get(0));
		int numHeaders = headers.split(",").length;
		Map<String, Map<String, String>> sortedLinesAcrossFiles = sortLinesAcrossFiles();

		List<String> labels = new ArrayList<>(sortedLinesAcrossFiles.keySet());
		Collections.sort(labels);

		String headerLine = "message id";
		String labelLine = ",";
		for (String label : labels) {
			headerLine += "," + headers;
			labelLine += label;
			for (int i = 0; i < numHeaders; i++) {
				labelLine += ",";
			}
		}
		if (labels.size() == 2) {
			labelLine += "difference";
			headerLine += "," + headers;
		} else {
			return;
		}
		FileUtils.write(outputFile, labelLine + "\n");
		FileUtils.write(outputFile, headerLine + "\n", true);
		List<String> outputRows = getOutputDataRows(labels, sortedLinesAcrossFiles, numHeaders);
		FileUtils.writeLines(outputFile, outputRows, true);
	}

	private List<String> getOutputDataRows(List<String> labels,
			Map<String, Map<String, String>> sortedLinesAcrossFiles, int numHeaders) {
		List<String> outputRows = new ArrayList<>();

		for (int i = 0; i < labels.size(); i++) {
			String label = labels.get(0);
			Set<String> messageIds = new TreeSet<>(sortedLinesAcrossFiles.get(label).keySet());

			for (String messageId : messageIds) {
				outputRows.add(getOutputRow(messageId, labels, sortedLinesAcrossFiles, numHeaders));
			}
		}
		
		return outputRows;
	}

	private String getOutputRow(String messageId, List<String> labels,
			Map<String, Map<String, String>> sortedLinesAcrossFiles, int numHeaders) {
		String outputRow = messageId;
		
		List<Double>[] vals = new List[2];
		if (labels.size() == 2) {
			vals[0] = new ArrayList<>();
			vals[1] = new ArrayList<>();
			for(int i=0; i < numHeaders; i++) {
				vals[0].add(null);
				vals[1].add(null);
			}
		}
		for(int i=0; i < labels.size(); i++) {
			String label = labels.get(i);
			Map<String, String> sortedLinesForLabel = sortedLinesAcrossFiles.get(label);
			boolean rowFound = false;
			if (sortedLinesForLabel != null) {
				String labelRow =  sortedLinesForLabel.get(messageId);
				if (labelRow != null) {
					outputRow += "," + labelRow;
					sortedLinesForLabel.remove(messageId);
					rowFound = true;
					if(labels.size() == 2) {
						String[] valStrs = labelRow.split(",");
						for(int valIndex = 0; valIndex < valStrs.length; valIndex++) {
							String valStr = valStrs[valIndex];
							if (valStr.length() > 0) {
								try{
									vals[i].set(valIndex, Double.parseDouble(valStr));
								} catch (NumberFormatException e) {}
							}
						}
					}
				}
			}
			if (!rowFound) {
				for (int j=0; j<numHeaders; j++) {
					outputRow += ",";
				}
			}
		}
		if (labels.size() == 2) {
			for(int valIndex = 0; valIndex < numHeaders; valIndex++) {
				outputRow += ",";
				Double firstVal = vals[0].get(valIndex);
				Double secondVal = vals[1].get(valIndex);
				if (firstVal != null && secondVal != null) {
					outputRow += (firstVal - secondVal);
				}
			}
		}
		return outputRow;
	}
}
