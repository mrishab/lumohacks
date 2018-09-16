package com.lumohacks.influxteam.influx;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AnalyzeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(AnalyzeReceiver.class.getSimpleName(), "Service Stops! OOPS!!!!");
        Intent i = new Intent(context, AnalyzeService.class);
        context.startService(i);
    }
}