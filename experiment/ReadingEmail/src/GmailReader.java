import java.io.IOException;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;



public class GmailReader {
	
	private Folder inbox;
	private int mailboxMsgCount;
	private int msgPointer;
	
	/**
	 * Initializes the mailbox. This must be called before the other methods can be used.
	 *  The email is assumed to be a gmail mailbox. Username is 
	 * the name of the account and password is the accounts password. The name of the folder
	 * which email is gotten from is assumed to be INBOX. The protocol used to get the email 
	 * is imaps and the email is gotten from imap.gmail.com. 
	 * @param username
	 * @param password
	 * @throws EmailException
	 */
	public void initMailbox(String username, String password) throws EmailException{
		Properties props = new Properties();
        //set protocol
        props.setProperty("mail.store.protocol", "imaps");
        Session session = Session.getInstance(props, null);
        Store store;
		try {
			store = session.getStore();
		} catch (NoSuchProviderException e) {
			throw new EmailException("Error: No such service provider");
		}
        //connect to gmails imap server and logs in with given credentials
        try {
			store.connect("imap.gmail.com", username, password);
		} catch (MessagingException e) {
			throw new EmailException("Error: error connecting to mail server");
		}
        
        //specify the folder from which to get email
        try {
			inbox = store.getFolder("INBOX");
			inbox.open(Folder.READ_ONLY);
		} catch (MessagingException e) {
			throw new EmailException("Error: error opening email folder");
		}
        
        //set the amount of mail in the box. 
        try {
        	mailboxMsgCount = inbox.getMessageCount();
		} catch (MessagingException e) {
			throw new EmailException("Error: error retreiving email message count");
		}
        msgPointer=mailboxMsgCount;
      
       
	}
	
	/**
	 * Gets the next email in the mailbox in descending order from newest to oldest.
	 * If end of mailbox reached, a null is returned. 
	 * @throws EmailException
	 */
	public Email getNextEmail() throws EmailException {
		Message msg;
		//if msgPointer is less than 1 then the end of the mailbox has been reached and 
		//we return a null;
		if(msgPointer<1){
			return null;
		}
		//get the message from the inbox. 
		try {
			msg = inbox.getMessage(msgPointer);
		} catch (MessagingException e) {
			throw new EmailException("Error: error retreiving email");
		}
		
         //create a email object and put the current emails information into it. 
        Address[] in;
        Email email = new Email();
		try {
			email.setSender(msg.getFrom()[0].toString());
			email.setSendTime(msg.getSentDate().toString());
			email.setSubject(msg.getSubject().toString());
	        Multipart mp = (Multipart) msg.getContent();
	        BodyPart bp = mp.getBodyPart(0);
	        email.setContent(bp.getContent().toString());
			
		} catch (Exception e) {
			throw new EmailException("Error: error parsing email information");
		}
		
		//decrease the msgPointer by one so the next getNextEmail call returns the next email. 
		msgPointer--;
		
		//return the email object
		return email;
	}
	
	/**
	 * Returns the number of emails in the mailbox. 
	 * @return
	 */
	public int getMailboxSize(){
        return mailboxMsgCount;
	}
	
	/**
	 * sets the msg pointer to the one defined in the parameter. If the new pointer is less than 1, 
	 * the pointer is set to 1. If the pointer is greater than the number of emails in the box, the 
	 * pointer is set to newesest message in the box. The oldest message in the box is numbered 1 not 0. 
	 * @param number
	 */
	public void setMsgPointer(int number){
		if(number < 1){
			msgPointer=1;
			return;
		}
		if(number > mailboxMsgCount){
			msgPointer = mailboxMsgCount;
		}
		msgPointer = number;
	}
	
	/**
	 * Refreshes the mailbox. 
	 * @throws EmailException
	 */
	public void refreshMailbox() throws EmailException{
        try {
        	mailboxMsgCount = inbox.getMessageCount();
		} catch (MessagingException e) {
			throw new EmailException("Error: error retreiving email message count");
		}
        msgPointer=mailboxMsgCount;
	}
	
	/**
	 *A generic exception for possible errors that could happen in this class. 
	 */
	public class EmailException extends Exception {
		public EmailException() { super(); }
		public EmailException(String message) { super(message); }
		public EmailException(String message, Throwable cause) { super(message, cause); }
		public EmailException(Throwable cause) { super(cause); }
		}
	
}
