package testbed.dataset.actions.messages.newsgroups;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Properties;
import java.util.TreeSet;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.NewsAddress;

import org.apache.commons.io.FileUtils;

import data.preprocess.threading.JavaMailNewsgroupThreadRetriever;
import data.representation.actionbased.messages.ComparableAddress;
import data.representation.actionbased.messages.newsgroup.JavaMailNewsgroupPost;
import data.representation.actionbased.messages.newsgroup.JavaMailNewsgroupThread;
import data.representation.actionbased.messages.newsgroup.NewsgroupThread;

public class Newsgroups20Dataset extends NewsgroupDataset<Integer, ComparableAddress, JavaMailNewsgroupPost, NewsgroupThread<ComparableAddress,JavaMailNewsgroupPost>> {

	private JavaMailNewsgroupThreadRetriever<JavaMailNewsgroupPost> threadRetriever = new JavaMailNewsgroupThreadRetriever<>();
	private Collection<JavaMailNewsgroupPost> posts;
	private Collection<NewsgroupThread<ComparableAddress, JavaMailNewsgroupPost>> threads;

	public Newsgroups20Dataset(String name, File rootFolder) {
		super(name, new Integer[1], rootFolder);
		try {
			loadPostsAndThreads();
		} catch (MessagingException | IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	public Newsgroups20Dataset(String name, File rootFolder, boolean loadPostsAndThreads) {
		super(name, new Integer[1], rootFolder);
		if (loadPostsAndThreads) {
			try {
				loadPostsAndThreads();
			} catch (MessagingException | IOException e) {
				e.printStackTrace();
				System.exit(0);
			}
		}
	}

	private File getPostsFolder() {
		return new File(getRootFolder(), "posts");
	}

	private MimeMessage loadPost(File location) throws MessagingException,
			IOException {
		Properties properties = System.getProperties();
		properties.setProperty("mail.mime.address.strict", "false");
		Session session = Session.getDefaultInstance(properties);

		String postContents = FileUtils.readFileToString(location);
		MimeMessage post = new MimeMessage(session, new ByteArrayInputStream(
				postContents.getBytes()));
		return post;
	}

	private void loadPosts(File folder) throws MessagingException, IOException {
		if (folder.isFile()) {
			Message message = loadPost(folder);
			posts.add(new JavaMailNewsgroupPost(message, true));
		} else {
			for (File subFolder : folder.listFiles()) {
				loadPosts(subFolder);
			}
		}
	}

	private void loadPostsAndThreads() throws MessagingException, IOException {
		posts = new TreeSet<JavaMailNewsgroupPost>(
				new Comparator<JavaMailNewsgroupPost>() {

					@Override
					public int compare(JavaMailNewsgroupPost arg0,
							JavaMailNewsgroupPost arg1) {
						if (!arg0.getLastActiveDate().equals(
								arg1.getLastActiveDate())) {
							return arg0.getLastActiveDate().compareTo(
									arg1.getLastActiveDate());
						}
						String messageId1 = null;
						try {
							messageId1 = arg0.getMessageId();
						} catch (MessagingException e) {
							e.printStackTrace();
						}
						String messageId2 = null;
						try {
							messageId2 = arg1.getMessageId();
						} catch (MessagingException e) {
							e.printStackTrace();
						}

						if (messageId1 == null || messageId2 == null) {
							if (messageId1 == null && messageId2 == null) {
								return 0;
							} else if (messageId1 == null) {
								return -1;
							} else {
								return 1;
							}
						}
						return messageId1.compareTo(messageId2);
					}
				});
		for (File folder : getPostsFolder().listFiles()) {
			loadPosts(folder);
		}
		threads = new ArrayList<>();
		Collection<JavaMailNewsgroupThread<JavaMailNewsgroupPost>> retrieveThreads = threadRetriever.retrieveThreads(posts);
		threads.addAll(retrieveThreads);
	}

	@Override
	public Collection<JavaMailNewsgroupPost> getAllMessages(Integer account) {
		return new ArrayList<>(posts);
	}

	@Override
	public Collection<NewsgroupThread<ComparableAddress, JavaMailNewsgroupPost>> getAllThreads(
			Integer account) {
		return new ArrayList<>(threads);
	}

	@Override
	public ComparableAddress parseCollaborator(String collaboratorStr) {
		return new ComparableAddress(new NewsAddress(collaboratorStr));
	}

}
