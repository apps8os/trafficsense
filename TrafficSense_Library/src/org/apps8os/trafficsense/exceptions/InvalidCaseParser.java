package org.apps8os.trafficsense.exceptions;


/**
 * Invalid case from the JouneyParser
 */
public class InvalidCaseParser extends Exception{

	private String _detailMessage = null ;
	
	public InvalidCaseParser(String detailMessage) {
		super(detailMessage);	
		this._detailMessage = detailMessage ;
	}
} 