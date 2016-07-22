package com.yfaney.asifathome;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import org.json.JSONException;

import java.io.IOException;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferencesFactory;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class RegistrationIntentService extends IntentService {
    private static final String TAG = "RegIntentService";
    private static final String[] TOPICS = {"global"};

    public RegistrationIntentService(){super(TAG);}

    @Override
    protected void onHandleIntent(Intent intent){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        synchronized (TAG){
            try {
                InstanceID instanceID = InstanceID.getInstance(this);
                String token = null;
                token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                        GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
                Log.i(TAG, "GCM Registration Token: " + token);
                boolean isTokenSentToServer = false;
                isTokenSentToServer = sharedPreferences.getBoolean(QuickstartPreferences.GCM_TOKEN_SENT, false);
                if(isTokenSentToServer){
                    broadcastGcmRegistrationFinished();
                }else{
                    sendRegistrationToServer(token);
                    subscribeTopics(token);
                }
            } catch (IOException | ClassCastException e) {
                e.printStackTrace();
                if(e instanceof  ClassCastException){
                    Log.d(TAG, "Cast Exception. Removing the preference");
                    sharedPreferences.edit().remove(QuickstartPreferences.GCM_TOKEN_SENT).apply();
                }
            }

        }
    }

    private void subscribeTopics(String token) throws IOException {
        for (String topic : TOPICS){
            GcmPubSub pubSub = GcmPubSub.getInstance(this);
            pubSub.subscribe(token, "/topics/" + topic, null);
        }
    }

    private void sendRegistrationToServer(String token) {
        final String f_token = token;
        new AsyncTask<String, Process, Boolean>(){

            @Override
            protected Boolean doInBackground(String... params) {
                Log.d(TAG, "Registering: " + params[0]);
                try {
                    return CloudRESTController.registerGcmToken(params[0]);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return false;
            }

            @Override
            public void onPostExecute(Boolean success){
                if(success){
                    runAfterRegistration(f_token);
                    broadcastGcmRegistrationFinished();
                }
            }
        }.execute(token);
    }

    private void broadcastGcmRegistrationFinished() {
        Intent registrationComplete = new Intent(QuickstartPreferences.REGISTRATION_COMPLETE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }

    private void runAfterRegistration(String token) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        sharedPreferences.edit().putBoolean(QuickstartPreferences.GCM_TOKEN_GENERATED, true).apply();
        sharedPreferences.edit().putString(QuickstartPreferences.GCM_TOKEN,  token).apply();
    }

}
