package org.apps8os.trafficsense.util;

public class EmailCredential {
	private String mAddress;
	private String mPassword;
	
	public EmailCredential() {
		mAddress = "";
		mPassword = "";
	}
	
	public EmailCredential(String address, String password) {
		mAddress = address;
		mPassword = password;
	}
	
	public String getAddress() {
		return mAddress;
	}
	
	public void setAddress(String address) {
		mAddress = address;
	}
	
	public String getPassword() {
		return mPassword;
	}
	
	public void setPassword(String password) {
		mPassword = password;
	}
}
