package metrics.recipients;

import general.actionbased.messages.SingleMessage;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import metrics.MetricResult;
import metrics.StatisticsResult;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class RelativeSwitchesMetric<RecipientType, MessageType extends SingleMessage<RecipientType>>
		implements RecipientMetric<RecipientType, MessageType> {

	private DescriptiveStatistics stats = new DescriptiveStatistics();

	public static <RecipientType, MessageType extends SingleMessage<RecipientType>> RecipientMetricFactory<RecipientType, MessageType> factory(
			Class<RecipientType> recipientClass, Class<MessageType> messageClass) {

		return new RecipientMetricFactory<RecipientType, MessageType>() {

			@Override
			public RecipientMetric<RecipientType, MessageType> create() {
				return new RelativeSwitchesMetric<>();
			}
		};
	}

	@Override
	public String getHeader() {
		return "avg-relative swiches between keyboard and mouse,stdev-relative swiches between keyboard and mouse";
	}

	@Override
	public void addMessageResult(SingleMessage<RecipientType> message,
			Collection<RecipientAddressingEvent> events) {

		int numSwitches = 0;
		for (RecipientAddressingEvent event : events) {
			if (event == RecipientAddressingEvent.SwitchBetweenClickAndType) {
				numSwitches++;
			}
		}

		Set<RecipientType> collaborators = new HashSet<>(
				message.getCollaborators());
		if (collaborators.size() > 2) {
			stats.addValue((double) numSwitches / (collaborators.size() - 2));
		}
	}

	@Override
	public MetricResult evaluate(Collection<MessageType> trainMessages,
			Collection<MessageType> testMessages) {
		return new StatisticsResult(stats);
	}

}
