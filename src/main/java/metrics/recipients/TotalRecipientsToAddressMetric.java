package metrics.recipients;

import general.actionbased.messages.SingleMessage;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import metrics.DoubleResult;
import metrics.MetricResult;

public class TotalRecipientsToAddressMetric<RecipientType, MessageType extends SingleMessage<RecipientType>>
		implements RecipientMetric<RecipientType, MessageType> {

	private int totalRecipients = 0;

	public static <RecipientType, MessageType extends SingleMessage<RecipientType>> RecipientMetricFactory<RecipientType, MessageType> factory(
			Class<RecipientType> recipientClass, Class<MessageType> messageClass) {

		return new RecipientMetricFactory<RecipientType, MessageType>() {

			@Override
			public RecipientMetric<RecipientType, MessageType> create() {
				return new TotalRecipientsToAddressMetric<>();
			}
		};
	}

	@Override
	public String getHeader() {
		return "recipients to address";
	}

	@Override
	public void addMessageResult(SingleMessage<RecipientType> message,
			Collection<RecipientAddressingEvents> events) {

		Set<RecipientType> recipients = new HashSet<>(message.getCollaborators());
		if (recipients.size() > 2) {
			totalRecipients += recipients.size() - 2;
		}
	}

	@Override
	public MetricResult evaluate(Collection<MessageType> trainMessages,
			Collection<MessageType> testMessages) {
		return new DoubleResult(totalRecipients);
	}

}
