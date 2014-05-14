package metrics.recipients;

import java.util.Collection;

import recommendation.general.actionbased.messages.SingleMessage;
import metrics.DoubleResult;
import metrics.MetricResult;

public class TotalTestMessagesMetric<RecipientType, MessageType extends SingleMessage<RecipientType>>
		implements RecipientMetric<RecipientType, MessageType> {

	public static <RecipientType, MessageType extends SingleMessage<RecipientType>> RecipientMetricFactory<RecipientType, MessageType> factory(
			Class<RecipientType> recipientClass, Class<MessageType> messageClass) {

		return new RecipientMetricFactory<RecipientType, MessageType>() {

			@Override
			public RecipientMetric<RecipientType, MessageType> create() {
				return new TotalTestMessagesMetric<>();
			}
		};
	}

	@Override
	public String getHeader() {
		return "test messages";
	}

	@Override
	public void addMessageResult(SingleMessage<RecipientType> message,
			Collection<RecipientAddressingEvent> events) {
		// Do nothing
	}

	@Override
	public MetricResult evaluate(Collection<MessageType> trainMessages,
			Collection<MessageType> testMessages) {

		return new DoubleResult(testMessages.size());
	}

}
