package org.apps8os.trafficsense.first;


/**
 * A simple data container for a email. Recognizes that emails have a
 * sender, a send time, a subject, and content. Provides methods to set 
 * and get these parts. 
 * @author Jussi
 *
 */
public class Email {
	private String sender;
	private String content;
	private String sendTime;
	private String subject;
	

	public Email(String sender, String sendTime, String subject, String content){
		setSender(sender);
		setSendTime(sendTime);
		setSubject(subject);
		setContent(content);
	}
	
	public Email(){}
	
	public void setSendTime(String sendTime){
		this.sendTime=sendTime;
	}
	
	public void setContent(String content){
		this.content=content;
	}
	
	public void setSender(String sender){
		this.sender=sender;
	}
	
	public void setSubject(String subject){
		this.subject=subject;
	}
	
	public String getContent(){
		return content;
	}
	
	public String getSendTime(){
		return sendTime;
	}
	
	public String getSender(){
		return sender;
	}
	
	public String getSubject(){
		return subject;
	}
	
	public String toString(){
		return "From: "+sender+"\nTime: "+sendTime+"\nSubject: "+subject+"\nContent:\n"+content ;
		
	}
}
