package com.thetonrifles.activitydetector.adapter;

import com.thetonrifles.activitydetector.core.DetectionItem;

public class EndDetectionItem extends AbstractDetectionItem {

    public EndDetectionItem(DetectionItem detection) {
        super(detection);
    }

    @Override
    public ListItemType getType() {
        return ListItemType.END_ACTIVITY;
    }

}
