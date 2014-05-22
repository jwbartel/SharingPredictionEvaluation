package testbed.dataset.actions.messages.stackoverflow;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;

import data.representation.actionbased.messages.stackoverflow.StackOverflowMessage;
import data.representation.actionbased.messages.stackoverflow.StackOverflowMessage.MessageType;
import data.representation.actionbased.messages.stackoverflow.StackOverflowThread;

public class SampledStackOverflowDataset extends
		StackOverflowDataset<String, StackOverflowMessage<String>, StackOverflowThread<String, StackOverflowMessage<String>>> {
	
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
	
	public SampledStackOverflowDataset(String name, File rootFolder)
			throws IOException, ParseException {
		super(name, new Long[1], rootFolder);
		loadMessages();
	}
	
	public File getInteractionsFolder() {
		return new File(getRootFolder(), "interactions");
	}

	private void loadMessages() throws IOException, ParseException {
		File[] messageFiles = getInteractionsFolder().listFiles();
		Arrays.sort(messageFiles);
		for (File messageFile : messageFiles) {
			long messageId = Long.parseLong(messageFile.getName());
			
			String content = FileUtils.readFileToString(messageFile);

			String dateStr = content.substring(content.indexOf("Date:")+5);
			dateStr = dateStr.substring(0, dateStr.indexOf('\n'));
			Date date = DATE_FORMAT.parse(dateStr);
			
			String title = content.substring(content.indexOf("Title:")+6);
			title = title.substring(0, title.indexOf('\n'));
			
			String owner = null;
			if (content.contains("\nOwner:")) {
				String ownerStr = content.substring(content.indexOf("Owner:")+6);
				ownerStr = ownerStr.substring(0, ownerStr.indexOf('\n'));
				if (!ownerStr.equals("null")) {
					owner = ownerStr;
				}
			}
			
			String threadIdStr = content.substring(content.indexOf("Thread ID:" + 10));
			threadIdStr = threadIdStr.substring(0, threadIdStr.indexOf('\n'));
			long threadId = Long.parseLong(threadIdStr);
			
			String typeStr = content.substring(content.indexOf("Type:") + 5);
			typeStr = typeStr.substring(0, typeStr.indexOf('\n'));
			MessageType type = null;
			if (typeStr.equals("question")) {
				type = MessageType.Question;
			} else if (typeStr.equals("comment")) {
				type = MessageType.Comment;
			} else if (typeStr.equals("answer")) {
				type = MessageType.Answer;
			} else {
				throw new RuntimeException("Unknown type: "+type);
			}
			
			String tagStr = content.substring(content.indexOf("Tags:"));
			tagStr = tagStr.substring(0, tagStr.indexOf('\n'));
			Collection<String> tags = new TreeSet<>();
			if(tagStr.length() > 2) {
				tagStr = tagStr.substring(1,tagStr.length()-1);
				String[] splitTagStr = tagStr.split(",");
				tags.addAll(Arrays.asList(splitTagStr));
			}

			StackOverflowMessage<String> message = new StackOverflowMessage<String>(
					messageId, owner, date, threadId, type, tags, title, false);
			this.messages.add(message);
			addToThread(message);
		}
	}

	@Override
	public StackOverflowThread<String, StackOverflowMessage<String>> createThread() {
		return new StackOverflowThread<>();
	}

}
