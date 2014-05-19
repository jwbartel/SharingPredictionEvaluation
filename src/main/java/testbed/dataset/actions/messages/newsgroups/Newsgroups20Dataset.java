package testbed.dataset.actions.messages.newsgroups;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.TreeSet;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import org.apache.commons.io.FileUtils;

import data.preprocess.threading.JavaMailNewsgroupThreadRetriever;
import data.representation.actionbased.messages.newsgroup.JavaMailNewsgroupPost;
import data.representation.actionbased.messages.newsgroup.JavaMailNewsgroupThread;

public class Newsgroups20Dataset extends NewsgroupDataset<Integer, Address, JavaMailNewsgroupPost, JavaMailNewsgroupThread<JavaMailNewsgroupPost>> {

	private JavaMailNewsgroupThreadRetriever<JavaMailNewsgroupPost> threadRetriever = new JavaMailNewsgroupThreadRetriever<>();
	private Collection<JavaMailNewsgroupPost> posts;
	private Collection<JavaMailNewsgroupThread<JavaMailNewsgroupPost>> threads;

	public Newsgroups20Dataset(String name, File rootFolder,
			Class<Integer> genericClass) throws MessagingException, IOException {
		super(name, new Integer[0], rootFolder, genericClass);
		loadPostsAndThreads();
	}

	private File getPostsFolder() {
		return new File(getRootFolder(), "posts");
	}

	private MimeMessage loadPost(File location) throws MessagingException,
			IOException {
		Session session = Session.getDefaultInstance(System.getProperties());

		String postContents = FileUtils.readFileToString(location);
		MimeMessage post = new MimeMessage(session, new ByteArrayInputStream(
				postContents.getBytes()));
		return post;
	}

	private void loadPosts(File folder) throws MessagingException, IOException {
		if (folder.isFile()) {
			Message message = loadPost(folder);
			posts.add(new JavaMailNewsgroupPost(message, false));
		} else {
			for (File subFolder : getPostsFolder().listFiles()) {
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
						if (arg0.getLastActiveDate().equals(
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
		threads = threadRetriever.retrieveThreads(posts);
	}

	@Override
	public Collection<JavaMailNewsgroupPost> getAllMessages(Integer account) {
		return new ArrayList<JavaMailNewsgroupPost>(posts);
	}

	@Override
	public Collection<JavaMailNewsgroupThread<JavaMailNewsgroupPost>> getAllThreads(
			Integer account) {
		return new ArrayList<>(threads);
	}

}
