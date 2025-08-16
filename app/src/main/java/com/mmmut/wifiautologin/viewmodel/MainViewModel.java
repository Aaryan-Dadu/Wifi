package com.mmmut.wifiautologin.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.mmmut.wifiautologin.data.PreferenceManager;

public class MainViewModel extends AndroidViewModel {

    private final PreferenceManager preferenceManager;
    private final MutableLiveData<String> rollNumber = new MutableLiveData<>();
    private final MutableLiveData<String> password = new MutableLiveData<>();
    private final MutableLiveData<Boolean> autoLoginEnabled = new MutableLiveData<>();
    private final MutableLiveData<String> lastLoginTime = new MutableLiveData<>();
    private final MutableLiveData<String> lastLoginResult = new MutableLiveData<>();
    private final MutableLiveData<String> toastMessage = new MutableLiveData<>();

    public MainViewModel(@NonNull Application application) {
        super(application);
        PreferenceManager tempPrefManager = null;
        try {
            tempPrefManager = new PreferenceManager(application);
            android.util.Log.d("MainViewModel", "MainViewModel initialized successfully");
        } catch (Exception e) {
            android.util.Log.e("MainViewModel", "Error initializing MainViewModel, using fallback", e);
            // Create a fallback without encryption
            tempPrefManager = new PreferenceManager(application);
        }
        preferenceManager = tempPrefManager;
    }

    public void loadCredentials() {
        rollNumber.setValue(preferenceManager.getRollNumber());
        password.setValue(preferenceManager.getPassword());
        autoLoginEnabled.setValue(preferenceManager.isAutoLoginEnabled());
        lastLoginTime.setValue(preferenceManager.getLastLoginTime());
        lastLoginResult.setValue(preferenceManager.getLastLoginResult());
    }

    public void saveCredentials(String rollNumber, String password) {
        preferenceManager.saveCredentials(rollNumber, password);
        this.rollNumber.setValue(rollNumber);
        this.password.setValue(password);
        toastMessage.setValue("Credentials saved successfully");
    }

    public void setAutoLoginEnabled(boolean enabled) {
        preferenceManager.setAutoLoginEnabled(enabled);
        autoLoginEnabled.setValue(enabled);
        toastMessage.setValue(enabled ? "Auto-login enabled" : "Auto-login disabled");
    }

    public void refreshStatus() {
        lastLoginTime.setValue(preferenceManager.getLastLoginTime());
        lastLoginResult.setValue(preferenceManager.getLastLoginResult());
    }

    // Getters
    public LiveData<String> getRollNumber() { return rollNumber; }
    public LiveData<String> getPassword() { return password; }
    public LiveData<Boolean> getAutoLoginEnabled() { return autoLoginEnabled; }
    public LiveData<String> getLastLoginTime() { return lastLoginTime; }
    public LiveData<String> getLastLoginResult() { return lastLoginResult; }
    public LiveData<String> getToastMessage() { return toastMessage; }
}
