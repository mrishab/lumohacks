package com.lumohacks.influxteam.influx;

import android.app.ActivityManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.*;
import com.google.android.gms.fitness.data.*;
import com.google.android.gms.fitness.request.*;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.tasks.*;


import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import java.util.Date;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;




public class ConnectFit extends AppCompatActivity {

    private String LOG_TAG ="test";

    private static final int GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 1;

    Intent mServiceIntent;
    private AnalyzeService mAnalyzeService;
    Context ctx;

    ImageButton button;

    public Context getCtx() {
        return ctx;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctx = this;
        setContentView(R.layout.activity_connect_fit);

        FitnessOptions fitnessOptions = FitnessOptions.builder()
                .addDataType(DataType.TYPE_HEART_RATE_BPM, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.AGGREGATE_HEART_RATE_SUMMARY, FitnessOptions.ACCESS_READ)
                .build();

        if (!GoogleSignIn.hasPermissions(
                GoogleSignIn.getLastSignedInAccount(this),
                fitnessOptions)
        ) {
            GoogleSignIn.requestPermissions(
                    this, // your activity
                    GOOGLE_FIT_PERMISSIONS_REQUEST_CODE,
                    GoogleSignIn.getLastSignedInAccount(this),
                    fitnessOptions);
        } else {
            accessGoogleFit();
        }
        initAnalyzeService();
    }

    public void googleAssistant(View view){

        Intent i = new Intent(Intent.ACTION_VOICE_COMMAND);
        startActivity(i);
    }

    private void initAnalyzeService(){
        mAnalyzeService = new AnalyzeService(getCtx());
        mServiceIntent = new Intent(getCtx(), mAnalyzeService.getClass());
        if (!isMyServiceRunning(mAnalyzeService.getClass())) {
            startService(mServiceIntent);
        }
    }
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i ("isMyServiceRunning?", true+"");
                return true;
            }
        }
        Log.i ("isMyServiceRunning?", false+"");
        return false;
    }

    protected void onDestroy() {
        stopService(mServiceIntent);
        Log.i("MAINACT", "onDestroy!");
        super.onDestroy();

    }

    private void accessGoogleFit() {

        Calendar cal = Calendar.getInstance();
        Date time_now = new Date();
        cal.setTime(time_now);
        long now = cal.getTimeInMillis();

        Date old_time = new Date("Sept 14, 2018");
        cal.setTime(old_time);
        long old = cal.getTimeInMillis();

        DataReadRequest readRequest = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_HEART_RATE_BPM, DataType.AGGREGATE_HEART_RATE_SUMMARY)
                .setTimeRange(old, now, TimeUnit.MILLISECONDS)
                .bucketByTime(5, TimeUnit.HOURS)
                .build();


        Fitness.getHistoryClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .readData(readRequest)
                .addOnSuccessListener(new OnSuccessListener<DataReadResponse>() {
                    @Override
                    public void onSuccess(DataReadResponse dataReadResponse) {
                        int i =0;

                            for (Bucket set: dataReadResponse.getBuckets()) {
                                for(DataSet ds: set.getDataSets())
                                    for(DataPoint dp : ds.getDataPoints())
                                        for (Field field : dp.getDataType().getFields()) {
                                            Log.i("STEPS", "\tField: " + field.getName() + " Value: " + dp.getValue(field));
                                        }
                            }
                        Log.e(LOG_TAG, "success()");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(LOG_TAG, "onFailure()", e);
                    }
                })
                .addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        Log.d(LOG_TAG, "onComplete()");
                        accessGoogleFit();

                    }
                });
    }

    public void watchFunnyVideos(View view){
        Intent webIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("http://www.youtube.com/results?search_query=funny+videos"));
        startActivity(webIntent);
    }

    public void watchMeditationVideo(View view) {
        Intent webIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("https://www.youtube.com/watch?v=6xDyPcJrl0c"));
        startActivity(webIntent);
    }
}
