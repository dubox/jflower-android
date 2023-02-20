package com.dubox.jflower;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class MyBroadcastReceiver extends BroadcastReceiver {

    public static final String ACTION_COPY = "com.dubox.jflower.ACTION_COPY";

    @Override
    public void onReceive(Context context, Intent intent) {
//        Toast.makeText(context, "呃呃licked", Toast.LENGTH_SHORT).show();
        Log.i("MyBroadcastReceiver",intent.getAction());
        if (intent.getAction().equals(ACTION_COPY)) {
            Toast.makeText(context, "Pause button clicked", Toast.LENGTH_SHORT).show();
        }
    }
}
