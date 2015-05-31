package com.thetonrifles.activitydetector.adapter;

import com.thetonrifles.activitydetector.core.DetectionItem;

public class SameDetectionItem extends AbstractDetectionItem {

    public SameDetectionItem(DetectionItem detection) {
        super(detection);
    }

    @Override
    public ListItemType getType() {
        return ListItemType.SAME_ACTIVITY;
    }

}
