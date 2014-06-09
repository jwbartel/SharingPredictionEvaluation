package testbed.dataset.actions.messages.stackoverflow.evaluation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import metrics.MetricResult;
import metrics.response.liveness.ResponseLivenessMetric;

import org.apache.commons.io.FileUtils;

import testbed.dataset.actions.messages.stackoverflow.StackOverflowDataset;
import data.representation.actionbased.messages.stackoverflow.StackOverflowMessage;
import data.representation.actionbased.messages.stackoverflow.StackOverflowThread;

public class GradientAscentLivenessEvaluator<Recipient, Message extends StackOverflowMessage<Recipient>, ThreadType extends StackOverflowThread<Recipient, Message>>
		extends LivenessEvaluator<Recipient, Message, ThreadType> {

	private File gradientAscentFolder;
	private Collection<ResponseLivenessMetric> metrics;
	
	public GradientAscentLivenessEvaluator(StackOverflowDataset<Recipient, Message, ThreadType> dataset,
			Collection<ResponseLivenessMetric> metrics) {
		super(dataset);
		this.gradientAscentFolder = new File(livenessFolder, "gradient ascent");
		this.metrics = metrics;
	}
	
	private File getPredictionsFolder() {
		return new File(gradientAscentFolder, "computed liveness");
	}
	
	private List<Double> getPredictedLiveness(Integer test) throws IOException {
		File predictionsFile = new File(getPredictionsFolder(), test + ".csv");
		List<Double> predictions = new ArrayList<Double>();
		List<String> lines = FileUtils.readLines(predictionsFile);
		for (String line : lines) {
			predictions.add(Double.parseDouble(line));
		}
		return predictions;
	}

	@Override
	public List<MetricResult> evaluate() {
		try {
			Map<Integer, List<Double>> testingTimes = dataset.getResponesTimesTestingTimes();
			for (Integer test : testingTimes.keySet()) {
				List<Double> trueTimes = testingTimes.get(test);
				List<Double> predictedLiveness = getPredictedLiveness(test);
				for (ResponseLivenessMetric metric : metrics) {
					metric.addTestResult(trueTimes, predictedLiveness);
				}
			}
			List<MetricResult> results = new ArrayList<>();
			for (ResponseLivenessMetric metric : metrics) {
				results.add(metric.evaluate());
			}
			return results;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
