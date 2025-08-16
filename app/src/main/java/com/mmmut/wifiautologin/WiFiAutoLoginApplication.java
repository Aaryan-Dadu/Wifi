package com.mmmut.wifiautologin;

import android.app.Application;
import android.util.Log;
import androidx.work.Configuration;
import androidx.work.WorkManager;

public class WiFiAutoLoginApplication extends Application {
    
    private static final String TAG = "WiFiAutoLoginApp";

    @Override
    public void onCreate() {
        super.onCreate();
        
        try {
            Log.d(TAG, "Application onCreate() called");
            
            // Initialize WorkManager with custom configuration
            Configuration workManagerConfig = new Configuration.Builder()
                    .setMinimumLoggingLevel(android.util.Log.INFO)
                    .build();
            
            WorkManager.initialize(this, workManagerConfig);
            
            Log.d(TAG, "Application initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing application", e);
        }
    }
}
