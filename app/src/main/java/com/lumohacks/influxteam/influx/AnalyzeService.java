package com.lumohacks.influxteam.influx;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * Created by fabio on 30/01/2016.
 */
public class AnalyzeService extends Service {
    public int counter=0;
    private static String LOG_TAG = "test";
    Context ctx;


    public AnalyzeService(Context applicationContext) {
        super();
        ctx = this;
        Log.i("HERE", "here I am!");
    }

    public AnalyzeService() {}

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        createNotificationChannel();
        startTimer();
        return START_STICKY;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("EXIT", "ondestroy!");
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("uk.ac.shef.oak.ActivityRecognition.RestartSensor");
        broadcastIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        sendBroadcast(broadcastIntent);
        stoptimertask();
    }

    private Timer timer;
    private TimerTask timerTask;
    long oldTime=0;
    public void startTimer() {
        //set a new Timer
        timer = new Timer();

        //initialize the TimerTask's job
        initializeTimerTask();

        //schedule the timer, to wake up every 1 second
        timer.schedule(timerTask, 1000, 1000); //
    }

    /**
     * it sets the timer to print the counter every x seconds 
     */
    public void initializeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {

                /// Add your code to here for the getting data and performing Analyses.
                Log.i("in timer", "in timer ++++  "+ (counter++));


                accessGoogleFit();


                createNotification();
            }
        };
    }



    public void accessGoogleFit() {

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

    private void createNotification(){
        Intent intent = new Intent(this, ConnectFit.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent appOpenIndent = PendingIntent.getActivity(this, 0, intent, 0);

        Intent googleIntent = new Intent(Intent.ACTION_VOICE_COMMAND);
        PendingIntent googleAssistOpenIntent = PendingIntent.getActivity(this, 0, googleIntent, 0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, "1")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("Influx")
                .setContentText("Feeling Stressed??")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .addAction(R.drawable.googleg_standard_color_18, "Google Assist", googleAssistOpenIntent)
                .setContentIntent(appOpenIndent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(2, mBuilder.build());
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "name";
            String description = "desc";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("1", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }



    /**
     * not needed
     */
    public void stoptimertask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}