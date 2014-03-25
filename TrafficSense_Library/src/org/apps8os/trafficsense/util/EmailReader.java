package org.apps8os.trafficsense.util;

import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;



public class EmailReader {
	
	private Folder inbox;
	private int mailboxMsgCount;
	private int msgPointer;
	
	/**
	 * Opens a mailbox.
	 * Opens the INBOX of the given e-mail account.
	 * Connects to the server using IMAP over SSL (imaps).
	 * The name of the folder which email is gotten from is assumed to be INBOX.
	 * 
	 * @param credential e-mail credential.
	 * 
	 * @throws EmailException
	 */
	public void initMailbox(EmailCredential credential) throws EmailException {
		Properties props = new Properties();
		props.put("mail.store.protocol", "imaps");
		Session session = Session.getInstance(props, null);
		Store store;
		try {
			store = session.getStore();
		} catch (NoSuchProviderException ex) {
			System.out.println("DBG initMailbox getStore:"+ex.getMessage());
			throw new EmailException("Error: No such service provider");
		}
		// Connect to the server using IMAP over SSL (imaps).
		try {
			store.connect(credential.getImapServer(), credential.getAddress(),
					credential.getPassword());
		} catch (MessagingException e) {
			System.out.println("DBG initMailbox connect(): " + e.getMessage());
			throw new EmailException("Error: error connecting to mail server");
		}

		// specify the folder from which to get email
		try {
			inbox = store.getFolder("INBOX");
			inbox.open(Folder.READ_ONLY);
		} catch (MessagingException e) {
			throw new EmailException("Error: error opening email folder");
		}

		// set the amount of mail in the box.
		try {
			mailboxMsgCount = inbox.getMessageCount();
		} catch (MessagingException e) {
			throw new EmailException(
					"Error: error retreiving email message count");
		}
		msgPointer = mailboxMsgCount;

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
		System.out.println(msg.toString());
         //create a email object and put the current emails information into it. 
        Email email = new Email();
		try {
			email.setSender(msg.getFrom()[0].toString());
			
		} catch (Exception e) {
			throw new EmailException("Error: error parsing sender");
		}
		try {
			email.setSendTime(msg.getSentDate().toString());
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			email.setSubject(msg.getSubject().toString());
		} catch (MessagingException e) {
			throw new EmailException("Error: error parsing subject");
		}
        Multipart mp;
		try {
			Object obj = msg.getContent();
			//get content returns either a multipart or a string object
			//so we need to test which it is. 
			if(obj instanceof Multipart){
				mp = (Multipart) obj;
				BodyPart bp;
				//only showing the first bodypart. There might be more parts 
				//so this may need to be modified
				bp = mp.getBodyPart(0);
				email.setContent(bp.getContent().toString());
			}
			else{
				email.setContent((String) obj);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new EmailException("Error: error 1 parsing content");
			
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
	static public class EmailException extends Exception {
		private static final long serialVersionUID = 1L;
		public EmailException() { super(); }
		public EmailException(String message) { super(message); }
		public EmailException(String message, Throwable cause) { super(message, cause); }
		public EmailException(Throwable cause) { super(cause); }
	}
	
	
}
