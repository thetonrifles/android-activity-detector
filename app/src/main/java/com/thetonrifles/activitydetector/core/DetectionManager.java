package com.thetonrifles.activitydetector.core;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.thetonrifles.activitydetector.LogTags;
import com.thetonrifles.activitydetector.MainActivity;

/**
 * Singleton class for encapsulating activity
 * recognition API logic
 */
public class DetectionManager implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final long UPDATE_PERIOD = 10000l;  // 10 seconds

    // singleton implementation

    private static DetectionManager instance;

    public static synchronized DetectionManager getInstance() {
        if (instance == null) {
            instance = new DetectionManager();
        }
        return instance;
    }

    private long mUpdatePeriod;

    private DetectionManager() {
        mUpdatePeriod = UPDATE_PERIOD;
    }

    private GoogleApiClient mActivityRecognitionClient;

    private PendingIntent mCallbackIntent;

    public void start(Context context, long updatePeriod) {
        mUpdatePeriod = updatePeriod;
        start(context);
    }

    public void start(Context context) {
        Log.d(LogTags.SERVICE, "detection started with update period: " + mUpdatePeriod + " millis");
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
        if (status == ConnectionResult.SUCCESS) {
            Intent intent = new Intent(context, DetectionService.class);
            mCallbackIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            mActivityRecognitionClient = new GoogleApiClient.Builder(context)
                    .addApi(ActivityRecognition.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
            mActivityRecognitionClient.connect();
        }
    }

    public void halt() {
        Log.d(LogTags.SERVICE, "activity recognition manager stop");
        if (mActivityRecognitionClient != null && mActivityRecognitionClient.isConnected()) {
            ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(
                    mActivityRecognitionClient, mCallbackIntent);
            mActivityRecognitionClient.disconnect();
            mActivityRecognitionClient = null;
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(
                mActivityRecognitionClient, UPDATE_PERIOD, mCallbackIntent);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(LogTags.SERVICE, "connection failure");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(LogTags.SERVICE, "connection suspended");
    }

    /**
     * Intent Service used for receiving activity recognition
     * updates from Google Play Services.
     */
    public static class DetectionService extends IntentService {

        public DetectionService() {
            super("DetectionService");
        }

        @Override
        protected void onHandleIntent(Intent intent) {
            Log.d(LogTags.SERVICE, "handling activity recognition intent");
            if (ActivityRecognitionResult.hasResult(intent)) {
                ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
                sendNotification(result);
            }
        }

        /**
         * Support method for delivering received activity recognition to UI.
         */
        private void sendNotification(ActivityRecognitionResult result) {
            Log.d(LogTags.SERVICE, "detected activity " + result.toString());
            // building intent to deliver to UI
            Intent intent = new Intent();
            intent.setAction(MainActivity.ActivityReceiver.NEW_ACTIVITY_ACTION);
            intent.putExtra(MainActivity.ActivityReceiver.NEW_ACTIVITY_PARAM, new DetectionItem(result));
            // delivering data to UI
            sendBroadcast(intent);
        }

    }

}
