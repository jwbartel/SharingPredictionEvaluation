package metrics.recipients;

import java.util.Collection;

import data.representation.actionbased.messages.SingleMessage;
import metrics.DoubleResult;
import metrics.MetricResult;

public class PrecisionMetric<RecipientType, MessageType extends SingleMessage<RecipientType>>
		implements RecipientMetric<RecipientType, MessageType> {

	private int numNonEmptyLists = 0;
	private int numNonEmptyListsWithCorrectPrediction = 0;

	public static <RecipientType, MessageType extends SingleMessage<RecipientType>> RecipientMetricFactory<RecipientType, MessageType> factory(
			Class<RecipientType> recipientClass, Class<MessageType> messageClass) {

		return new RecipientMetricFactory<RecipientType, MessageType>() {

			@Override
			public RecipientMetric<RecipientType, MessageType> create() {
				return new PrecisionMetric<>();
			}
		};
	}

	@Override
	public String getHeader() {
		return "precision";
	}

	@Override
	public void addMessageResult(SingleMessage<RecipientType> message,
			Collection<RecipientAddressingEvent> events) {

		for (RecipientAddressingEvent event : events) {
			if (event == RecipientAddressingEvent.ListWithNoCorrectEntriesGenerated
					|| event == RecipientAddressingEvent.ListWithCorrectEntriesGenerated) {

				numNonEmptyLists++;
				if (event == RecipientAddressingEvent.ListWithCorrectEntriesGenerated) {
					numNonEmptyListsWithCorrectPrediction++;
				}
			}
		}
	}

	@Override
	public MetricResult evaluate(Collection<MessageType> trainMessages,
			Collection<MessageType> testMessages) {
		return new DoubleResult((double) numNonEmptyListsWithCorrectPrediction
				/ numNonEmptyLists);
	}

}
