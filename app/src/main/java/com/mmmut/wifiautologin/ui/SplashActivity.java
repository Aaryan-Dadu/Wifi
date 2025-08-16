package com.mmmut.wifiautologin.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import com.mmmut.wifiautologin.R;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";
    private static final int SPLASH_DELAY = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            Log.d(TAG, "SplashActivity onCreate() called");
            
            setContentView(R.layout.activity_splash);
            
            Log.d(TAG, "Splash layout set successfully");

            // Navigate to MainActivity after delay
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                try {
                    Log.d(TAG, "Starting MainActivity");
                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } catch (Exception e) {
                    Log.e(TAG, "Error starting MainActivity", e);
                }
            }, SPLASH_DELAY);
            
        } catch (Exception e) {
            Log.e(TAG, "Error in SplashActivity onCreate", e);
            // Try to start MainActivity directly if splash fails
            try {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
            } catch (Exception ex) {
                Log.e(TAG, "Critical error - cannot start app", ex);
            }
        }
    }
}
