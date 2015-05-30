package com.thetonrifles.activitydetector;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;

/**
 * Singleton class for encapsulating activity
 * recognition API logic
 */
public class DetectionManager implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String LOG_TAG = "DetectionManager";

    private static final long UPDATE_PERIOD = 10000l;  // 10 seconds

    private static DetectionManager instance;

    public static synchronized DetectionManager getInstance() {
        if (instance == null) {
            instance = new DetectionManager();
        }
        return instance;
    }

    private DetectionManager() {
    }

    private GoogleApiClient mActivityRecognitionClient;

    private PendingIntent mCallbackIntent;

    public void start(Context context) {
        Log.d(LOG_TAG, "activity recognition manager start");
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
        Log.d(LOG_TAG, "activity recognition manager stop");
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
        Log.e(LOG_TAG, "connection failure");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(LOG_TAG, "connection suspended");
    }

}
