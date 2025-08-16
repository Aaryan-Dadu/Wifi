package com.mmmut.wifiautologin.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.mmmut.wifiautologin.data.PreferenceManager;

public class MainViewModel extends AndroidViewModel {

    private final PreferenceManager preferenceManager;
    private final MutableLiveData<String> rollNumber = new MutableLiveData<>("");
    private final MutableLiveData<String> password = new MutableLiveData<>("");
    private final MutableLiveData<Boolean> autoLoginEnabled = new MutableLiveData<>(false);
    private final MutableLiveData<String> lastLoginTime = new MutableLiveData<>(null);
    private final MutableLiveData<String> lastLoginResult = new MutableLiveData<>(null);
    private final MutableLiveData<String> toastMessage = new MutableLiveData<>(null);

    public MainViewModel(@NonNull Application application) {
        super(application);
        PreferenceManager tempPrefManager = null;
        try {
            tempPrefManager = new PreferenceManager(application);
            android.util.Log.d("MainViewModel", "MainViewModel initialized successfully");
        } catch (Exception e) {
            android.util.Log.e("MainViewModel", "Error initializing MainViewModel, using fallback", e);
            // Create a fallback without encryption
            try {
                tempPrefManager = new PreferenceManager(application);
            } catch (Exception ex) {
                android.util.Log.e("MainViewModel", "Critical error - cannot create PreferenceManager", ex);
                // Create a minimal fallback
                tempPrefManager = null;
            }
        }
        preferenceManager = tempPrefManager;
    }

    public void loadCredentials() {
        try {
            if (preferenceManager != null) {
                String roll = preferenceManager.getRollNumber();
                String pass = preferenceManager.getPassword();
                Boolean enabled = preferenceManager.isAutoLoginEnabled();
                String time = preferenceManager.getLastLoginTime();
                String result = preferenceManager.getLastLoginResult();
                
                rollNumber.setValue(roll != null ? roll : "");
                password.setValue(pass != null ? pass : "");
                autoLoginEnabled.setValue(enabled != null ? enabled : false);
                lastLoginTime.setValue(time);
                lastLoginResult.setValue(result);
            } else {
                // Set default values if PreferenceManager is null
                rollNumber.setValue("");
                password.setValue("");
                autoLoginEnabled.setValue(false);
                lastLoginTime.setValue(null);
                lastLoginResult.setValue(null);
            }
        } catch (Exception e) {
            android.util.Log.e("MainViewModel", "Error loading credentials", e);
            // Set safe default values
            rollNumber.setValue("");
            password.setValue("");
            autoLoginEnabled.setValue(false);
            lastLoginTime.setValue(null);
            lastLoginResult.setValue(null);
        }
    }

    public void saveCredentials(String rollNumber, String password) {
        try {
            if (preferenceManager != null) {
                if (rollNumber == null) rollNumber = "";
                if (password == null) password = "";
                
                preferenceManager.saveCredentials(rollNumber, password);
                this.rollNumber.setValue(rollNumber);
                this.password.setValue(password);
                toastMessage.setValue("Credentials saved successfully");
            } else {
                toastMessage.setValue("Error: App not properly initialized");
            }
        } catch (Exception e) {
            android.util.Log.e("MainViewModel", "Error saving credentials", e);
            toastMessage.setValue("Error saving credentials");
        }
    }

    public void setAutoLoginEnabled(boolean enabled) {
        try {
            if (preferenceManager != null) {
                preferenceManager.setAutoLoginEnabled(enabled);
                autoLoginEnabled.setValue(enabled);
                toastMessage.setValue(enabled ? "Auto-login enabled" : "Auto-login disabled");
            } else {
                toastMessage.setValue("Error: App not properly initialized");
            }
        } catch (Exception e) {
            android.util.Log.e("MainViewModel", "Error setting auto login enabled", e);
            toastMessage.setValue("Error updating settings");
        }
    }

    public void refreshStatus() {
        try {
            if (preferenceManager != null) {
                String time = preferenceManager.getLastLoginTime();
                String result = preferenceManager.getLastLoginResult();
                lastLoginTime.setValue(time);
                lastLoginResult.setValue(result);
            }
        } catch (Exception e) {
            android.util.Log.e("MainViewModel", "Error refreshing status", e);
        }
    }

    // Getters
    public LiveData<String> getRollNumber() { return rollNumber; }
    public LiveData<String> getPassword() { return password; }
    public LiveData<Boolean> getAutoLoginEnabled() { return autoLoginEnabled; }
    public LiveData<String> getLastLoginTime() { return lastLoginTime; }
    public LiveData<String> getLastLoginResult() { return lastLoginResult; }
    public LiveData<String> getToastMessage() { return toastMessage; }
}
