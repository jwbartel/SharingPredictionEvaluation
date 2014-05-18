package testbed.dataset.actions.messages.email;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import data.representation.actionbased.messages.email.EmailMessage;
import data.representation.actionbased.messages.email.EmailThread;
import bus.data.structures.AddressLists;

public class EnronEmailDataSet extends EmailDataSet<String, String> {

	public static final String[] DEFAULT_ACCOUNTS = { "allen-p", "arnold-j",
			"arora-h", "badeer-r","bailey-s", "baughman-d", "beck-s",
			"blair-l", "buy-r", "campbell-l", "carson-m", "cash-m",
			"causholli-m", "corman-s", "cuilla-m", "davis-d", "dean-c",
			"delainey-d", "derrick-j", "dickson-s", "donoho-l", "donohoe-t",
			"dorland-c", "ermis-f", "fischer-m", "forney-j", "fossum-d",
			"gang-l", "gay-r", "geaccone-t", "germany-c", "giron-d",
			"griffith-j", "grigsby-m", "guzman-m", "haedicke-m", "hain-m",
			"harris-s", "heard-m", "hernandez-j", "hodge-j", "holst-k",
			"hyatt-k", "hyvl-d", "keavey-p", "keiser-k", "king-j",
			"kuykendall-t", "lavorato-j", "lay-k", "lenhart-m", "lewis-a",
			"linder-e", "lokay-m", "lokey-t", "love-p", "lucci-p", "maggi-m",
			"mann-k", "martin-t", "may-l", "mccarty-d", "mckay-b", "mckay-j",
			"mclaughlin-e", "merriss-s", "meyers-a", "motley-m", "neal-s",
			"panus-s", "parks-j", "pereira-s", "perlingiere-d", "pimenov-v",
			"platter-p", "presto-k", "quenet-j", "quigley-d", "rapp-b",
			"reitmeyer-j", "richey-c", "ring-a", "ring-r", "rodrique-r",
			"rogers-b", "ruscitti-k", "sager-e", "saibi-e", "salisbury-h",
			"sanchez-m", "sanders-r", "scholtes-d", "schoolcraft-d",
			"schwieger-j", "semperger-c", "shankman-j", "shapiro-r",
			"shively-h", "slinger-r", "solberg-g", "south-s", "staab-t",
			"stclair-c", "steffes-j", "stepenovitch-j", "stokley-c",
			"storey-g", "sturm-f", "swerzbin-m", "symes-k", "tholt-j",
			"townsend-j", "tycholiz-b", "ward-k", "watson-k", "white-s",
			"whitt-m", "williams-w3", "wolfe-j", "ybarbo-p", "zipper-a",
			"zufferli-j" };

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
		File addrFile = new File(messageFile.getAbsoluteFile()
				+ "_ADDRESSES.TXT");
		AddressLists addrLists = new AddressLists(addrFile);
		return addrLists;
	}

	private EmailMessage<String> getEmailMessage(File messageFile, String id) {

		try {
			Date date = getMessageDate(messageFile);
			AddressLists addressLists = getAddressLists(messageFile);
			ArrayList<String> from = addressLists.getFrom();
			ArrayList<String> to = addressLists.getTo();
			ArrayList<String> cc = addressLists.getCC();
			ArrayList<String> bcc = addressLists.getBCC();
			boolean wasSent = id.toLowerCase().matches("(.*\\W)?((sent)|(draft)).*");

			return new EmailMessage<>(id, null, date, wasSent, from, to, cc,
					bcc, null, null, null);
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
				continue;
			}
			testMessages.add(message);
			count++;
		}
		return testMessages;
	}

	@Override
	public Collection<EmailThread<String, EmailMessage<String>>> getAllThreads(
			String account) {
		return null;
	}

	@Override
	public Collection<EmailThread<String, EmailMessage<String>>> getTrainThreads(
			String account, double percentTrain) {
		return null;
	}

	@Override
	public Collection<EmailThread<String, EmailMessage<String>>> getTestThreads(
			String account, double percentTest) {
		return null;
	}
	
	private File getMetricsFolder() {
		return new File(getRootFolder(), "metric statistics");
	}
	
	@Override
	public File getRecipientRecommendationMetricsFile() {
		return new File(getMetricsFolder(), "recipient recommendation results.csv");
	}
	
	@Override
	public File getHierarchicalRecipientRecommendationMetricsFile() {
		return new File(getMetricsFolder(), "hierarchical recipient recommendation results.csv");
	}

	@Override
	public File getActionBasedSeedlessGroupsMetricsFile() {
		return new File(getMetricsFolder(), "action based seedless group recommendation results.csv");
	}

}
