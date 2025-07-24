package com.dreamsofmyparents.mediremind;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity implements PermissionHelper.PermissionCallback {

    // Custom Bottom Navigation Views
    private FloatingActionButton fabHome, fabReminder, fabReport, fabProfile, fabSettings;
    
    private int currentSelectedTab = 0; // 0=Home, 1=Reminder, 2=Report, 3=Profile, 4=Settings
    private PermissionHelper permissionHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize permission helper
        permissionHelper = new PermissionHelper(this);
        
        // Check permissions first
        checkRequiredPermissions();

        initCustomBottomNavigation();
        
        // Default fragment when app opens
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        }
        
        // Set home as selected by default
        updateSelectedTab(0);
    }
    
    private void checkRequiredPermissions() {
        if (!PermissionHelper.hasAllRequiredPermissions(this)) {
            permissionHelper.checkAndRequestAllPermissions(this);
        }
    }

    private void initCustomBottomNavigation() {
        // Initialize FABs
        fabHome = findViewById(R.id.fab_home);
        fabReminder = findViewById(R.id.fab_reminder);
        fabReport = findViewById(R.id.fab_report);
        fabProfile = findViewById(R.id.fab_profile);
        fabSettings = findViewById(R.id.fab_settings);
        
        // Set click listeners only on FABs
        fabHome.setOnClickListener(v -> {
            if (currentSelectedTab != 0) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new HomeFragment())
                        .commit();
                updateSelectedTab(0);
            }
        });
        
        fabReminder.setOnClickListener(v -> {
            if (currentSelectedTab != 1) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new ReminderFragment())
                        .commit();
                updateSelectedTab(1);
            }
        });
        
        fabReport.setOnClickListener(v -> {
            if (currentSelectedTab != 2) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new ReportFragment())
                        .commit();
                updateSelectedTab(2);
            }
        });
        
        fabProfile.setOnClickListener(v -> {
            if (currentSelectedTab != 3) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new ProfileFragment())
                        .commit();
                updateSelectedTab(3);
            }
        });
        
        fabSettings.setOnClickListener(v -> {
            if (currentSelectedTab != 4) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new SettingsFragment())
                        .commit();
                updateSelectedTab(4);
            }
        });
    }
    
    private void updateSelectedTab(int selectedTab) {
        currentSelectedTab = selectedTab;
        
        // Reset all to inactive state
        resetAllTabs();
        
        // Set selected tab to active state
        switch (selectedTab) {
            case 0: // Home
                fabHome.setBackgroundTintList(getColorStateList(R.color.primary_blue));
                fabHome.setImageTintList(getColorStateList(R.color.text_white));
                break;
            case 1: // Reminder
                fabReminder.setBackgroundTintList(getColorStateList(R.color.primary_blue));
                fabReminder.setImageTintList(getColorStateList(R.color.text_white));
                break;
            case 2: // Report
                fabReport.setBackgroundTintList(getColorStateList(R.color.primary_blue));
                fabReport.setImageTintList(getColorStateList(R.color.text_white));
                break;
            case 3: // Profile
                fabProfile.setBackgroundTintList(getColorStateList(R.color.primary_blue));
                fabProfile.setImageTintList(getColorStateList(R.color.text_white));
                break;
            case 4: // Settings
                fabSettings.setBackgroundTintList(getColorStateList(R.color.primary_blue));
                fabSettings.setImageTintList(getColorStateList(R.color.text_white));
                break;
        }
    }
    
    private void resetAllTabs() {
        // Reset all FABs to inactive state (white background, gray icon)
        fabHome.setBackgroundTintList(getColorStateList(R.color.background_white));
        fabHome.setImageTintList(getColorStateList(R.color.text_secondary));
        
        fabReminder.setBackgroundTintList(getColorStateList(R.color.background_white));
        fabReminder.setImageTintList(getColorStateList(R.color.text_secondary));
        
        fabReport.setBackgroundTintList(getColorStateList(R.color.background_white));
        fabReport.setImageTintList(getColorStateList(R.color.text_secondary));
        
        fabProfile.setBackgroundTintList(getColorStateList(R.color.background_white));
        fabProfile.setImageTintList(getColorStateList(R.color.text_secondary));
        
        fabSettings.setBackgroundTintList(getColorStateList(R.color.background_white));
        fabSettings.setImageTintList(getColorStateList(R.color.text_secondary));
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }
    
    // Permission callback methods
    @Override
    public void onPermissionsGranted() {
        // Permissions granted, app can work normally
        // You can show a success message if needed
    }
    
    @Override
    public void onPermissionsDenied() {
        // Show message that some features may not work
        // You can show a dialog or toast here explaining the importance of permissions
    }
    
    // Handle permission request results
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (permissionHelper != null) {
            permissionHelper.handlePermissionResult(requestCode, permissions, grantResults);
        }
    }
    
    // Handle activity results (for settings intents)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (permissionHelper != null) {
            permissionHelper.handleActivityResult(requestCode, resultCode);
        }
    }

}
