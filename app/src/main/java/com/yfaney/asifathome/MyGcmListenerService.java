package com.yfaney.asifathome;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

public class MyGcmListenerService extends GcmListenerService {
    private static final String TAG = "MyGcmListenerService";


    // [START receive_message]
    @Override
    public void onMessageReceived(String from, Bundle extras) {
//        String message = extras.getString("message");
//        Log.d(TAG, "From: " + from);
//        Log.d(TAG, "Message: " + message);

        /**
         * Production applications would usually process the message here.
         * Eg: - Syncing with server.
         *     - Store message in local database.
         *     - Update UI.
         */

        /**
         * In some cases it may be useful to show a notification indicating to the user
         * that a message was received.
         */
        String lSTemp = extras.getString("temp");
        double lTemp= 0f;
        if(lSTemp != null){
            lTemp = Double.parseDouble(lSTemp);
        }
        String lSHumi = extras.getString("humi");
        double lHumi = 0f;
        if(lSHumi != null){
            lHumi = Double.parseDouble(lSHumi);
        }
        String title = String.format("%.1f\u2103/%.1f%%", lTemp, lHumi);
        String message = extras.getString("message");
        if(message != null){
            // Post notification of received message.
            sendNotification(title, extras.getString("message"));
        }

        Log.i(TAG, "Received: " + extras.toString());
        setTextFromPreferences(lTemp, lHumi);
    }
    // [END receive_message]

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(String title, String msg) {
        NotificationManager mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_stat_hot)
                        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.bic_stat_hot))
                        .setContentTitle(title)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setContentText(msg);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(0, mBuilder.build());
    }


    private void setTextFromPreferences(double temp, double humi){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putFloat(getString(R.string.pref_dream_temp_key), (float)temp);
        editor.putFloat(getString(R.string.pref_dream_humi_key), (float)humi);
        editor.apply();
    }
}
