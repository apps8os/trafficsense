package org.apps8os.trafficsense.exceptions;

/**
 * The stop code from JourneyParser is not valid
 *
 */
public class StopCodeInvalidParser extends Exception {

	private String _detailMessage = null;
	
	public StopCodeInvalidParser() {
	}

	/**
	 * @param detailMessage Specify the exception
	 */
	public StopCodeInvalidParser(String detailMessage) {
		super(detailMessage);
		this._detailMessage = detailMessage;
	}
}
