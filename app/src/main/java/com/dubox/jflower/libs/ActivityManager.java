package com.dubox.jflower.libs;

import android.app.Activity;

import java.lang.ref.WeakReference;

public class ActivityManager {

    private static ActivityManager activityManager = new ActivityManager();
    private static WeakReference<Activity> topActivity;

    private ActivityManager() {

    }

    public static ActivityManager getInstance(){
        return activityManager;
    }

    public static Activity getTopActivity() {
        if (topActivity!=null){
            return topActivity.get();
        }
        return null;
    }

    public static void setTopActivity(Activity activity) {
        topActivity = new WeakReference<>(activity);
    }

}