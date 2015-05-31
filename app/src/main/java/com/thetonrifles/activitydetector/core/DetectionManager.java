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

/**
 * Singleton class for encapsulating activity
 * recognition API logic
 */
public class DetectionManager implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    public static final String NEW_DETECTION = "com.thetonrifles.activitydetector.NEW_DETECTION";

    public static final String SAME_DETECTION = "com.thetonrifles.activitydetector.SAME_DETECTION";

    public static final String DETECTED_ACTIVITY = "com.thetonrifles.activitydetector.DETECTED_ACTIVITY";

    private static final long UPDATE_PERIOD = 10000l;  // 10 seconds

    // singleton implementation

    private static DetectionManager instance;

    public static synchronized DetectionManager getInstance() {
        if (instance == null) {
            instance = new DetectionManager();
        }
        return instance;
    }

    /**
     * Last detected value.
     */
    private static DetectionItem _LAST_DETECTION;

    /**
     * Lock for accessing last detection object.
     */
    private static Object _LAST_DETECTION_LOCK = new Object();

    /**
     * Update period for activity recognition.
     */
    private long mUpdatePeriod;

    /**
     * Play Services API Client to be used for handling
     * activity recognition.
     */
    private GoogleApiClient mActivityRecognitionClient;

    /**
     * Callback to be used for handling new detection
     * coming from Play Services.
     */
    private PendingIntent mCallbackIntent;

    private DetectionManager() {
        mUpdatePeriod = UPDATE_PERIOD;
    }

    /**
     * Start manager with default update period.
     */
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

    /**
     * Define update period. This method must be invoked
     * before start() for having effect.
     */
    public void setUpdatePeriod(long updatePeriod) {
        mUpdatePeriod = updatePeriod;
    }

    /**
     * Stop manager deallocating resources.
     */
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
                Log.d(LogTags.SERVICE, "detected activity " + result.toString());
                synchronized (_LAST_DETECTION_LOCK) {
                    // building detection
                    DetectionItem detection = new DetectionItem(result);
                    // notification to be delivered?
                    if (_LAST_DETECTION == null) {
                        Log.d(LogTags.SERVICE, "first detection... notify new event!");
                        sendEventNotification(detection, true);
                    } else {
                        boolean isNew = !_LAST_DETECTION.equals(detection);
                        sendEventNotification(detection, isNew);
                    }
                    // update last detection
                    _LAST_DETECTION = detection;
                }
            } else {
                Log.d(LogTags.SERVICE, "null activity recognition intent");
            }
        }

        /**
         * Support method for broadcasting received activity.
         */
        private void sendEventNotification(DetectionItem detection, boolean isNew) {
            if (isNew) {
                Log.d(LogTags.SERVICE, "new detection... notify new event!");
            } else {
                Log.d(LogTags.SERVICE, "same detection as before... notify same event!");
            }
            Log.d(LogTags.SERVICE, "notifying detection...");
            // building intent to deliver to UI
            Intent intent = new Intent();
            if (isNew) {
                intent.setAction(NEW_DETECTION);
            } else {
                intent.setAction(SAME_DETECTION);
            }
            intent.putExtra(DETECTED_ACTIVITY, detection);
            // delivering data to UI
            sendBroadcast(intent);
        }

    }

}
