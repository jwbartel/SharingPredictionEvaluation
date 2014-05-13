package metrics.recipients;

import general.actionbased.messages.SingleMessage;

import java.util.Collection;

import metrics.DoubleResult;
import metrics.MetricResult;

public class TrainWithMultipleFromMetric<RecipientType, MessageType extends SingleMessage<RecipientType>>
		implements RecipientMetric<RecipientType, MessageType> {

	public static <RecipientType, MessageType extends SingleMessage<RecipientType>> RecipientMetricFactory<RecipientType, MessageType> factory(
			Class<RecipientType> recipientClass, Class<MessageType> messageClass) {

		return new RecipientMetricFactory<RecipientType, MessageType>() {

			@Override
			public RecipientMetric<RecipientType, MessageType> create() {
				return new TrainWithMultipleFromMetric<>();
			}
		};
	}

	@Override
	public String getHeader() {
		return "train msgs with multiple from";
	}

	@Override
	public void addMessageResult(SingleMessage<RecipientType> message,
			Collection<RecipientAddressingEvent> events) {
		// Do nothing
	}

	@Override
	public MetricResult evaluate(Collection<MessageType> trainMessages,
			Collection<MessageType> testMessages) {

		int msgsWithMultipleFrom = 0;
		for (MessageType message : trainMessages) {
			if (message.getCreators().size() > 1) {
				msgsWithMultipleFrom++;
			}
		}
		return new DoubleResult((double) msgsWithMultipleFrom
				/ trainMessages.size());
	}

}
