package com.dubox.jflower;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.dubox.jflower.libs.ActivityManager;
import com.pgyer.pgyersdk.PgyerSDKManager;
import com.pgyer.pgyersdk.pgyerenum.Features;


public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                ActivityManager.setTopActivity(activity);
            }

            @Override
            public void onActivityStarted(Activity activity) {

            }

            @Override
            public void onActivityResumed(Activity activity) {
                ActivityManager.setTopActivity(activity);
            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {
//                ActivityManager.setTopActivity(null);
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });

        initPgyerSDK(this);
    }


    /**
     *  初始化蒲公英SDK
     * @param application
     */
    private static void initPgyerSDK( MyApplication application){
        new PgyerSDKManager.Init()
                .setContext(application) //设置上下问对象
                .enable(Features.CHECK_UPDATE)//开启自动更新检测（不设置默认功能关闭 ，AndroidManifest中也可以设置该属性的开关）
                .start();
    }
}