package testbed.summarize;

public class SortableColumn implements Comparable<SortableColumn> {
	
	public static enum Order {
		Ascending, Descending,
	}
	
	private final String label;
	private final Order order;
	
	public SortableColumn(String label, Order order) {
		this.label = label;
		this.order = order;
	}

	@Override
	public int compareTo(SortableColumn arg0) {
		return label.compareTo(arg0.label);
	}

	public String getLabel() {
		return label;
	}

	public Order getOrder() {
		return order;
	}
	
	public String toString() {
		return label;
	}
}
