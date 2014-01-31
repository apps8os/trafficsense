/**
 * 
 */
package org.apps8os.trafficsense.first;

import android.content.Context;

/**
 * Controls the Pebble UI
 *
 */
public class PebbleUiController {
	PebbleCommunication mPebbleCommunication;
	public PebbleUiController(Context applicationContext) {
		mPebbleCommunication = new PebbleCommunication(applicationContext);
	}
}
