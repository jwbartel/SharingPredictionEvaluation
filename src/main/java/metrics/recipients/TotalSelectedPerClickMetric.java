package metrics.recipients;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import metrics.MetricResult;
import metrics.StatisticsResult;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import data.representation.actionbased.messages.SingleMessage;

public class TotalSelectedPerClickMetric<RecipientType, MessageType extends SingleMessage<RecipientType>>
		implements RecipientMetric<RecipientType, MessageType> {

	private DescriptiveStatistics stats = new DescriptiveStatistics();

	public static <RecipientType, MessageType extends SingleMessage<RecipientType>> RecipientMetricFactory<RecipientType, MessageType> factory(
			Class<RecipientType> recipientClass, Class<MessageType> messageClass) {

		return new RecipientMetricFactory<RecipientType, MessageType>() {

			@Override
			public RecipientMetric<RecipientType, MessageType> create() {
				return new TotalSelectedPerClickMetric<>();
			}
		};
	}

	@Override
	public String getHeader() {
		return "avg-selected per clicks,stdev-selected per clicks";
	}

	@Override
	public void addMessageResult(SingleMessage<RecipientType> message,
			Collection<RecipientAddressingEvent> events) {

		for (RecipientAddressingEvent event : events) {
			if (event == RecipientAddressingEvent.SelectSingleRecipient) {
				stats.addValue(1);
			} else if (event instanceof RecipientAddressingEvent.SelectMultipleRecipientsEvent) {
				stats.addValue(((RecipientAddressingEvent.SelectMultipleRecipientsEvent) event).numRecipients);
			}
		}
	}

	@Override
	public MetricResult evaluate(Collection<MessageType> trainMessages,
			Collection<MessageType> testMessages) {
		return new StatisticsResult(stats);
	}

}
