package testbed.summarize;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import testbed.summarize.PerMessageMetricsMatcher.LabelFinder;

public class PerMessageMetricsCombiner<IdType> {
	File perAccountRootFolder;
	List<IdType> accounts;

	public PerMessageMetricsCombiner(File perAccountRootFolder, List<IdType> accounts) {
		this.perAccountRootFolder = perAccountRootFolder;
		this.accounts = accounts;
	}

	public void combineFiles(File outputFolder) throws IOException {
		File combinedCsvFilesRoot = new File(outputFolder, "combined csv files");
		combineAccountsIntoMultipleCSVs(combinedCsvFilesRoot);
		
		combineAccountsIntoSingleCSV(combinedCsvFilesRoot, new File(outputFolder, "sign test stats.csv"));
	}

	private void combineAccountsIntoMultipleCSVs(File outputCsvFolder) throws IOException {
		int i = 0;
		File baseAccountFolder = null;
		for (; i < accounts.size(); i++) {
			File accountFolder = new File(perAccountRootFolder, "" + accounts.get(i));
			if (accountFolder.exists()) {
				baseAccountFolder = accountFolder;
				break;
			}
		}
		traverseBaseAccountIntoCSV(outputCsvFolder, baseAccountFolder, baseAccountFolder);
	}

	private void traverseBaseAccountIntoCSV(File outputCsvFolder, File baseAccountRoot,
			File baseAccountFile) throws IOException {
		if (baseAccountFile.isDirectory()) {
			for (File subFile : baseAccountFile.listFiles()) {
				traverseBaseAccountIntoCSV(outputCsvFolder, baseAccountRoot, subFile);
			}
		} else {
			String relativePath = baseAccountFile.getAbsolutePath().substring(
					baseAccountRoot.getAbsolutePath().length());

			File outputFile = new File(outputCsvFolder, relativePath);

			List<File> accountCsvFiles = new ArrayList<>();
			for (IdType account : accounts) {
				File accountCsvFile = new File(new File(perAccountRootFolder, "" + account),
						relativePath);
				if (accountCsvFile.exists()) {
					accountCsvFiles.add(accountCsvFile);
				}
			}

			LabelFinder labelFinder = new LabelFinder(accountCsvFiles);
			boolean headerWritten = false;

			for (File accountCsvFile : accountCsvFiles) {
				List<String> accountLines = FileUtils.readLines(accountCsvFile);
				if (!headerWritten) {
					FileUtils.write(outputFile, "," + accountLines.get(0) + "\n");
					FileUtils.write(outputFile, "account," + accountLines.get(1) + "\n", true);
					headerWritten = true;
				}
				for (int i = 2; i < accountLines.size(); i++) {
					FileUtils.write(outputFile, labelFinder.getLabel(accountCsvFile) + ",", true);
					FileUtils.write(outputFile, accountLines.get(i) + "\n", true);
				}
			}
		}
	}

	private void combineAccountsIntoSingleCSV(File combinedCsvFilesRoot, File outputCsvFile)
			throws IOException {
		List<File> fileList = new ArrayList<>();
		addChildrenFiles(combinedCsvFilesRoot, fileList);
		Collections.sort(fileList);

		LabelFinder labelFinder = new LabelFinder(fileList);
		boolean headerWritten = false;
		for (File inputCsvFile : fileList) {
			DescriptiveStatistics[] stats = statsOfDifferences(inputCsvFile);
			if (stats == null) {
				continue;
			}
 			double[][] ltAndGtPercentages = percentagesLtAndGtZeroOfDifferences(inputCsvFile);
			
			String label = labelFinder.getLabel(inputCsvFile);
			int splitPt = label.indexOf('\\');
			String predictor = label.substring(0, splitPt);
			String weights = label.substring(splitPt + 1).replace("half life - ", "")
					.replace("wout - ", ",")
					.replaceAll("\\\\", "");
			
			String numberRow = predictor + "," + weights + ",n";
			String percentLtZeroRow = ",,,percent less than 0";
			String percentGtZeroRow = ",,,percent greater than 0";
			String meanRow = ",,,mean";
			String stdevRow = ",,,stdev";
			
			for (int i = 0; i < stats.length; i++) {
				numberRow += "," + stats[i].getN();
				percentLtZeroRow += "," + ltAndGtPercentages[i][0];
				percentGtZeroRow += "," + ltAndGtPercentages[i][1];
				meanRow += "," + stats[i].getMean();
				stdevRow += "," + stats[i].getStandardDeviation();
			}
			
			if (!headerWritten) {
				List<String> lines = FileUtils.readLines(inputCsvFile);
				int startOfDifferencesColumn = findColumn(lines, "difference", 0);
				String[] headers = getHeaders(lines.get(1), startOfDifferencesColumn);
				String headerRow = "predictor,half life,wOut,";
				for (String header : headers) {
					headerRow += "," + header;
				}
				FileUtils.write(outputCsvFile, headerRow + "\n");
				headerWritten = true;
			}
			FileUtils.write(outputCsvFile, numberRow + "\n", true);
			FileUtils.write(outputCsvFile, percentLtZeroRow + "\n", true);
			FileUtils.write(outputCsvFile, percentGtZeroRow + "\n", true);
			FileUtils.write(outputCsvFile, meanRow + "\n", true);
			FileUtils.write(outputCsvFile, stdevRow + "\n", true);
		}
	}

	private void addChildrenFiles(File candidateFolder, List<File> files) {
		if (candidateFolder.isDirectory()) {
			for (File subFile : candidateFolder.listFiles()) {
				addChildrenFiles(subFile, files);
			}
		} else {
			files.add(candidateFolder);
		}
	}

	private DescriptiveStatistics[] statsOfDifferences(File combinedCSVFile) throws IOException {
		List<String> lines = FileUtils.readLines(combinedCSVFile);

		int startOfDifferencesColumn = findColumn(lines, "difference", 0);
		if (startOfDifferencesColumn == -1) {
			return null;
		}
		String[] headers = getHeaders(lines.get(1), startOfDifferencesColumn);
		DescriptiveStatistics[] retVal = new DescriptiveStatistics[headers.length];
		for (int i = 0; i < retVal.length; i++) {
			retVal[i] = new DescriptiveStatistics();
		}

		for (int i = 2; i < lines.size(); i++) {
			String[] rowVals = lines.get(i).split(",");
			for (int j = startOfDifferencesColumn; j < rowVals.length; j++) {

				String strVal = rowVals[j];
				if (strVal.length() == 0 || strVal.equalsIgnoreCase("nan")) {
					continue;
				}

				int statsIndex = j - startOfDifferencesColumn;
				double val = Double.parseDouble(strVal);
				retVal[statsIndex].addValue(val);
			}
		}
		return retVal;
	}

	private double[][] percentagesLtAndGtZeroOfDifferences(File combinedCSVFile) throws IOException {
		List<String> lines = FileUtils.readLines(combinedCSVFile);

		int startOfDifferencesColumn = findColumn(lines, "difference", 0);
		String[] headers = getHeaders(lines.get(1), startOfDifferencesColumn);
		double[][] retVal = new double[headers.length][2];

		int[] totalCounts = new int[headers.length];
		int[][] percentageCounts = new int[headers.length][2];

		for (int i = 2; i < lines.size(); i++) {
			String[] rowVals = lines.get(i).split(",");
			for (int j = startOfDifferencesColumn; j < rowVals.length; j++) {

				String strVal = rowVals[j];
				if (strVal.length() == 0 || strVal.equalsIgnoreCase("nan")) {
					continue;
				}

				int percentsIndex = j - startOfDifferencesColumn;
				double val = Double.parseDouble(strVal);
				totalCounts[percentsIndex]++;
				if (val < 0) {
					percentageCounts[percentsIndex][0]++;
				} else if (val > 0) {
					percentageCounts[percentsIndex][1]++;
				}
			}
		}

		for (int i = 0; i < totalCounts.length; i++) {
			retVal[i][0] = ((double) percentageCounts[i][0])/totalCounts[i];
			retVal[i][1] = ((double) percentageCounts[i][1])/totalCounts[i];
		}
		return retVal;
	}

	private int findColumn(List<String> lines, String columnLabel, int row) {
		String[] rowVals = lines.get(row).split(",");
		for (int index = 0; index < rowVals.length; index++) {
			String val = rowVals[index];
			if (val.equals(columnLabel)) {
				return index;
			}
		}
		return -1;
	}

	private String[] getHeaders(String headerRow, int startHeadersColumn) {
		String[] headerRowVals = headerRow.split(",");
		return Arrays.copyOfRange(headerRowVals, startHeadersColumn, headerRowVals.length);
	}
}
