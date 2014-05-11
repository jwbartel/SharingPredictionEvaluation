package metrics.recipients;

import general.actionbased.messages.SingleMessage;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import metrics.MetricResult;
import metrics.StatisticsResult;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class RelativeScansMetric<RecipientType, MessageType extends SingleMessage<RecipientType>>
		implements RecipientMetric<RecipientType, MessageType> {

	private DescriptiveStatistics stats = new DescriptiveStatistics();

	public static <RecipientType, MessageType extends SingleMessage<RecipientType>> RecipientMetricFactory<RecipientType, MessageType> factory(
			Class<RecipientType> recipientClass, Class<MessageType> messageClass) {

		return new RecipientMetricFactory<RecipientType, MessageType>() {

			@Override
			public RecipientMetric<RecipientType, MessageType> create() {
				return new RelativeScansMetric<>();
			}
		};
	}

	@Override
	public String getHeader() {
		return "avg-relative scans,stdev-relative scans";
	}

	@Override
	public void addMessageResult(SingleMessage<RecipientType> message,
			Collection<RecipientAddressingEvents> events) {

		int numScans = 0;
		for (RecipientAddressingEvents event : events) {
			if (event == RecipientAddressingEvents.Scan) {
				numScans++;
			}
		}
		
		Set<RecipientType> collaborators = new HashSet<>(message.getCollaborators());
		if (collaborators.size() > 2) {
			stats.addValue((double)numScans / (collaborators.size() - 2));
		}
	}

	@Override
	public MetricResult evaluate(Collection<MessageType> trainMessages,
			Collection<MessageType> testMessages) {
		return new StatisticsResult(stats);
	}

}
