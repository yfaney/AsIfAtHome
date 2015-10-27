package com.yfaney.asifathome;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
import com.google.android.gms.iid.InstanceIDListenerService;

public class MyGcmListenerService extends GcmListenerService {

    public static final int NOTIFICATION_ID = 1;
    private static final String TAG = "MyGcmListenerService";

    public MyGcmListenerService() {
    }

    @Override
    public void onCreate(){

    }

    @Override
    public void onMessageReceived(String from, Bundle data){
        String message = data.getString(QuickstartPreferences.KEY_MESSAGE);
        Log.d(TAG, "from:" + from);
        Log.i(TAG, "Received: " + data.toString());
        Log.d(TAG, "message: " + message);
        double lTemp = Double.parseDouble(data.getString("temp"));
        double lHumi = Double.parseDouble(data.getString("humi"));
        String title = String.format("%.1f\u2103/%.1f%%", lTemp, lHumi);
        String status = data.getString("status");
        Log.d(TAG, "title: " + title);
        if(message != null){
            // Post notification of received message.
            sendNotification(title, data.getString("message"), status);
        }

        setTextFromPreferences(lTemp, lHumi);
    }
    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(String title, String msg, String status) {
        NotificationManager notificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_stat_hot)
//                        .setLargeIcon()
                        .setContentTitle(title)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setPriority(NotificationCompat.PRIORITY_MIN)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setContentText(msg);

        mBuilder.setContentIntent(contentIntent);
        notificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }


    private void setTextFromPreferences(double temp, double humi){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putFloat(getString(R.string.pref_dream_temp_key), (float)temp);
        editor.putFloat(getString(R.string.pref_dream_humi_key), (float) humi);
        editor.apply();
    }

}
