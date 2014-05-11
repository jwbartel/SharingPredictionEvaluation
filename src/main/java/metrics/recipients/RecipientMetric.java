package metrics.recipients;

import general.actionbased.messages.SingleMessage;

import java.util.Collection;

import metrics.Metric;
import metrics.MetricResult;

public interface RecipientMetric<RecipientType, MessageType extends SingleMessage<RecipientType>>
		extends Metric {

	String getHeader();

	public void addMessageResult(SingleMessage<RecipientType> message,
			Collection<RecipientAddressingEvents> events);

	public MetricResult evaluate(Collection<MessageType> trainMessages,
			Collection<MessageType> testMessages);

}
