package metrics.permessage;

import java.util.ArrayList;
import java.util.Collection;

import metrics.MetricResult;
import metrics.recipients.RecipientAddressingEvent;
import metrics.recipients.RecipientMetric;
import metrics.recipients.RecipientMetricFactory;
import data.representation.actionbased.messages.SingleMessage;

public class RecipientPerMessageMetric<Recipient, Message extends SingleMessage<Recipient>>
	extends PerMessageMetric<Recipient, Message> {

	private String header;	
	private RecipientMetricFactory<Recipient, Message> recipientMetricFactory;
	
	public static <Recipient, Message extends SingleMessage<Recipient>> PerMessageMetric.Factory<Recipient, Message> factory(
			final RecipientMetricFactory<Recipient, Message> recipientMetric) {
		return new PerMessageMetric.Factory<Recipient,Message>() {
			
			@Override
			public PerMessageMetric<Recipient,Message> create() {
				return new RecipientPerMessageMetric<>(recipientMetric);
			}
		};
	}
	
	public RecipientPerMessageMetric(
			RecipientMetricFactory<Recipient, Message> recipientMetric) {
		this.recipientMetricFactory = recipientMetric;
		this.header = recipientMetric.create().getHeader();
	}

	@Override
	public String getHeader() {
		return header;
	}

	@Override
	public MetricResult evaluate(Message message,
			Collection<RecipientAddressingEvent> events, int seedSize) {
		
		RecipientMetric<Recipient, Message> recipientMetric = recipientMetricFactory.create();
		recipientMetric.addMessageResult(message, events, seedSize);
		
		Collection<Message> trainMessages = new ArrayList<>();
		Collection<Message> testMessages = new ArrayList<>();
		testMessages.add(message);
		return recipientMetric.evaluate(trainMessages, testMessages);
	}
	
}
