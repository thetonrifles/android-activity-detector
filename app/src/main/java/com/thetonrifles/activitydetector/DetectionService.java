package com.thetonrifles.activitydetector;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.thetonrifles.activitydetector.model.DetectionItem;

import java.util.Date;

/**
 * Intent Service used for receiving activity recognition
 * updates from Google Play Services.
 */
public class DetectionService extends IntentService {

    public DetectionService() {
        super("DetectionService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(AppParams.Tags.SERVICE, "handling activity recognition intent");
        if (ActivityRecognitionResult.hasResult(intent)) {
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            sendNotification(result);
        }
    }

    /**
     * Support method for delivering received activity recognition to UI.
     */
    private void sendNotification(ActivityRecognitionResult result) {
        Log.d(AppParams.Tags.SERVICE, "detected activity " + result.toString());
        // building intent to deliver to UI
        Intent intent = new Intent();
        intent.setAction(MainActivity.ActivityReceiver.NEW_ACTIVITY_ACTION);
        intent.putExtra(MainActivity.ActivityReceiver.NEW_ACTIVITY_PARAM, buildDetectionItem(result));
        // delivering data to UI
        sendBroadcast(intent);
    }

    /**
     * Support method for building activity recognition data item.
     */
    private DetectionItem buildDetectionItem(ActivityRecognitionResult result) {
        DetectionItem item = new DetectionItem();
        item.setTimestamp(new Date().getTime());
        for (DetectedActivity activity : result.getProbableActivities()) {
            item.addActivity(getActivityType(activity), activity.getConfidence());
        }
        return item;
    }

    /**
     * Support method for returing activity name/type from
     * its Google Play Services wrapper.
     */
    private String getActivityType(DetectedActivity activity) {
        String type = "unknown";
        switch (activity.getType()) {
            case DetectedActivity.IN_VEHICLE:
                type = "vehicle";
                break;
            case DetectedActivity.ON_BICYCLE:
                type = "on_bicycle";
                break;
            case DetectedActivity.ON_FOOT:
                type = "on_foot";
                break;
            case DetectedActivity.WALKING:
                type = "walking";
                break;
            case DetectedActivity.RUNNING:
                type = "running";
                break;
            case DetectedActivity.STILL:
                type = "still";
                break;
            case DetectedActivity.TILTING:
                type = "tilting";
                break;
        }
        return type;
    }

}
