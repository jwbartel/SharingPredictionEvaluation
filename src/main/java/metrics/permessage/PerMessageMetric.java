package metrics.permessage;

import java.util.Collection;

import metrics.Metric;
import metrics.MetricResult;
import metrics.recipients.RecipientAddressingEvent;
import data.representation.actionbased.messages.SingleMessage;

public abstract class PerMessageMetric<Recipient, Message extends SingleMessage<Recipient>> implements Metric {

	public interface Factory<Recipient, Message extends SingleMessage<Recipient>> {
		
		public PerMessageMetric<Recipient, Message> create();
	}
	
	public abstract MetricResult evaluate(Message message,
			Collection<RecipientAddressingEvent> events, int seedSize);
	
}
