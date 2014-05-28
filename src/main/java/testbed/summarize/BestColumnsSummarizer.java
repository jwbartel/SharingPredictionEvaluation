package testbed.summarize;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import testbed.summarize.SortableColumn.Order;

public class BestColumnsSummarizer extends GroupedRowSummarizer {

	private final File resultsFile;
	private final String lastPrefixColumn;
	private final Collection<SortableColumn> columnsToRankBy;
	private final int offset;

	public BestColumnsSummarizer(File resultsFile, String lastPrefixColumn,
			Collection<SortableColumn> columnsToRankBy) {
		super(resultsFile, null, lastPrefixColumn);
		this.resultsFile = resultsFile;
		this.lastPrefixColumn = lastPrefixColumn;
		this.columnsToRankBy = new TreeSet<>(columnsToRankBy);
		this.offset = 0;
	}

	public BestColumnsSummarizer(File resultsFile, String lastPrefixColumn,
			Collection<SortableColumn> columnsToRankBy, int offset) {
		super(resultsFile, null, lastPrefixColumn);
		this.resultsFile = resultsFile;
		this.lastPrefixColumn = lastPrefixColumn;
		this.columnsToRankBy = columnsToRankBy;
		this.offset = offset;
	}

	private int indexOfLabel(String header, String headerLabel) {
		String[] columns = header.split(",");
		for (int i = 0; i < columns.length; i++) {
			if (columns[i].equals(headerLabel)) {
				return i;
			}
		}
		return -1;
	}

	private Double getRankedValueOfRow(int rankIndex, String row) {
		return Double.parseDouble(row.split(",")[rankIndex]);
	}

	private void sortRows(final SortableColumn column, final String header,
			ArrayList<String> rows) {
		final ArrayList<String> originalOrder = new ArrayList<>(rows);
		Collections.sort(rows, new Comparator<String>() {

			private int compare(SortableColumn column, String[] row1, String[] row2)
					throws NumberFormatException {
				int colIndex = indexOfLabel(header, column.getLabel());
				if (colIndex >= 0) {
					colIndex += offset;
					Double row1Val = Double.parseDouble(row1[colIndex]);
					Double row2Val = Double.parseDouble(row2[colIndex]);
					int retVal = row1Val.compareTo(row2Val);
					if (column.getOrder() == Order.Descending) {
						retVal *= -1;
					}
					return retVal;
				}
				return 0;
			}

			@Override
			public int compare(String row1, String row2) {
				String[] splitRow1 = row1.split(",");
				String[] splitRow2 = row2.split(",");

				int compareVal = 0;
				try {
					compareVal = compare(column, splitRow1, splitRow2);
					for (SortableColumn secondarySort : columnsToRankBy) {
						if (compareVal != 0) {
							return compareVal;
						}
						compareVal = compare(secondarySort, splitRow1,
								splitRow2);
					}
					if (compareVal != 0) {
						return compareVal;
					}
				} catch (NumberFormatException e) {
				}
				Integer row1Pos = originalOrder.indexOf(row1);
				Integer row2Pos = originalOrder.indexOf(row2);
				return row1Pos.compareTo(row2Pos);
			}

		});
	}

	private ArrayList<String> initializeOutputLines(String header,
			List<String> lines) {

		ArrayList<String> outputLines = new ArrayList<>();
		outputLines.add("ranking," + header);

		int testIndex = -1;
		for (SortableColumn columnToRankBy : columnsToRankBy) {
			testIndex = indexOfLabel(header, columnToRankBy.getLabel());
			if (testIndex != -1) {
				break;
			}
		}

		int startOfRows = 1;
		while (startOfRows < lines.size()) {
			try {
				getRankedValueOfRow(testIndex, lines.get(startOfRows));
				break;
			} catch (NumberFormatException e) {
				outputLines.add("," + lines.get(startOfRows));
				startOfRows++;
			}
		}
		return outputLines;
	}

	private Map<String, Collection<String[]>> groupRows(String header,
			int startOfRows, String lastPrefixColumn, List<String> lines, ArrayList<String> outputLines) {

		ArrayList<String> cleanedLines = new ArrayList<String>(lines.subList(
				startOfRows, lines.size()));
		cleanedLines.add(0, header);

		int posLastPrefix = indexOfLabel(header, lastPrefixColumn);
		return super.groupRows(-1, posLastPrefix, cleanedLines);
	}

	public void addBestValues(String header, Collection<String[]> rows,
			ArrayList<String> outputLines) {

		for (SortableColumn columnToRankBy : columnsToRankBy) {
			int index = indexOfLabel(header, columnToRankBy.getLabel());
			if (index == -1) {
				continue;
			}
			ArrayList<String> rowList = new ArrayList<>();
			for (String[] row : rows) {
				rowList.add(StringUtils.join(row,","));
			}
			sortRows(columnToRankBy, header, rowList);
			outputLines.add("best " + columnToRankBy + "," + rowList.get(0));
		}
	}

	@Override
	public void summarize(File outputFile) throws IOException {
		List<String> lines = FileUtils.readLines(resultsFile);

		String header = lines.get(0);
		ArrayList<String> outputLines = initializeOutputLines(header, lines);

		Map<String, Collection<String[]>> groupedRows = groupRows(header,
				outputLines.size(), lastPrefixColumn, lines, outputLines);

		for (String prefix : groupedRows.keySet()) {
			Collection<String[]> rows = groupedRows.get(prefix);
			addBestValues(header, rows, outputLines);
		}

		String output = "";
		for (String outputLine : outputLines) {
			output += outputLine + "\n";
		}
		FileUtils.write(outputFile, output);
	}
}
