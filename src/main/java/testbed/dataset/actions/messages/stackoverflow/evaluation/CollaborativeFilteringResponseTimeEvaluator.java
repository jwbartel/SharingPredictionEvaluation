package testbed.dataset.actions.messages.stackoverflow.evaluation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import metrics.MetricResult;
import metrics.response.time.ResponseTimeMetric;

import org.apache.commons.io.FileUtils;

import prediction.response.time.ResponseTimeRange;
import testbed.dataset.actions.messages.stackoverflow.StackOverflowDataset;
import data.representation.actionbased.messages.stackoverflow.StackOverflowMessage;
import data.representation.actionbased.messages.stackoverflow.StackOverflowThread;

public class CollaborativeFilteringResponseTimeEvaluator<Recipient, Message extends StackOverflowMessage<Recipient>, ThreadType extends StackOverflowThread<Recipient, Message>>
		extends ResponseTimeEvaluator<Recipient, Message, ThreadType> {
	
	public static <Recipient, Message extends StackOverflowMessage<Recipient>, ThreadType extends StackOverflowThread<Recipient, Message>>
	ResponseTimeEvaluatorFactory<Recipient, Message, ThreadType> factory(
			Class<Recipient> recipientClass,
			Class<Message> messageClass,
			Class<ThreadType> threadClass,
			final String featureType,
			final String collaborativeFilteringType) {
		return new ResponseTimeEvaluatorFactory<Recipient, Message, ThreadType>() {

			@Override
			public ResponseTimeEvaluator<Recipient, Message, ThreadType> create(
					StackOverflowDataset<Recipient, Message, ThreadType> dataset,
					Collection<ResponseTimeMetric> metrics) {
				return new CollaborativeFilteringResponseTimeEvaluator<>(featureType, collaborativeFilteringType, dataset, metrics);
			}
		};
	}

	public static class DataPoint<V> {
		public final List<V> vector;
		public final Long responseTime;

		public DataPoint(List<V> vector, Long responseTime) {
			this.vector = vector;
			this.responseTime = responseTime;
		}
	}

	private File collaborativeFilteringResultsFolder;
	private String featureType;
	private String collaborativeFilteringType;

	public CollaborativeFilteringResponseTimeEvaluator(String featureType,
			String collaborativeFilteringType,
			StackOverflowDataset<Recipient, Message, ThreadType> dataset,
			Collection<ResponseTimeMetric> metrics) {
		super(dataset, metrics);
		this.featureType = featureType;
		this.collaborativeFilteringType = collaborativeFilteringType;
		this.collaborativeFilteringResultsFolder = new File(new File(timeFolder, "collaborative filtering"), featureType);
	}

	private Map<Integer,List<DataPoint<Integer>>> getTestPoints() throws IOException {
		
		Map<Integer,List<DataPoint<Integer>>> retVal = new TreeMap<>();
		
		File[] testFolders = collaborativeFilteringResultsFolder.listFiles();
		for (File testFolder : testFolders) {
			Integer testId = Integer.parseInt(testFolder.getName());
			
			File testPointsFile = new File(testFolder,"test.csv");
			List<String> lines = FileUtils.readLines(testPointsFile);
			List<DataPoint<Integer>> dataPoints = new ArrayList<>();
			for (String line : lines) {
				String[] splitLine = line.split(",");
				List<Integer> vector = new ArrayList<>();
				for (int i=0; i<splitLine.length-1; i++) {
					vector.add(Integer.parseInt(splitLine[i]));
				}
				double dblresponseTime = Double.parseDouble(splitLine[splitLine.length-1]);
				Long responseTime = (long) dblresponseTime;
				dataPoints.add(new DataPoint<>(vector, responseTime));
			}
			retVal.put(testId, dataPoints);
		}
		return retVal;
	}
	
	private Map<Integer,List<Double>> getTrueTimes() throws IOException {
		Map<Integer,List<DataPoint<Integer>>> testPoints = getTestPoints();
		Map<Integer, List<Double>> retVal = new TreeMap<>();
		for (Integer test : testPoints.keySet()) {
			List<Double> trueTimes = new ArrayList<>();
			for (DataPoint<Integer> datapoint : testPoints.get(test)) {
				trueTimes.add(datapoint.responseTime/1000.0);
			}
			retVal.put(test, trueTimes);
		}
		return retVal;
	}
	
	@Override
	protected List<ResponseTimeRange> getPredictedResponseTimes(Integer test) throws IOException {
		File testFolder = new File(collaborativeFilteringResultsFolder, ""+test);
		File predictionsFile = new File(testFolder, collaborativeFilteringType + " results.csv");
		List<String> lines = FileUtils.readLines(predictionsFile);
		List<ResponseTimeRange> ranges = new ArrayList<>();
		for (String line : lines) {
			 Double predictedTime = null;
			 if (!line.equals("null")) {
				 predictedTime = Double.parseDouble(line)/1000;
			 }
			 ranges.add(new ResponseTimeRange(predictedTime, null));
		}
		return ranges;
	}
	
	@Override
	public Collection<Integer> getTestIds() throws IOException {
		return getTrueTimes().keySet();
	}

	@Override
	public List<MetricResult> evaluate(Integer testId) {
		try {
			Map<Integer, List<Double>> testingTimes = getTrueTimes();
			List<Double> trueTimes = testingTimes.get(testId);
			List<ResponseTimeRange> predictedResponseTimes = getPredictedResponseTimes(testId);
			for (ResponseTimeMetric metric : metrics) {
				metric.addTestResult(trueTimes, predictedResponseTimes);
			}
			List<MetricResult> results = new ArrayList<>();
			for (ResponseTimeMetric metric : metrics) {
				results.add(metric.evaluate());
			}
			return results;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String getType() {
		return "collaborative filtering " + featureType + "-" + collaborativeFilteringType+",N/A";
	}
}
