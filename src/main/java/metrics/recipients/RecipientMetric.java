package metrics.recipients;

import java.util.Collection;

import data.representation.actionbased.messages.SingleMessage;
import metrics.Metric;
import metrics.MetricResult;

public interface RecipientMetric<RecipientType, MessageType extends SingleMessage<RecipientType>>
		extends Metric {

	String getHeader();

	public void addMessageResult(SingleMessage<RecipientType> message,
			Collection<RecipientAddressingEvent> events);

	public MetricResult evaluate(Collection<MessageType> trainMessages,
			Collection<MessageType> testMessages);

}
