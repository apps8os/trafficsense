package org.apps8os.trafficsense.util;

/**
 * Class for e-mail account credentials.
 */
public class EmailCredential {
	/**
	 * The e-mail address. ( for.example @ example.org )
	 */
	private String mAddress;
	/**
	 * Password for this account.
	 */
	private String mPassword;
	/**
	 * IMAP server for this account.
	 */
	private String mImapServer;
	
	/**
	 * Constructor.
	 */
	public EmailCredential() {
		mAddress = "";
		mPassword = "";
		mImapServer = "";
	}
	
	/**
	 * Instantiate this class with supplied information.
	 * The IMAP server must support IMAP over SSL (imaps).
	 * 
	 * @param address e-mail address.
	 * @param password password for the account.
	 * @param imapServer IMAP server for the account.
	 */
	public EmailCredential(String address, String password, String imapServer) {
		mAddress = address;
		mPassword = password;
		mImapServer = imapServer;
	}
	
	/**
	 * Return the e-mail address.
	 * 
	 * @return e-mail address.
	 */
	public String getAddress() {
		return mAddress;
	}
	
	/**
	 * Assign the given e-mail address.
	 * 
	 * @param address e-mail address to assign.
	 */
	public void setAddress(String address) {
		mAddress = address;
	}
	
	/**
	 * Return the password.
	 * 
	 * @return password.
	 */
	public String getPassword() {
		return mPassword;
	}
	
	/**
	 * Assign the given password.
	 * 
	 * @param password password to assign.
	 */
	public void setPassword(String password) {
		mPassword = password;
	}
	
	public String getImapServer() {
		return mImapServer;
	}
	
	/**
	 * Assign the given IMAP server.
	 * Must support IMAP over SSL (imaps).
	 * 
	 * @param imapServer IMAP server.
	 */
	public void setImapServer(String imapServer) {
		mImapServer = imapServer;
	}
}
