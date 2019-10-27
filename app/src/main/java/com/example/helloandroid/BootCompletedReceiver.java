package com.example.helloandroid;

import android.content.Context;
import android.content.Intent;
import android.util.Log;


public class BootCompletedReceiver extends android.content.BroadcastReceiver {

    private static final String TAG = "HelloAndroid: Receiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "onReceive()");
        String  bootCompIntent = "android.intent.action.BOOT_COMPLETED";
        String action = intent.getAction();

        if (action.equals(bootCompIntent)) {
            Log.i(TAG, "in BOOT_COMPLETED");

            Intent i = new Intent(context, BackgroundService.class);
            context.startService(i);
        }
    }
}

