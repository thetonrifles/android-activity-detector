package com.thetonrifles.activitydetector;

import android.app.Application;
import android.test.ApplicationTestCase;

import com.thetonrifles.activitydetector.core.DetectionItem;

import junit.framework.Assert;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {

    public ApplicationTest() {
        super(Application.class);
    }

    public void testEqualsDetectionItems() throws Exception {
        String activity = DetectionItem.TYPE_STILL;
        // same confidence values
        Assert.assertEquals(buildDetectionItem(activity, 100), buildDetectionItem(activity, 100));
        // near confidence values
        Assert.assertEquals(buildDetectionItem(activity, 100), buildDetectionItem(activity, 90));
        // limit confidence values
        Assert.assertEquals(buildDetectionItem(activity, 100), buildDetectionItem(activity, 75));
        // too different confidence values
        Assert.assertNotSame(buildDetectionItem(activity, 100), buildDetectionItem(activity, 74));
    }

    private DetectionItem buildDetectionItem(String activity, int confidence) {
        DetectionItem item = new DetectionItem();
        item.addActivity(activity, confidence);
        return item;
    }

}