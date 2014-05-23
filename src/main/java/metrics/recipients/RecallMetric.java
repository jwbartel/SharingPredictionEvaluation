package metrics.recipients;

import java.util.Collection;

import data.representation.actionbased.messages.SingleMessage;
import metrics.DoubleResult;
import metrics.MetricResult;

public class RecallMetric<RecipientType, MessageType extends SingleMessage<RecipientType>>
		implements RecipientMetric<RecipientType, MessageType> {

	private int numNonEmptyLists = 0;
	private int numRequestsForLists = 0;

	public static <RecipientType, MessageType extends SingleMessage<RecipientType>> RecipientMetricFactory<RecipientType, MessageType> factory(
			Class<RecipientType> recipientClass, Class<MessageType> messageClass) {

		return new RecipientMetricFactory<RecipientType, MessageType>() {

			@Override
			public RecipientMetric<RecipientType, MessageType> create() {
				return new RecallMetric<>();
			}
		};
	}

	@Override
	public String getHeader() {
		return "recall";
	}

	@Override
	public void addMessageResult(SingleMessage<RecipientType> message,
			Collection<RecipientAddressingEvent> events, int seedSize) {

		for (RecipientAddressingEvent event : events) {
			if (event == RecipientAddressingEvent.EmptyListGenerated) {
				numRequestsForLists++;
			}
			if (event == RecipientAddressingEvent.ListWithNoCorrectEntriesGenerated
					|| event == RecipientAddressingEvent.ListWithCorrectEntriesGenerated) {

				numNonEmptyLists++;
				numRequestsForLists++;
			}
		}
	}

	@Override
	public MetricResult evaluate(Collection<MessageType> trainMessages,
			Collection<MessageType> testMessages) {
		return new DoubleResult((double) numNonEmptyLists / numRequestsForLists);
	}

}
