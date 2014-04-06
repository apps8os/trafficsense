package org.apps8os.trafficsense.pebble;


import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

/**
 * Pebble communication message spooling facility.
 */
public class MessageManager implements Runnable {
	/**
	 * A Handler for processing Pebble messages. 
	 */
    public Handler messageHandler;
    /**
     * Pebble message queue.
     */
    private final BlockingQueue<PebbleDictionary> messageQueue = new LinkedBlockingQueue<PebbleDictionary>();
    /**
     * Indicates if there are messages awaiting process.
     */
    private volatile Boolean isMessagePending = Boolean.valueOf(false);
    /**
     * Context in which we are running.
     */
    private Context mContext;
    /**
     * Pebble application UUID.
     */
    private UUID mUUID;
    /**
     * Looper of this thread.
     */
    private Looper threadLooper;
    
    /**
     * Constructor.
     * 
     * @param context Context in which we runs.
     * @param uuid Pebble application UUID.
     */
    public MessageManager(Context context, UUID uuid) {
    	mContext = context;
    	mUUID = uuid;
    }
    
    @Override
    public void run() {
        Looper.prepare();
        threadLooper = Looper.myLooper();
        messageHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
            	/**
            	 * Do not use. Messages should be post()-ed.
            	 */
                Log.w(this.getClass().getSimpleName(), "Please post() your blocking runnables to Mr Manager, " +
                        "don't use sendMessage()");
            }
        };
        // Start looping so we do not quit after here.
        Looper.loop();
    }

    /**
     * Consume and send a message to Pebble.
     */
    private void consumeAsync() {
    	messageHandler.post(new Runnable() {
    		@Override
    		public synchronized void run() {
    			if (isMessagePending.booleanValue()) {
    				return;
    			}
    			if (messageQueue.size() == 0) {
    				return;
    			}

    			// DBG stuff
    			long command = messageQueue.peek().getUnsignedInteger(0);
    			System.out.println("DBG MessageManager sent dict to pebble with command " + command);

    			PebbleKit.sendDataToPebble(mContext.getApplicationContext(), mUUID, messageQueue.peek());
    			isMessagePending = Boolean.valueOf(true);
    		}
    	});
    }

    /**
     * Handle ACKs from Pebble.
     * 
     * @param transactionId Pebble communication transaction ID.
     */
    public void notifyAckReceivedAsync(final int transactionId) {
    	messageHandler.post(new Runnable() {
    		@Override
    		public synchronized void run() {
    			isMessagePending = Boolean.valueOf(false);
    			System.out.println("DBG MessageManager Received ack from command: " + messageQueue.peek().getUnsignedInteger(0));
    			// Check this because there might be fragmentation.
    			if (messageQueue.isEmpty() == false)
    			{
    				messageQueue.remove();
    			}
    		}
    	});
    	consumeAsync();
    }

    /**
     * Handle NACK from Pebble.
     * 
     * @param transactionId Pebble communication transaction ID.
     */
    public void notifyNackReceivedAsync(final int transactionId) {
        messageHandler.post(new Runnable() {
            @Override
            public synchronized void run() {
            	isMessagePending = Boolean.valueOf(false);
            }
        });
        consumeAsync();
    }

    /**
     * Add a message to be sent to Pebble.
     * 
     * This method should be used for adding a new message to the queue.
     * Call this whenever you need to send a message to the Pebble application.
     * Note that the message is processed asynchronously.
     *  
     * @param data message to send.
     * @return true if successfully spooled.
     */
    public boolean offer(final PebbleDictionary data) {
        final boolean success = messageQueue.offer(data);
        if (success) {
            consumeAsync();
        }
        return success;
    }
    
    /**
     * Stop Pebble message spooler.
     */
    public void stop() {
    	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 ) {
    		threadLooper.quitSafely();
    	} else {
    		threadLooper.quit();
    	}
    }
}
