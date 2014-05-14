package metrics.recipients;

import recommendation.general.actionbased.messages.SingleMessage;

public interface RecipientMetricFactory<V,MessageType extends SingleMessage<V>> {

	public RecipientMetric<V, MessageType> create();

}
