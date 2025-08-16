package com.mmmut.wifiautologin.workers;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.mmmut.wifiautologin.data.PreferenceManager;
import com.mmmut.wifiautologin.network.LoginService;

public class WiFiLoginWorker extends Worker {

    private static final String TAG = "WiFiLoginWorker";

    public WiFiLoginWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "WiFi login work started");
        
        PreferenceManager preferenceManager = null;
        
        try {
            Context context = getApplicationContext();
            if (context == null) {
                Log.e(TAG, "Application context is null");
                return Result.failure();
            }
            
            preferenceManager = new PreferenceManager(context);
            
            // Get saved credentials
            String rollNumber = preferenceManager.getRollNumber();
            String password = preferenceManager.getPassword();
            
            if (rollNumber == null || rollNumber.isEmpty() || 
                password == null || password.isEmpty()) {
                Log.e(TAG, "No credentials available");
                if (preferenceManager != null) {
                    preferenceManager.setLastLoginResult("Failed: No credentials");
                }
                return Result.failure();
            }
            
            // Perform login
            LoginService loginService = new LoginService();
            LoginService.LoginResult result = loginService.performLogin(rollNumber, password);
            
            // Update preferences with result
            if (preferenceManager != null) {
                preferenceManager.setLastLoginTime(System.currentTimeMillis());
                preferenceManager.setLastLoginResult(result.message);
            }
            
            Log.d(TAG, "Login result: " + result.message);
            
            return result.success ? Result.success() : Result.failure();
            
        } catch (Exception e) {
            Log.e(TAG, "Error during login", e);
            
            try {
                if (preferenceManager != null) {
                    preferenceManager.setLastLoginTime(System.currentTimeMillis());
                    preferenceManager.setLastLoginResult("Failed: " + e.getMessage());
                }
            } catch (Exception ex) {
                Log.e(TAG, "Error updating login result", ex);
            }
            
            return Result.failure();
        }
    }
}
