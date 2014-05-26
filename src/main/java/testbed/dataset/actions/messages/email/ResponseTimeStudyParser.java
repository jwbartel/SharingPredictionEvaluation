package testbed.dataset.actions.messages.email;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import data.representation.actionbased.messages.email.EmailMessage;

/*
 * Code to extract message data from user study
 * Adapted from code by Andrew Ghobrial
 */
public class ResponseTimeStudyParser {

	public List<EmailMessage<Integer>> parseMessagesFile(File messagesFile) throws IOException {
		List<EmailMessage<Integer>> messages = new ArrayList<>();
        BufferedReader in = new BufferedReader(new FileReader(messagesFile));
        String currLine;
        while((currLine=in.readLine())!=null) {
            try {
                if(!(currLine.substring(0,8)).equalsIgnoreCase("Message:")) {
                    continue;
                }
            } catch (StringIndexOutOfBoundsException e) {
                continue;
            }
            EmailMessage<Integer> message = parseLine(currLine);
            if(message!=null) messages.add(message);
        }
        in.close();
        return sort(messages);
	}
	
	 static EmailMessage<Integer> parseLine(String currLine) {
		 String messageId = null;
		 String threadId = null;
		 ArrayList<Integer> from = new ArrayList<>();
		 ArrayList<Integer> recipients = new ArrayList<>();
		 Date receivedDate = null;
		 
		 // parse message id
		 Pattern pattern = Pattern.compile("Message:(.*?) ");
		 Matcher matcher = pattern.matcher(currLine);
		 if (matcher.find()) {
			 messageId = matcher.group().substring(8).trim();
		 }
		 // parse thread id
		 pattern = Pattern.compile("Thread:(.*?) ");
		 matcher = pattern.matcher(currLine);
		 if (matcher.find()) {
			 threadId = matcher.group().substring(7).trim();
		 }
		 
		 // parse from id
		 pattern = Pattern.compile("From:(.*?) ");
		 matcher = pattern.matcher(currLine);
		 if(matcher.find()){
			 String fromIdStr = matcher.group().substring(5).replaceAll("\\[", "").replaceAll("\\]", "").trim();
			 if(fromIdStr.equals("")) {
				 System.out.println("No From Id found!");
			}else{
				from.add(Integer.parseInt(fromIdStr));
			}
		 }
		 // parse recipients
		 pattern = Pattern.compile("Recipients:(.*?) ");
		 matcher = pattern.matcher(currLine);
		 if(matcher.find()) {
			 String[] sa = matcher.group().substring(11).replaceAll("\\[", "").replaceAll("\\]", "").trim().split(",");
			 ArrayList<Integer> ial = new ArrayList<Integer>(sa.length);
			 for(int j=0;j<sa.length;j++) if(sa[j].equalsIgnoreCase("")) continue; else {
				 ial.add(j,Integer.parseInt(sa[j]));
			 }
			 recipients = ial;
		 }
		 // received date
		 pattern = Pattern.compile("Received-Date:.*");
		 matcher = pattern.matcher(currLine);
		 if(matcher.find()) {
			 String dateString = matcher.group().substring(14).trim();
			 SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
			 Date parsedDate = null;
			 try {
				 parsedDate = formatter.parse(dateString);
			 } catch (ParseException e) {
				 e.printStackTrace();
			 }
			receivedDate = parsedDate;
		 }
		return new EmailMessage<Integer>(messageId, threadId, receivedDate,
				false, from, recipients, new ArrayList<Integer>(),
				new ArrayList<Integer>(), new ArrayList<Integer>(), null, null);
	 }
	 
	 static List<EmailMessage<Integer>> sort(List<EmailMessage<Integer>> messages) {
		 Collections.sort(messages, new Comparator<EmailMessage<Integer>>() {
			 @Override
			 public int compare(EmailMessage<Integer> m1, EmailMessage<Integer> m2) {
				 return m1.getLastActiveDate().compareTo(m2.getLastActiveDate());
			 }
		 });
		 return messages;
	 }
}
