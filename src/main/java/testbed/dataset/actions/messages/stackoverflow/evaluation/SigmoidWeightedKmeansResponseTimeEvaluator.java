package testbed.dataset.actions.messages.stackoverflow.evaluation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import metrics.response.time.ResponseTimeMetric;

import org.apache.commons.io.FileUtils;

import testbed.dataset.actions.messages.stackoverflow.StackOverflowDataset;
import data.representation.actionbased.messages.stackoverflow.StackOverflowMessage;
import data.representation.actionbased.messages.stackoverflow.StackOverflowThread;

public class SigmoidWeightedKmeansResponseTimeEvaluator<Recipient, Message extends StackOverflowMessage<Recipient>, ThreadType extends StackOverflowThread<Recipient, Message>>
		extends ResponseTimeEvaluator<Recipient, Message, ThreadType> {

	private int k;
	private File kmeansFolder;
	
	public SigmoidWeightedKmeansResponseTimeEvaluator(
			StackOverflowDataset<Recipient, Message, ThreadType> dataset,
			int k,
			Collection<ResponseTimeMetric> metrics) {
		super(dataset, metrics);
		this.k = k;
		this.kmeansFolder = new File(timeFolder, "k-means");
	}
	
	private Double parseTime(String timeStr) {
		if (timeStr.equals("Inf")) {
			return Double.POSITIVE_INFINITY;
		} else {
			return Double.parseDouble(timeStr);
		}
	}
	
	private Map<Integer,ResponseTimeRange> getClusterToRange(Integer test) throws IOException {
		File clustersFolder = new File(kmeansFolder, "ranges");
		File clustersTestFolder = new File(clustersFolder,  "" + test);
		File clustersFile = new File(clustersTestFolder, k+".csv");
		
		Map<Integer,ResponseTimeRange> retVal = new TreeMap<>();
		List<String> lines = FileUtils.readLines(clustersFile);
		Integer clusterNum = 1;
		for (String line : lines) {
			String[] parts = line.split(",");
			Double minTime = parseTime(parts[0]);
			Double maxTime = parseTime(parts[1]);
			retVal.put(clusterNum, new ResponseTimeRange(minTime, maxTime));
			clusterNum++;
		}
		return retVal;
	}
	
	private List<Integer> getPredictedLabels(Integer test) throws IOException {
		File labelsFolder = new File(kmeansFolder, "test labels");
		File labelsFile = new File(new File(labelsFolder, ""+test), ""+k);
		
		List<Integer> retVal = new ArrayList<>();
		List<String> lines = FileUtils.readLines(labelsFile);
		for (String line : lines) {
			retVal.add(Integer.parseInt(line));
		}
		return retVal;
	}

	@Override
	protected List<ResponseTimeRange> getPredictedResponseTimes(Integer test) throws IOException {
		
		Map<Integer,ResponseTimeRange> clusterToRange = getClusterToRange(test);
		List<Integer> predictedLabels = getPredictedLabels(test);
		
		List<ResponseTimeRange> predictions = new ArrayList<>();
		for (Integer predictedLabel : predictedLabels) {
			predictions.add(clusterToRange.get(predictedLabel));
		}
		return predictions;
	}

	@Override
	public String getType() {
		return "sigmoid weighted k-means";
	}
}
