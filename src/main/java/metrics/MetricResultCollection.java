package metrics;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class MetricResultCollection<V> {

	private final String headerPrefix;
	private final Collection<Metric> metrics;
	private final Map<String, Map<V, Collection<MetricResult>>> typeToAccountToResult =
			new TreeMap<String, Map<V, Collection<MetricResult>>>();

	public MetricResultCollection(String headerPrefix, Collection<Metric> metrics) {
		this.headerPrefix = headerPrefix;
		this.metrics = metrics;
	}
	
	public void addResults(String type, V account, Collection<MetricResult> results) {
		Map<V, Collection<MetricResult>> accountToResult = typeToAccountToResult.get(type);
		if (accountToResult == null) {
			accountToResult = new HashMap<V, Collection<MetricResult>>();
			typeToAccountToResult.put(type, accountToResult);
		}
		accountToResult.put(account, results);
	}
	
	public String toString() {
		String header = headerPrefix;
		for (Metric metric : metrics) {
			header += "," + metric.getHeader();
		}
		
		String vals = "";
		for (Entry<String, Map<V, Collection<MetricResult>>> entry : typeToAccountToResult.entrySet()) {
			String type = entry.getKey();
			for(Entry<V,Collection<MetricResult>> subEntry : entry.getValue().entrySet()) {
				V account = subEntry.getKey();
				
				vals += "\n" + type + "," + account;
				for(MetricResult result : subEntry.getValue()) {
					vals += "," + result.toString();
				}
			}
		}
		
		return header + vals;
	}

}
