package com.thetonrifles.activitydetector.adapter;

import com.thetonrifles.activitydetector.core.DetectionItem;

public class NewDetectionItem extends AbstractDetectionItem {

    public NewDetectionItem(DetectionItem detection) {
        super(detection);
    }

    @Override
    public ListItemType getType() {
        return ListItemType.NEW_ACTIVITY;
    }

}
