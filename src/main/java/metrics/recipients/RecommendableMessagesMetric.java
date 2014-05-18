package metrics.recipients;

import java.util.Collection;

import data.representation.actionbased.messages.SingleMessage;
import metrics.DoubleResult;
import metrics.MetricResult;

public class RecommendableMessagesMetric<RecipientType, MessageType extends SingleMessage<RecipientType>>
		implements RecipientMetric<RecipientType, MessageType> {

	private int numMessages = 0;
	private int numMessagesWithSmallSeeds = 0;

	public static <RecipientType, MessageType extends SingleMessage<RecipientType>> RecipientMetricFactory<RecipientType, MessageType> factory(
			Class<RecipientType> recipientClass, Class<MessageType> messageClass) {

		return new RecipientMetricFactory<RecipientType, MessageType>() {

			@Override
			public RecipientMetric<RecipientType, MessageType> create() {
				return new RecommendableMessagesMetric<>();
			}
		};
	}

	@Override
	public String getHeader() {
		return "recommendable test messages";
	}

	@Override
	public void addMessageResult(SingleMessage<RecipientType> message,
			Collection<RecipientAddressingEvent> events) {

		numMessages++;
		if (events
				.contains(RecipientAddressingEvent.SeedTooSmallForListGeneration)) {
			numMessagesWithSmallSeeds++;
		}
	}

	@Override
	public MetricResult evaluate(Collection<MessageType> trainMessages,
			Collection<MessageType> testMessages) {
		return new DoubleResult(
				(double) (numMessages - numMessagesWithSmallSeeds));
	}

}
