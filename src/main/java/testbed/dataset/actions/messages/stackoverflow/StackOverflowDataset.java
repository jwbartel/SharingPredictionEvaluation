package testbed.dataset.actions.messages.stackoverflow;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import testbed.dataset.actions.messages.MessageDataset;
import data.representation.actionbased.messages.stackoverflow.StackOverflowMessage;
import data.representation.actionbased.messages.stackoverflow.StackOverflowThread;

public abstract class StackOverflowDataset<Recipient, MessageType extends StackOverflowMessage<Recipient>, ThreadType extends StackOverflowThread<Recipient, MessageType>>
		extends MessageDataset<Long, Recipient, MessageType, ThreadType> {

	protected Collection<MessageType> messages = new TreeSet<>();
	protected Map<Long, ThreadType> threads = new TreeMap<>();

	public StackOverflowDataset(String name, Long[] accountIds, File rootFolder) {
		super(name, accountIds, rootFolder);
	}

	@Override
	public Collection<MessageType> getAllMessages(Long account) {
		return new TreeSet<>(messages);
	}

	@Override
	public Collection<ThreadType> getAllThreads(Long account) {
		return new HashSet<>(threads.values());
	}

	public static <Recipient, MessageType extends StackOverflowMessage<Recipient>> Collection<MessageType> getQuestions(
			Collection<MessageType> messages) {
		Collection<MessageType> questions = new ArrayList<>();
		for (MessageType message : messages) {
			if (message.getType() == data.representation.actionbased.messages.stackoverflow.StackOverflowMessage.MessageType.Question) {
				questions.add(message);
			}
		}
		return questions;
	}

	public Collection<MessageType> getTrainQuestions(Long account, double percentTrain) {

		Collection<MessageType> questions = getQuestions(getAllMessages(account));
		return getTrainMessages(questions, percentTrain);
	}

	public Collection<MessageType> getTestQuestions(Long account, double percentTrain) {

		Collection<MessageType> questions = getQuestions(getAllMessages(account));
		return getTestMessages(questions, percentTrain);
	}

	public abstract ThreadType createThread();

	protected void addToThread(MessageType message) {
		Long id = message.getThreadId();
		ThreadType thread = threads.get(id);
		if (thread == null) {
			thread = createThread();
			threads.put(id, thread);
		}
		thread.addThreadedAction(message);
	}
}
