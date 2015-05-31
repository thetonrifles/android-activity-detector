package com.thetonrifles.activitydetector.adapter;

import com.thetonrifles.activitydetector.core.DetectionItem;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public abstract class AbstractDetectionItem {

    private DetectionItem mDetection;

    protected AbstractDetectionItem(DetectionItem detection) {
        mDetection = detection;
    }

    public String getActivity() {
        return mDetection.getMostProbableActivity();
    }

    public String getTimestamp() {
        return formatTimestamp(mDetection.getTimestamp());
    }

    protected String formatTimestamp(long timestamp) {
        SimpleDateFormat df = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return df.format(new Date(timestamp));
    }

    public abstract ListItemType getType();

}
