package com.example.pebbletest;

import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class MessageManager implements Runnable {
    public Handler messageHandler;
    private final BlockingQueue<PebbleDictionary> messageQueue = new LinkedBlockingQueue<PebbleDictionary>();
    private Boolean isMessagePending = Boolean.valueOf(false);
    private Activity mContext;
    private UUID mUUID;
    
    public MessageManager(Activity context, UUID uuid) {
    	mContext = context;
    	mUUID = uuid;
    }
    
    @Override
    public void run() {
        Looper.prepare();
        messageHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Log.w(this.getClass().getSimpleName(), "Please post() your blocking runnables to Mr Manager, " +
                        "don't use sendMessage()");
            }

        };
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
        final boolean success = messageQueue.offer(data);

        if (success) {
            consumeAsync();
        }

        return success;
    }
}