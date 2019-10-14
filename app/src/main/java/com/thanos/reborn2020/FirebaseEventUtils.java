package com.thanos.reborn2020;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.util.HashMap;
import java.util.Map;

public class FirebaseEventUtils {
    private static final String TAG = "FirebaseEventUtils";
    private static String sBannerText;
    private static String sButtonColor;
    private static long sDuration;

    public static void logFirebaseEvent(Context context, String eventName, Bundle bundle) {
        FirebaseAnalytics.getInstance(context.getApplicationContext()).logEvent(eventName, bundle);
    }

    public static void fetchFirebaseRemoteConfig(final Context context) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final FirebaseRemoteConfig firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
                FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                        .setDeveloperModeEnabled(BuildConfig.DEBUG)
                        .build();
                firebaseRemoteConfig.setConfigSettings(configSettings);

                Map<String, Object> defaultValues = new HashMap<>();
                defaultValues.put("so_bannertext", "Game center");
                defaultValues.put("so_buttoncolor", "red");
                defaultValues.put("so_duration", 0L);
                firebaseRemoteConfig.setDefaults(defaultValues);

                long cacheExpiration = 3600;

                if (firebaseRemoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled()) {
                    cacheExpiration = 0;
                }

                firebaseRemoteConfig.fetch(cacheExpiration)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Log.d(TAG, "onComplete: task.isSuccessful() = " + task.isSuccessful());
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "Fetch and activate succeeded");
                                    firebaseRemoteConfig.activateFetched();
                                    logFirebaseEvent(context, firebaseRemoteConfig);
                                } else {
                                    Log.d(TAG, "Fetch Failed");
                                }
                            }
                        });
            }

        }).start();
    }

    private static void logFirebaseEvent(final Context context, final FirebaseRemoteConfig firebaseRemoteConfig) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.d(TAG, FirebaseInstanceId.getInstance().getToken());
                sBannerText = firebaseRemoteConfig.getString("so_bannertext");
                Log.d(TAG, "onComplete: bannertext = " + sBannerText);
                sButtonColor = firebaseRemoteConfig.getString("so_buttoncolor");
                Log.d(TAG, "onComplete: buttoncolor = " + sButtonColor);
                sDuration = firebaseRemoteConfig.getLong("so_duration");
                Log.d(TAG, "onComplete: duration = " + sDuration);
                logFirebaseEvent(context, "so_main_page_open", null);
                Bundle bundle = new Bundle();
                bundle.putString("so_bannertext", sBannerText);
                bundle.putString("so_buttoncolor", sButtonColor);
                logFirebaseEvent(context, "so_gc_banner", bundle);
                logFirebaseEvent(context, "so_gc_active", null);
                logFirebaseEvent(context, "so_app_active", null);

                if (sBannerText.equals("Game center") && sButtonColor.equals("red")) {
                    //对照组
                    logFirebaseGcClick(context, 20, 5);
                } else if (sBannerText.equals("let’s have some fun") && sButtonColor.equals("red")) {
                    //A
                    logFirebaseGcClick(context, 35, 2);
                } else if (sBannerText.equals("Game center") && sButtonColor.equals("yellow")) {
                    //B
                    logFirebaseGcClick(context, 15, 6);

                } else if (sBannerText.equals("let’s have some fun") && sButtonColor.equals("yellow")) {
                    //C
                    logFirebaseGcClick(context, 30, 4);
                }
            }
        }).start();
    }

    private static void logFirebaseGcClick(Context context, int maxRate, int times) {
        int rate = (int) (System.currentTimeMillis() % 100);
        if (rate < maxRate) {
            for (int i = 0; i < times; i++) {
                logFirebaseEvent(context, "so_gc_click", null);
            }
        }
    }
}
