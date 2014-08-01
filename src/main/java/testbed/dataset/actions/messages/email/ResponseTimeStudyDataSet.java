package testbed.dataset.actions.messages.email;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.mail.MessagingException;

import org.apache.commons.io.FileUtils;

import data.representation.actionbased.messages.email.EmailMessage;
import data.representation.actionbased.messages.email.EmailThread;

public class ResponseTimeStudyDataSet
		extends
		EmailDataSet<String, String, EmailMessage<String>, EmailThread<String, EmailMessage<String>>> {

	private static final DateFormat dateFormat = new SimpleDateFormat(
			"EEE MMM dd HH:mm:ss z yyyy");

	public ResponseTimeStudyDataSet(String name, File rootFolder) {
		super(name, getAccountIds(rootFolder), rootFolder);
	}

	private File getEmailThreadsFolder() {
		return new File(getRootFolder(), "email_threads");
	}

	private File getAnonymousDataFolder() {
		return new File(getEmailThreadsFolder(), "anonymous_data");
	}

	private File getPrivateDataFolder() {
		return new File(getEmailThreadsFolder(), "private_data");
	}

	private String getAccountOwnerRecipientId(String account)
			throws IOException {
		File privateFolder = new File(getPrivateDataFolder(), account);
		File summaryFile = new File(privateFolder, "summary.txt");
		File addressesFile = new File(privateFolder, "addresses.txt");
		if (summaryFile.exists() && addressesFile.exists()) {
			String sourceLine = FileUtils.readLines(summaryFile).get(0);
			String sourceEmail = sourceLine.substring("Source email:".length());

			List<String> emailIds = FileUtils.readLines(addressesFile);
			for (String emailId : emailIds) {
				if (emailId.toLowerCase().endsWith(
						":" + sourceEmail.toLowerCase())) {
					String id = emailId.substring(0, emailId.indexOf(':'));
					return id;
				}
			}
		}
		return null;
	}

	private static String[] getAccountIds(File rootFolder) {
		File anonymousDataFolder = new File(rootFolder,
				"email_threads/anonymous_data");

		File[] accountFolders = anonymousDataFolder.listFiles();
		ArrayList<String> accounts = new ArrayList<>();
		for (File accountFolder : accountFolders) {
			if (accountFolder.isDirectory()
					&& accountFolder.getName().matches("\\d+_\\d+")
					&& new File(accountFolder, "messages.txt").exists()) {

				accounts.add(accountFolder.getName());
			}
		}
		
		Collections.sort(accounts);
		return accounts.toArray(new String[0]);
	}

	private String parseValue(String line, String label, String nextLabel) {
		if (line.startsWith("Message:") && line.contains(label)) {

			line = line.substring(line.indexOf(label));
			line = line.substring(label.length());
			if (nextLabel != null) {
				line = line.substring(0, line.indexOf(nextLabel));
				if (!nextLabel.equals(" ")) {
					line = line.substring(0, line.length() - 1);
				}
			}
			return line;
		}
		return null;
	}

	private String parseMessageId(String line) {
		return parseValue(line, "Message:", " ");
	}

	private String parseThreadId(String line) {
		return parseValue(line, "Thread:", "From:");
	}

	private ArrayList<String> parseIndividualsList(String listStr) {
		listStr = listStr.substring(1, listStr.length() - 1);
		String[] parts = listStr.split(",");
		ArrayList<String> individuals = new ArrayList<>();
		for (String part : parts) {
			individuals.add(part);
		}
		return individuals;
	}

	private ArrayList<String> parseFrom(String line) {
		String fromListStr = parseValue(line, "From:", "Recipients:");
		if (fromListStr != null) {
			return parseIndividualsList(fromListStr);
		} else {
			return null;
		}
	}

	private ArrayList<String> parseRecipients(String line) {
		String recipientsListStr = parseValue(line, "Recipients:",
				"Received-Date:");
		if (recipientsListStr != null) {
			return parseIndividualsList(recipientsListStr);
		} else {
			return null;
		}
	}

	private Date parseReceivedDate(String line) {
		String dateStr = parseValue(line, "Received-Date:", null);
		if (dateStr != null) {
			try {
				return dateFormat.parse(dateStr);
			} catch (ParseException e) {
			}
		}
		return null;
	}

	private String parseSubject(String line) {
		return parseValue(line, "Subject:", null);
	}

	private Map<String, String> loadMessageSubjects(File accountAnonymousFolder)
			throws IOException {
		Map<String, String> subjectMap = new TreeMap<>();
		File subjectsFile = new File(accountAnonymousFolder, "subjects.txt");
		if (subjectsFile.exists()) {
			List<String> lines = FileUtils.readLines(subjectsFile);
			for (String line : lines) {
				String messageId = parseMessageId(line);
				String subject = parseSubject(line);
				if (messageId != null && subject != null) {
					subjectMap.put(messageId, subject);
				}
			}
		}
		return subjectMap;
	}

	private Collection<EmailMessage<String>> loadMessages(String account,
			File accountAnonymousFolder, Map<String, String> subjectMap)
			throws IOException {

		String accountOwner = getAccountOwnerRecipientId(account);

		List<EmailMessage<String>> messages = new ArrayList<>();
		File messagesFile = new File(accountAnonymousFolder, "messages.txt");
		List<String> lines = FileUtils.readLines(messagesFile);
		for (String line : lines) {

			String messageId = parseMessageId(line);
			String threadId = parseThreadId(line);
			ArrayList<String> from = parseFrom(line);
			ArrayList<String> recipients = parseRecipients(line);
			Date date = parseReceivedDate(line);
			String subject = subjectMap.get(messageId);

			EmailMessage<String> message = new EmailMessage<String>(messageId,
					threadId, date, from.contains(accountOwner), from,
					recipients, new ArrayList<String>(),
					new ArrayList<String>(), new ArrayList<String>(), subject,
					null);
			messages.add(message);
		}
		Collections.sort(messages, new Comparator<EmailMessage<String>>() {

			@Override
			public int compare(EmailMessage<String> o1, EmailMessage<String> o2) {
				if (!o1.getStartDate().equals(o2.getStartDate())) {
					return o1.getStartDate().compareTo(o2.getStartDate());
				}
				try {
					return o1.getMessageId().compareTo(o2.getMessageId());
				} catch (MessagingException e) {
					e.printStackTrace();
					System.exit(0);
					return 0;
				}
			}
		});
		return messages;
	}

	@Override
	public Collection<EmailMessage<String>> getAllMessages(String account) {

		File accountAnonymousFolder = new File(getAnonymousDataFolder(),
				account);

		try {
			Map<String, String> subjectMap = loadMessageSubjects(accountAnonymousFolder);
			Collection<EmailMessage<String>> messages = loadMessages(account,
					accountAnonymousFolder, subjectMap);
			return messages;
		} catch (IOException e) {
			return null;
		}
	}

	@Override
	public Collection<EmailThread<String, EmailMessage<String>>> getAllThreads(
			String account) {
		
		Collection<EmailMessage<String>> messages = getAllMessages(account);
		
		Map<String, EmailThread<String, EmailMessage<String>>> threadMap =
				new TreeMap<String, EmailThread<String, EmailMessage<String>>>();
		for (EmailMessage<String> message : messages) {
			EmailThread<String, EmailMessage<String>> thread = threadMap.get(message.getThreadId());
			if (thread == null) {
				thread = new EmailThread<>();
				threadMap.put(message.getThreadId(), thread);
			}
			thread.addThreadedAction(message);
		}
		return new ArrayList<>(threadMap.values());
	}

	@Override
	public String parseCollaborator(String collaboratorStr) {
		return collaboratorStr;
	}

}
