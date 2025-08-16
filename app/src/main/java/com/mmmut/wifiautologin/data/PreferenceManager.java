package com.mmmut.wifiautologin.data;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PreferenceManager {

    private static final String PREF_NAME = "wifi_auto_login_prefs";
    private static final String KEY_ROLL_NUMBER = "roll_number";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_AUTO_LOGIN_ENABLED = "auto_login_enabled";
    private static final String KEY_LAST_LOGIN_TIME = "last_login_time";
    private static final String KEY_LAST_LOGIN_RESULT = "last_login_result";

    private final SharedPreferences encryptedPrefs;
    private final SharedPreferences regularPrefs;
    private final SimpleDateFormat dateFormat;
    private final boolean isEncryptionAvailable;

    public PreferenceManager(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("Context cannot be null");
        }
        
        dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        
        // Regular preferences for non-sensitive data
        regularPrefs = context.getSharedPreferences(PREF_NAME + "_regular", Context.MODE_PRIVATE);
        
        // Encrypted preferences for sensitive data
        SharedPreferences tempEncryptedPrefs = null;
        boolean tempEncryptionAvailable = false;
        
        try {
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            tempEncryptedPrefs = EncryptedSharedPreferences.create(
                PREF_NAME + "_encrypted",
                masterKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
            tempEncryptionAvailable = true;
            android.util.Log.d("PreferenceManager", "EncryptedSharedPreferences initialized successfully");
        } catch (GeneralSecurityException | IOException e) {
            // Fallback to regular preferences if encryption fails
            android.util.Log.w("PreferenceManager", "Failed to create encrypted preferences, using fallback", e);
            tempEncryptedPrefs = context.getSharedPreferences(PREF_NAME + "_fallback", Context.MODE_PRIVATE);
            tempEncryptionAvailable = false;
        } catch (Exception e) {
            // Ultimate fallback
            android.util.Log.e("PreferenceManager", "All preferences failed, using basic fallback", e);
            tempEncryptedPrefs = context.getSharedPreferences(PREF_NAME + "_basic", Context.MODE_PRIVATE);
            tempEncryptionAvailable = false;
        }
        
        encryptedPrefs = tempEncryptedPrefs;
        isEncryptionAvailable = tempEncryptionAvailable;
    }

    public void saveCredentials(String rollNumber, String password) {
        if (rollNumber == null) rollNumber = "";
        if (password == null) password = "";
        
        try {
            encryptedPrefs.edit()
                .putString(KEY_ROLL_NUMBER, rollNumber)
                .putString(KEY_PASSWORD, password)
                .apply();
        } catch (Exception e) {
            android.util.Log.e("PreferenceManager", "Error saving credentials", e);
        }
    }

    public String getRollNumber() {
        try {
            return encryptedPrefs.getString(KEY_ROLL_NUMBER, "");
        } catch (Exception e) {
            android.util.Log.e("PreferenceManager", "Error getting roll number", e);
            return "";
        }
    }

    public String getPassword() {
        try {
            return encryptedPrefs.getString(KEY_PASSWORD, "");
        } catch (Exception e) {
            android.util.Log.e("PreferenceManager", "Error getting password", e);
            return "";
        }
    }

    public void setAutoLoginEnabled(boolean enabled) {
        try {
            regularPrefs.edit()
                .putBoolean(KEY_AUTO_LOGIN_ENABLED, enabled)
                .apply();
        } catch (Exception e) {
            android.util.Log.e("PreferenceManager", "Error setting auto login enabled", e);
        }
    }

    public boolean isAutoLoginEnabled() {
        try {
            return regularPrefs.getBoolean(KEY_AUTO_LOGIN_ENABLED, false);
        } catch (Exception e) {
            android.util.Log.e("PreferenceManager", "Error getting auto login enabled", e);
            return false;
        }
    }

    public void setLastLoginTime(long timestamp) {
        try {
            String formattedTime = dateFormat.format(new Date(timestamp));
            regularPrefs.edit()
                .putString(KEY_LAST_LOGIN_TIME, formattedTime)
                .apply();
        } catch (Exception e) {
            android.util.Log.e("PreferenceManager", "Error setting last login time", e);
        }
    }

    public String getLastLoginTime() {
        try {
            return regularPrefs.getString(KEY_LAST_LOGIN_TIME, null);
        } catch (Exception e) {
            android.util.Log.e("PreferenceManager", "Error getting last login time", e);
            return null;
        }
    }

    public void setLastLoginResult(String result) {
        if (result == null) result = "";
        
        try {
            regularPrefs.edit()
                .putString(KEY_LAST_LOGIN_RESULT, result)
                .apply();
        } catch (Exception e) {
            android.util.Log.e("PreferenceManager", "Error setting last login result", e);
        }
    }

    public String getLastLoginResult() {
        try {
            return regularPrefs.getString(KEY_LAST_LOGIN_RESULT, null);
        } catch (Exception e) {
            android.util.Log.e("PreferenceManager", "Error getting last login result", e);
            return null;
        }
    }

    public boolean hasCredentials() {
        try {
            String rollNumber = getRollNumber();
            String password = getPassword();
            return rollNumber != null && !rollNumber.isEmpty() && 
                   password != null && !password.isEmpty();
        } catch (Exception e) {
            android.util.Log.e("PreferenceManager", "Error checking credentials", e);
            return false;
        }
    }

    public boolean isEncryptionAvailable() {
        return isEncryptionAvailable;
    }
}
