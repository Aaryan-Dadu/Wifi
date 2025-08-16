package com.mmmut.wifiautologin.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.work.Configuration;
import androidx.work.WorkManager;
import com.mmmut.wifiautologin.databinding.ActivityMainBinding;
import com.mmmut.wifiautologin.viewmodel.MainViewModel;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 1001;
    private ActivityMainBinding binding;
    private MainViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            Log.d("MainActivity", "MainActivity onCreate() called");
            
            binding = ActivityMainBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());
            
            Log.d("MainActivity", "Layout inflated successfully");
            
            viewModel = new ViewModelProvider(this).get(MainViewModel.class);
            
            Log.d("MainActivity", "ViewModel created successfully");
            
            setupToolbar();
            setupUI();
            observeViewModel();
            checkPermissions();
            
            Log.d("MainActivity", "MainActivity setup completed");
            
        } catch (Exception e) {
            Log.e("MainActivity", "Error in MainActivity onCreate", e);
            // Show a simple error message and close gracefully
            finish();
        }
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("MMMUT WiFi Auto Login");
        }
    }

    private void setupUI() {
        // Load saved credentials
        viewModel.loadCredentials();
        
        // Set up click listeners
        binding.btnSave.setOnClickListener(v -> saveCredentials());
        
        // Set up toggle listener
        binding.switchAutoLogin.setOnCheckedChangeListener((buttonView, isChecked) -> 
            viewModel.setAutoLoginEnabled(isChecked));
        
        // Set up swipe refresh
        binding.swipeRefresh.setOnRefreshListener(() -> {
            viewModel.refreshStatus();
            binding.swipeRefresh.setRefreshing(false);
        });
    }

    private void observeViewModel() {
        viewModel.getRollNumber().observe(this, rollNumber -> 
            binding.etRollNumber.setText(rollNumber));
        
        viewModel.getPassword().observe(this, password -> 
            binding.etPassword.setText(password));
        
        viewModel.getAutoLoginEnabled().observe(this, enabled -> 
            binding.switchAutoLogin.setChecked(enabled));
        
        viewModel.getLastLoginTime().observe(this, time -> 
            binding.tvLastLoginTime.setText(time != null ? "Last login: " + time : "Never logged in"));
        
        viewModel.getLastLoginResult().observe(this, result -> 
            binding.tvLastLoginResult.setText(result != null ? "Status: " + result : "No status"));
        
        viewModel.getToastMessage().observe(this, message -> {
            if (message != null) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveCredentials() {
        String rollNumber = binding.etRollNumber.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        
        if (rollNumber.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter both roll number and password", Toast.LENGTH_SHORT).show();
            return;
        }
        
        viewModel.saveCredentials(rollNumber, password);
    }

    private void checkPermissions() {
        String[] permissions = {
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        };
        
        boolean allGranted = true;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                break;
            }
        }
        
        if (!allGranted) {
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            
            if (!allGranted) {
                Toast.makeText(this, "Permissions are required for WiFi auto-login to work", 
                    Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
