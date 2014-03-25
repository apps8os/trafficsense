package org.apps8os.trafficsense.exceptions;


/**
 * Invalid case from the JouneyParser
 */
public class InvalidCase extends Exception{

	private String _detailMessage = null ;
	
	public InvalidCase(String detailMessage) {
		super(detailMessage);	
		this._detailMessage = detailMessage ;
	}
} 