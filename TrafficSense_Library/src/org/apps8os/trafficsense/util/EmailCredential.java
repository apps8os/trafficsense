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
	 * Constructor.
	 */
	public EmailCredential() {
		mAddress = "";
		mPassword = "";
	}
	
	/**
	 * Instantiate this class with supplied e-mail address and password.
	 * 
	 * @param address e-mail address.
	 * @param password password for the account.
	 */
	public EmailCredential(String address, String password) {
		mAddress = address;
		mPassword = password;
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
}
