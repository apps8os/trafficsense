package org.apps8os.trafficsense.exceptions;


/**
 * The Journey text passed to the parser is malformed 
 *
 */
public class MalformedJourneyText extends Exception {

	private String _detailMessage = null;
	
	public MalformedJourneyText() {
	}

	/**
	 * @param detailMessage Specify the exception
	 */
	public MalformedJourneyText(String detailMessage) {
		super(detailMessage);
		this._detailMessage = detailMessage;
	}
}
