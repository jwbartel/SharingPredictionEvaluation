package metrics.recipients;

import general.actionbased.messages.SingleMessage;

import java.util.Collection;

import metrics.Metric;
import metrics.MetricResult;

public interface RecipientMetric<V, MessageType extends SingleMessage<V>> extends Metric {

	String getHeader();

	void addMessageResult(SingleMessage<V> message,
			Collection<RecipientAddressingEvents> events);

	MetricResult evaluate(Collection<MessageType> trainMessages,
			Collection<MessageType> testMessages);

}
