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
import com.mmmut.wifiautologin.databinding.ActivityMainBinding;
import com.mmmut.wifiautologin.viewmodel.MainViewModel;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int PERMISSION_REQUEST_CODE = 1001;
    private ActivityMainBinding binding;
    private MainViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            Log.d(TAG, "MainActivity onCreate() called");
            
            // Initialize binding
            try {
                binding = ActivityMainBinding.inflate(getLayoutInflater());
                setContentView(binding.getRoot());
                Log.d(TAG, "Layout inflated successfully");
            } catch (Exception e) {
                Log.e(TAG, "Error inflating layout", e);
                // Fallback to basic layout
                setContentView(com.mmmut.wifiautologin.R.layout.activity_main);
                binding = null;
            }
            
            // Initialize ViewModel
            try {
                viewModel = new ViewModelProvider(this).get(MainViewModel.class);
                Log.d(TAG, "ViewModel created successfully");
            } catch (Exception e) {
                Log.e(TAG, "Error creating ViewModel", e);
                Toast.makeText(this, "Error initializing app", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            
            setupToolbar();
            setupUI();
            observeViewModel();
            checkPermissions();
            
            Log.d(TAG, "MainActivity setup completed");
            
        } catch (Exception e) {
            Log.e(TAG, "Critical error in MainActivity onCreate", e);
            Toast.makeText(this, "App initialization failed", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void setupToolbar() {
        try {
            if (binding != null && binding.toolbar != null) {
                setSupportActionBar(binding.toolbar);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle("MMMUT WiFi Auto Login");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up toolbar", e);
        }
    }

    private void setupUI() {
        if (binding == null) {
            Log.e(TAG, "Binding is null, cannot setup UI");
            return;
        }
        
        try {
            // Load saved credentials
            viewModel.loadCredentials();
            
            // Set up click listeners
            if (binding.btnSave != null) {
                binding.btnSave.setOnClickListener(v -> saveCredentials());
            }
            
            // Set up toggle listener
            if (binding.switchAutoLogin != null) {
                binding.switchAutoLogin.setOnCheckedChangeListener((buttonView, isChecked) -> 
                    viewModel.setAutoLoginEnabled(isChecked));
            }
            
            // Set up swipe refresh
            if (binding.swipeRefresh != null) {
                binding.swipeRefresh.setOnRefreshListener(() -> {
                    viewModel.refreshStatus();
                    binding.swipeRefresh.setRefreshing(false);
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up UI", e);
        }
    }

    private void observeViewModel() {
        if (viewModel == null) {
            Log.e(TAG, "ViewModel is null, cannot observe");
            return;
        }
        
        try {
            viewModel.getRollNumber().observe(this, rollNumber -> {
                if (binding != null && binding.etRollNumber != null) {
                    binding.etRollNumber.setText(rollNumber != null ? rollNumber : "");
                }
            });
            
            viewModel.getPassword().observe(this, password -> {
                if (binding != null && binding.etPassword != null) {
                    binding.etPassword.setText(password != null ? password : "");
                }
            });
            
            viewModel.getAutoLoginEnabled().observe(this, enabled -> {
                if (binding != null && binding.switchAutoLogin != null) {
                    binding.switchAutoLogin.setChecked(enabled != null ? enabled : false);
                }
            });
            
            viewModel.getLastLoginTime().observe(this, time -> {
                if (binding != null && binding.tvLastLoginTime != null) {
                    binding.tvLastLoginTime.setText(time != null ? "Last login: " + time : "Never logged in");
                }
            });
            
            viewModel.getLastLoginResult().observe(this, result -> {
                if (binding != null && binding.tvLastLoginResult != null) {
                    binding.tvLastLoginResult.setText(result != null ? "Status: " + result : "No status");
                }
            });
            
            viewModel.getToastMessage().observe(this, message -> {
                if (message != null) {
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error observing ViewModel", e);
        }
    }

    private void saveCredentials() {
        if (binding == null) {
            Toast.makeText(this, "UI not available", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            String rollNumber = "";
            String password = "";
            
            if (binding.etRollNumber != null) {
                rollNumber = binding.etRollNumber.getText().toString().trim();
            }
            
            if (binding.etPassword != null) {
                password = binding.etPassword.getText().toString().trim();
            }
            
            if (rollNumber.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both roll number and password", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Validate input to prevent crashes
            if (rollNumber.length() > 50 || password.length() > 100) {
                Toast.makeText(this, "Input too long. Please use shorter values.", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (viewModel != null) {
                viewModel.saveCredentials(rollNumber, password);
                Log.d(TAG, "Credentials saved successfully for roll number: " + rollNumber);
            } else {
                Toast.makeText(this, "App not properly initialized", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error saving credentials", e);
            Toast.makeText(this, "Error saving credentials: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void checkPermissions() {
        try {
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
        } catch (Exception e) {
            Log.e(TAG, "Error checking permissions", e);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == PERMISSION_REQUEST_CODE) {
            try {
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
            } catch (Exception e) {
                Log.e(TAG, "Error handling permission results", e);
            }
        }
    }

    @Override
    protected void onDestroy() {
        try {
            super.onDestroy();
            binding = null;
        } catch (Exception e) {
            Log.e(TAG, "Error in onDestroy", e);
        }
    }
}
