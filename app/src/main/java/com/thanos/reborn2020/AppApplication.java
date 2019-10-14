package com.thanos.reborn2020;

import android.app.Application;

public class AppApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseEventUtils.fetchFirebaseRemoteConfig(this);
    }
}
