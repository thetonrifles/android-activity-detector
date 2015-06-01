package com.thetonrifles.activitydetector.core;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Wrapper class for detected activities in
 * a specific timestamp.
 */
public class DetectionItem implements Comparable<DetectionItem>, Serializable {

    private static final long serialVersionUID = 1L;

    private long start;
    private long duration;
    private Map<String,Integer> activities;

    /**
     * Builds an empty detection item.
     */
    public DetectionItem() {
        activities = new TreeMap<>();
    }

    /**
     * Builds detection item starting from a valid
     * ActivityRecognitionResult instance.
     */
    public DetectionItem(ActivityRecognitionResult result) {
        this();
        setStart(new Date().getTime());
        for (DetectedActivity activity : result.getProbableActivities()) {
            addActivity(getActivityType(activity), activity.getConfidence());
        }

    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public List<String> getActivities() {
        return new ArrayList<String>(activities.keySet());
    }

    public Map<String,Integer> getActivitiesWithConfidence() {
        return activities;
    }

    public void addActivity(String type, int confidence) {
        activities.put(type, confidence);
    }

    public int getConfidence(String type) {
        if (activities.containsKey(type)) {
            return activities.get(type);
        }
        return 0;
    }

    public String getMostProbableActivity() {
        String activity = "unknown";
        int max = 0;
        for (Map.Entry entry : activities.entrySet()) {
            int temp = (Integer) entry.getValue();
            if (temp > max) {
                max = temp;
                activity = (String) entry.getKey();
            }
        }
        return activity;
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

    @Override
    public boolean equals(Object o) {
        // equality is based on activities-confidences only
        boolean equals = true;
        // getting all activities
        String[] current = activities.keySet().toArray(new String[]{});
        String[] other = ((DetectionItem) o).activities.keySet().toArray(new String[]{});
        // same number of activities in both objects?
        if (current.length == other.length) {
            // comparing with iteration is enough because we are
            // using a TreeSet (keys are sorted)
            for (int i = 0; i < current.length; i++) {
                // different keys or different confidence values?
                if (!current[i].equals(other[i]) ||
                        activities.get(current[i]) != activities.get(other[i])) {
                    equals = false;
                }
            }
        } else {
            equals = false;
        }
        return equals;
    }

    @Override
    public int compareTo(DetectionItem other) {
        // sorting is start time based
        Long currentStart = start;
        Long otherStart = other.start;
        return currentStart.compareTo(otherStart);
    }

}
