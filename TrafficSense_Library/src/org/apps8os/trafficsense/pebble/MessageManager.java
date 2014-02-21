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

public class MessageManager implements Runnable {
    public Handler messageHandler;
    private final BlockingQueue<PebbleDictionary> messageQueue = new LinkedBlockingQueue<PebbleDictionary>();
    private Boolean isMessagePending = Boolean.valueOf(false);
    private Context mContext;
    private UUID mUUID;
    private Looper threadLooper;
    
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
            	// My guess is that this method should be never used and thus it sends the following message to log
                Log.w(this.getClass().getSimpleName(), "Please post() your blocking runnables to Mr Manager, " +
                        "don't use sendMessage()");
            }

        };
        // Start a loop, so the thread won't quit immediately.
        Looper.loop();
    }

    private void consumeAsync() {
        messageHandler.post(new Runnable() {
            @Override
            public void run() {
                synchronized (isMessagePending) {
                    if (isMessagePending.booleanValue()) {
                        return;
                    }

                    synchronized (messageQueue) {
                        if (messageQueue.size() == 0) {
                            return;
                        }
                        
                        PebbleKit.sendDataToPebble(mContext.getApplicationContext(), mUUID, messageQueue.peek());
                    }

                    isMessagePending = Boolean.valueOf(true);
                }
            }
        });
    }

    public void notifyAckReceivedAsync() {
        messageHandler.post(new Runnable() {
            @Override
            public void run() {
                synchronized (isMessagePending) {
                    isMessagePending = Boolean.valueOf(false);
                }
                //Log.i("Pebble", "Received ack from stop: " + messageQueue.peek().toString());
                //TODO: I guess this is about fragmentation
                if (messageQueue.isEmpty() == false)
                	messageQueue.remove();
            }
        });
        consumeAsync();
    }

    public void notifyNackReceivedAsync() {
        messageHandler.post(new Runnable() {
            @Override
            public void run() {
                synchronized (isMessagePending) {
                    isMessagePending = Boolean.valueOf(false);
                }
            }
        });
        consumeAsync();
    }

    public boolean offer(final PebbleDictionary data) {
    	/**
    	 * This method should be used for adding a new message to the queue.
    	 * Call this whenever you need to send a message to the Pebble app.
    	 */
        final boolean success = messageQueue.offer(data);

        if (success) {
            consumeAsync();
        }

        return success;
    }
    
    public void stop() {
    	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 ) {
    		threadLooper.quitSafely();
    	} else {
    		threadLooper.quit();
    	}
    }
}
