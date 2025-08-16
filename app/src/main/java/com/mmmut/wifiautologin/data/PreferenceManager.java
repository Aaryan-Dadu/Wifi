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

    public PreferenceManager(Context context) {
        dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        
        // Regular preferences for non-sensitive data
        regularPrefs = context.getSharedPreferences(PREF_NAME + "_regular", Context.MODE_PRIVATE);
        
        // Encrypted preferences for sensitive data
        SharedPreferences tempEncryptedPrefs = null;
        try {
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            tempEncryptedPrefs = EncryptedSharedPreferences.create(
                PREF_NAME + "_encrypted",
                masterKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
            android.util.Log.d("PreferenceManager", "EncryptedSharedPreferences initialized successfully");
        } catch (GeneralSecurityException | IOException e) {
            // Fallback to regular preferences if encryption fails
            android.util.Log.w("PreferenceManager", "Failed to create encrypted preferences, using fallback", e);
            tempEncryptedPrefs = context.getSharedPreferences(PREF_NAME + "_fallback", Context.MODE_PRIVATE);
        } catch (Exception e) {
            // Ultimate fallback
            android.util.Log.e("PreferenceManager", "All preferences failed, using basic fallback", e);
            tempEncryptedPrefs = context.getSharedPreferences(PREF_NAME + "_basic", Context.MODE_PRIVATE);
        }
        encryptedPrefs = tempEncryptedPrefs;
    }

    public void saveCredentials(String rollNumber, String password) {
        encryptedPrefs.edit()
            .putString(KEY_ROLL_NUMBER, rollNumber)
            .putString(KEY_PASSWORD, password)
            .apply();
    }

    public String getRollNumber() {
        return encryptedPrefs.getString(KEY_ROLL_NUMBER, "");
    }

    public String getPassword() {
        return encryptedPrefs.getString(KEY_PASSWORD, "");
    }

    public void setAutoLoginEnabled(boolean enabled) {
        regularPrefs.edit()
            .putBoolean(KEY_AUTO_LOGIN_ENABLED, enabled)
            .apply();
    }

    public boolean isAutoLoginEnabled() {
        return regularPrefs.getBoolean(KEY_AUTO_LOGIN_ENABLED, false);
    }

    public void setLastLoginTime(long timestamp) {
        String formattedTime = dateFormat.format(new Date(timestamp));
        regularPrefs.edit()
            .putString(KEY_LAST_LOGIN_TIME, formattedTime)
            .apply();
    }

    public String getLastLoginTime() {
        return regularPrefs.getString(KEY_LAST_LOGIN_TIME, null);
    }

    public void setLastLoginResult(String result) {
        regularPrefs.edit()
            .putString(KEY_LAST_LOGIN_RESULT, result)
            .apply();
    }

    public String getLastLoginResult() {
        return regularPrefs.getString(KEY_LAST_LOGIN_RESULT, null);
    }

    public boolean hasCredentials() {
        return !getRollNumber().isEmpty() && !getPassword().isEmpty();
    }
}
