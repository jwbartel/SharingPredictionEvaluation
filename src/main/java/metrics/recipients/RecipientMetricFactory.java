package metrics.recipients;

import data.representation.actionbased.messages.SingleMessage;

public interface RecipientMetricFactory<V,MessageType extends SingleMessage<V>> {

	public RecipientMetric<V, MessageType> create();

}
