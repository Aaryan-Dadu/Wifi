package com.mmmut.wifiautologin.network;

import android.util.Log;
import okhttp3.*;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class LoginService {

    private static final String TAG = "LoginService";
    private static final String LOGIN_URL = "http://172.16.1.3:8090/httpclient.html";
    private static final int TIMEOUT_SECONDS = 30;

    private final OkHttpClient client;

    public LoginService() {
        client = new OkHttpClient.Builder()
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .build();
    }

    public LoginResult performLogin(String rollNumber, String password) {
        if (rollNumber == null || password == null) {
            Log.e(TAG, "Invalid credentials provided");
            return new LoginResult(false, "Invalid credentials");
        }
        
        Log.d(TAG, "Attempting login for roll number: " + rollNumber);
        
        Response getResponse = null;
        Response postResponse = null;
        
        try {
            // First, try to get the login page to understand the form structure
            Request getRequest = new Request.Builder()
                .url(LOGIN_URL)
                .get()
                .build();

            getResponse = client.newCall(getRequest).execute();
            
            if (!getResponse.isSuccessful()) {
                Log.e(TAG, "Failed to access login page. Response code: " + getResponse.code());
                return new LoginResult(false, "Failed to access login portal");
            }

            String loginPageContent = getResponse.body() != null ? getResponse.body().string() : "";
            Log.d(TAG, "Successfully accessed login page");

            // Build the POST request with form data
            // Common parameter names for campus login portals
            RequestBody formBody = new FormBody.Builder()
                .add("username", rollNumber)
                .add("password", password)
                .add("mode", "191")
                .add("a", System.currentTimeMillis() + "")
                .add("producttype", "0")
                .build();

            Request postRequest = new Request.Builder()
                .url(LOGIN_URL)
                .post(formBody)
                .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 10; SM-G973F) AppleWebKit/537.36")
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("Referer", LOGIN_URL)
                .build();

            postResponse = client.newCall(postRequest).execute();
            
            if (postResponse.isSuccessful()) {
                String responseBody = postResponse.body() != null ? postResponse.body().string() : "";
                
                Log.d(TAG, "Login request completed with status: " + postResponse.code());
                
                // Check response content for success indicators
                if (responseBody.toLowerCase().contains("success") || 
                    responseBody.toLowerCase().contains("logged in") ||
                    responseBody.toLowerCase().contains("authentication successful")) {
                    
                    Log.i(TAG, "Login successful");
                    return new LoginResult(true, "Login successful");
                    
                } else if (responseBody.toLowerCase().contains("invalid") ||
                          responseBody.toLowerCase().contains("incorrect") ||
                          responseBody.toLowerCase().contains("failed")) {
                    
                    Log.w(TAG, "Login failed - invalid credentials");
                    return new LoginResult(false, "Invalid credentials");
                    
                } else {
                    // If we can't determine from response content, assume success for 2xx status
                    Log.i(TAG, "Login likely successful (status: " + postResponse.code() + ")");
                    return new LoginResult(true, "Login completed");
                }
            } else {
                Log.e(TAG, "Login request failed with status: " + postResponse.code());
                return new LoginResult(false, "Login failed (HTTP " + postResponse.code() + ")");
            }

        } catch (IOException e) {
            Log.e(TAG, "Network error during login", e);
            return new LoginResult(false, "Network error: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error during login", e);
            return new LoginResult(false, "Error: " + e.getMessage());
        } finally {
            // Clean up resources
            try {
                if (getResponse != null && getResponse.body() != null) {
                    getResponse.body().close();
                }
                if (postResponse != null && postResponse.body() != null) {
                    postResponse.body().close();
                }
            } catch (Exception e) {
                Log.w(TAG, "Error closing response bodies", e);
            }
        }
    }

    public static class LoginResult {
        public final boolean success;
        public final String message;

        public LoginResult(boolean success, String message) {
            this.success = success;
            this.message = message != null ? message : "";
        }
    }
}
