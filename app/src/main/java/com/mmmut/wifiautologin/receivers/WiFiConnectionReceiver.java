package com.mmmut.wifiautologin.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import com.mmmut.wifiautologin.data.PreferenceManager;
import com.mmmut.wifiautologin.workers.WiFiLoginWorker;

public class WiFiConnectionReceiver extends BroadcastReceiver {

    private static final String TAG = "WiFiConnectionReceiver";
    private static final String MMMUT_SSID_PREFIX = "MMMUT";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null || intent == null) {
            Log.w(TAG, "Received null context or intent");
            return;
        }

        String action = intent.getAction();
        if (action == null) {
            Log.w(TAG, "Received intent with null action");
            return;
        }
        
        Log.d(TAG, "Received action: " + action);

        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action) ||
            WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
            
            handleNetworkChange(context);
        }
    }

    private void handleNetworkChange(Context context) {
        if (context == null) {
            Log.e(TAG, "Context is null in handleNetworkChange");
            return;
        }
        
        try {
            PreferenceManager preferenceManager = new PreferenceManager(context);
            
            // Check if auto-login is enabled
            if (!preferenceManager.isAutoLoginEnabled()) {
                Log.d(TAG, "Auto-login is disabled");
                return;
            }

            // Check if credentials are saved
            if (!preferenceManager.hasCredentials()) {
                Log.d(TAG, "No credentials saved");
                return;
            }

            // Check if connected to WiFi
            ConnectivityManager connectivityManager = 
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            
            if (connectivityManager == null) {
                Log.e(TAG, "ConnectivityManager is null");
                return;
            }

            NetworkInfo wifiInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            
            if (wifiInfo != null && wifiInfo.isConnected()) {
                // Get current WiFi SSID
                WifiManager wifiManager = (WifiManager) context.getApplicationContext()
                    .getSystemService(Context.WIFI_SERVICE);
                
                if (wifiManager != null) {
                    WifiInfo currentWifi = wifiManager.getConnectionInfo();
                    if (currentWifi != null) {
                        String ssid = currentWifi.getSSID();
                        
                        // Remove quotes from SSID if present
                        if (ssid != null) {
                            ssid = ssid.replace("\"", "");
                            Log.d(TAG, "Current WiFi SSID: " + ssid);
                            
                            // Check if SSID starts with "MMMUT"
                            if (ssid.startsWith(MMMUT_SSID_PREFIX)) {
                                Log.d(TAG, "Connected to MMMUT network, starting login worker");
                                
                                // Start the login worker
                                try {
                                    OneTimeWorkRequest loginWork = new OneTimeWorkRequest.Builder(WiFiLoginWorker.class)
                                        .addTag("wifi_login")
                                        .build();
                                    
                                    WorkManager workManager = WorkManager.getInstance(context);
                                    if (workManager != null) {
                                        workManager.enqueue(loginWork);
                                    } else {
                                        Log.e(TAG, "WorkManager is null");
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "Error starting login worker", e);
                                }
                            } else {
                                Log.d(TAG, "Not a MMMUT network: " + ssid);
                            }
                        } else {
                            Log.d(TAG, "SSID is null");
                        }
                    } else {
                        Log.d(TAG, "WifiInfo is null");
                    }
                } else {
                    Log.e(TAG, "WifiManager is null");
                }
            } else {
                Log.d(TAG, "Not connected to WiFi");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in handleNetworkChange", e);
        }
    }
}
