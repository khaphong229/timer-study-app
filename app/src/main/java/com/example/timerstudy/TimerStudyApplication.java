package com.example.timerstudy;

import android.app.Application;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

/**
 * Custom Application class for initializing Facebook SDK
 * This must be initialized before any Facebook features are used
 */
public class TimerStudyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize Facebook SDK
        // This MUST be called before using any Facebook features
        FacebookSdk.sdkInitialize(getApplicationContext());

        // Enable App Events logging (optional, for analytics)
        AppEventsLogger.activateApp(this);
    }
}
