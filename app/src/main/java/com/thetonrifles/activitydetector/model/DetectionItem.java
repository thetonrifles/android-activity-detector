package com.thetonrifles.activitydetector.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Wrapper class for detected activities in
 * a specific timestamp.
 */
public class DetectionItem implements Comparable<DetectionItem>, Serializable {

    private static final long serialVersionUID = 1L;

    private long timestamp;
    private Map<String,Integer> activities;

    public DetectionItem() {
        activities = new HashMap<>();
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, Integer> getActivities() {
        return activities;
    }

    public void setActivities(Map<String, Integer> activities) {
        this.activities = activities;
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

    @Override
    public int compareTo(DetectionItem other) {
        Long currentTimestamp = timestamp;
        Long otherTimestamp = other.timestamp;
        return currentTimestamp.compareTo(otherTimestamp);
    }

}
