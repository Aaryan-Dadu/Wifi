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
            
            // Set up global exception handler
            setupGlobalExceptionHandler();
            
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
    
    private void setupGlobalExceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable throwable) {
                Log.e(TAG, "Uncaught exception in thread " + thread.getName(), throwable);
                
                // Log additional information
                Log.e(TAG, "Exception type: " + throwable.getClass().getSimpleName());
                Log.e(TAG, "Exception message: " + throwable.getMessage());
                
                // Don't kill the app for certain exceptions
                if (throwable instanceof OutOfMemoryError) {
                    Log.e(TAG, "Out of memory error - attempting to recover");
                    System.gc();
                    return;
                }
                
                // For other exceptions, let the default handler deal with it
                if (Thread.getDefaultUncaughtExceptionHandler() != this) {
                    Thread.getDefaultUncaughtExceptionHandler().uncaughtException(thread, throwable);
                }
            }
        });
    }
}
