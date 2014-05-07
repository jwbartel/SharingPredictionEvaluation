package metrics;


public class DoubleResult implements MetricResult {

	private final double value;
	
	public DoubleResult(double value) {
		this.value = value;
	}
	
	public double getValue() {
		return value;
	}
	
	public String toString() {
		return "" + value;
	}
}
