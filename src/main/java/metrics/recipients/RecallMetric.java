package metrics.recipients;

import general.actionbased.messages.SingleMessage;

import java.util.Collection;

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
			Collection<RecipientAddressingEvents> events) {

		for (RecipientAddressingEvents event : events) {
			if (event == RecipientAddressingEvents.EmptyListGenerated) {
				numRequestsForLists++;
			}
			if (event == RecipientAddressingEvents.ListWithNoCorrectEntriesGenerated
					|| event == RecipientAddressingEvents.ListWithCorrectEntriesGenerated) {

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
