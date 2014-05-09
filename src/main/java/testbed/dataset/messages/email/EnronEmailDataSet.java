package testbed.dataset.messages.email;

import general.actionbased.messages.MessageThread;
import general.actionbased.messages.email.EmailMessage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import bus.data.structures.AddressLists;

public class EnronEmailDataSet extends EmailDataSet<String, String> {

	private final String messageListName;
	private String currentAccountMessages = null;
	private Collection<EmailMessage<String>> currentMessages;

	public EnronEmailDataSet(String name, String[] accountIds, File rootFolder) {
		super(name, accountIds, rootFolder, String.class);
		messageListName = "ALL_MESSAGES_ADAPTED.TXT";
	}

	public EnronEmailDataSet(String name, String[] accountIds, File rootFolder,
			String messageListName) {
		super(name, accountIds, rootFolder, String.class);
		this.messageListName = messageListName;
	}

	private File getAccountsFolder() {
		return new File(getRootFolder(), "account files");
	}

	private File getAccountFolder(String account) {
		return new File(getAccountsFolder(), account);
	}

	protected static Date getMessageDate(File messageFile) throws IOException {
		File dateFile = new File(messageFile.getAbsolutePath() + "_DATE.TXT");

		if (dateFile == null || !dateFile.exists()) {
			return null;
		}

		BufferedReader in = new BufferedReader(new FileReader(dateFile));
		String dateStr = in.readLine();
		in.close();
		if (dateStr == null) {
			return null;
		}

		Date date = new Date(Long.parseLong(dateStr));
		return date;
	}
	
	private AddressLists getAddressLists(File messageFile) throws IOException {
		File addrFile = new File(messageFile.getAbsoluteFile()+"_ADDRESSES.TXT");
		AddressLists addrLists = new AddressLists(addrFile);
		return addrLists;
	}

	private EmailMessage<String> getEmailMessage(File messageFile, String id) {
		
		try {
			Date date = getMessageDate(messageFile);
			AddressLists addressLists = getAddressLists(messageFile);
			String from = addressLists.getFrom().get(0);
			ArrayList<String> to = addressLists.getTo();
			ArrayList<String> cc = addressLists.getCC();
			ArrayList<String> bcc = addressLists.getBCC();
			
			return new EmailMessage<String>(id, null, date, from, to, cc, bcc, null, null);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private void loadMessages(String account) throws IOException {

		File messageListFile = new File(getAccountFolder(account),
				messageListName);
		currentAccountMessages = null;
		BufferedReader in = new BufferedReader(new FileReader(messageListFile));
		int totalMessages = Integer.parseInt(in.readLine());

		currentMessages = new ArrayList<>(totalMessages);
		String line = in.readLine();
		while (line != null) {
			if (!line.startsWith("\t")) {
				File messageFile = new File(getAccountsFolder(), line);
				currentMessages.add(getEmailMessage(messageFile, line));
			}
			line = in.readLine();
		}
		in.close();
		currentAccountMessages = account;
	}

	@Override
	public Collection<EmailMessage<String>> getAllMessages(String account) {

		if (currentAccountMessages == null
				|| !currentAccountMessages.equals(account)) {
			try {
				loadMessages(account);
			} catch (IOException e) {
				return null;
			}
		}
		return currentMessages;
	}

	@Override
	public Collection<EmailMessage<String>> getTrainMessages(String account,
			double percentTrain) {

		Collection<EmailMessage<String>> allMessages = getAllMessages(account);
		if (allMessages == null) {
			return null;
		}

		int numTrain = (int) (allMessages.size() * percentTrain);
		Collection<EmailMessage<String>> trainingMessages = new ArrayList<>();
		for (EmailMessage<String> message : allMessages) {
			if (trainingMessages.size() == numTrain) {
				break;
			}
			trainingMessages.add(message);
		}
		return trainingMessages;
	}

	@Override
	public Collection<EmailMessage<String>> getTestMessages(String account,
			double percentTrain) {
		Collection<EmailMessage<String>> allMessages = getAllMessages(account);
		if (allMessages == null) {
			return null;
		}

		int numTrain = (int) (allMessages.size() * percentTrain);
		Collection<EmailMessage<String>> testMessages = new ArrayList<>();
		int count = 0;
		for (EmailMessage<String> message : allMessages) {
			if (count < numTrain) {
				count++;
				break;
			}
			testMessages.add(message);
			count++;
		}
		return testMessages;
	}

	@Override
	public Collection<MessageThread<String, EmailMessage<String>>> getAllThreads(
			String account) {
		return null;
	}

	@Override
	public Collection<MessageThread<String, EmailMessage<String>>> getTrainThreads(
			String account, double percentTrain) {
		return null;
	}

	@Override
	public Collection<MessageThread<String, EmailMessage<String>>> getTestThreads(
			String account, double percentTest) {
		return null;
	}

}
