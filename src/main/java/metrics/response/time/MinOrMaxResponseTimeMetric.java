package metrics.response.time;

import prediction.response.time.ResponseTimeRange;

public abstract class MinOrMaxResponseTimeMetric extends ResponseTimeMetric {
	
	public static enum MinOrMaxType {
		Minimum, Maximum
	}

	protected final MinOrMaxType type;
	
	public MinOrMaxResponseTimeMetric(MinOrMaxType type) {
		this.type = type;
	}
	
	protected Double getPredictionValue(ResponseTimeRange predictedRange) {
		if(type == MinOrMaxType.Minimum) {
			return predictedRange.minResponseTime;
		} else if (type == MinOrMaxType.Maximum) {
			return predictedRange.maxResponseTime;
		} else {
			return null;
		}
	}
}
