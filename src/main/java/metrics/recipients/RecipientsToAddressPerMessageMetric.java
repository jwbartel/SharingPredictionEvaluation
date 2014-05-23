package metrics.recipients;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import metrics.MetricResult;
import metrics.StatisticsResult;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import data.representation.actionbased.messages.SingleMessage;

public class RecipientsToAddressPerMessageMetric<RecipientType, MessageType extends SingleMessage<RecipientType>>
		implements RecipientMetric<RecipientType, MessageType> {

	private DescriptiveStatistics stats = new DescriptiveStatistics();

	public static <RecipientType, MessageType extends SingleMessage<RecipientType>> RecipientMetricFactory<RecipientType, MessageType> factory(
			Class<RecipientType> recipientClass, Class<MessageType> messageClass) {

		return new RecipientMetricFactory<RecipientType, MessageType>() {

			@Override
			public RecipientMetric<RecipientType, MessageType> create() {
				return new RecipientsToAddressPerMessageMetric<>();
			}
		};
	}

	@Override
	public String getHeader() {
		return "avg-recipients to address per message,stdev-recipients to address per message";
	}

	@Override
	public void addMessageResult(SingleMessage<RecipientType> message,
			Collection<RecipientAddressingEvent> events, int seedSize) {
		
		Set<RecipientType> collaborators = new TreeSet<>(message.getCollaborators());
		if (collaborators.size() > seedSize) {
			stats.addValue(collaborators.size() - seedSize);
		}
	}

	@Override
	public MetricResult evaluate(Collection<MessageType> trainMessages,
			Collection<MessageType> testMessages) {
		return new StatisticsResult(stats);
	}

}
