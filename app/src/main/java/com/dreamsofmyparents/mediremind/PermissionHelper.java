package com.dreamsofmyparents.mediremind;

import android.Manifest;
import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionHelper {

    private static final int NOTIFICATION_PERMISSION_CODE = 1001;
    private static final int EXACT_ALARM_PERMISSION_CODE = 1002;
    private static final int BATTERY_OPTIMIZATION_CODE = 1003;

    private final AppCompatActivity activity;
    private PermissionCallback callback;

    public PermissionHelper(AppCompatActivity activity) {
        this.activity = activity;
    }

    public static boolean hasAllRequiredPermissions(Context context) {
        // Check notification permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        // Check exact alarm permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            return alarmManager == null || alarmManager.canScheduleExactAlarms();
        }

        return true;
    }

    public void checkAndRequestAllPermissions(PermissionCallback callback) {
        this.callback = callback;

        // Check notification permission first
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestNotificationPermission();
                return;
            }
        }

        // Check exact alarm permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                requestExactAlarmPermission();
                return;
            }
        }

        // Check battery optimization
        if (!isBatteryOptimizationDisabled()) {
            requestDisableBatteryOptimization();
            return;
        }

        // All permissions granted
        if (callback != null) {
            callback.onPermissionsGranted();
        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            new AlertDialog.Builder(activity).setTitle("নোটিফিকেশন অনুমতি প্রয়োজন").setMessage("রিমাইন্ডার কাজ করার জন্য নোটিফিকেশন অনুমতি দিন।").setPositiveButton("অনুমতি দিন", (dialog, which) -> ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_CODE)).setNegativeButton("বাতিল", (dialog, which) -> {
                if (callback != null) callback.onPermissionsDenied();
            }).show();
        }
    }

    private void requestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            new AlertDialog.Builder(activity).setTitle("সঠিক সময়ে অ্যালার্ম অনুমতি").setMessage("রিমাইন্ডার সঠিক সময়ে কাজ করার জন্য 'Alarms & reminders' অনুমতি চালু করুন।").setPositiveButton("সেটিংস খুলুন", (dialog, which) -> {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                intent.setData(Uri.parse("package:" + activity.getPackageName()));
                activity.startActivityForResult(intent, EXACT_ALARM_PERMISSION_CODE);
            }).setNegativeButton("বাতিল", (dialog, which) -> {
                if (callback != null) callback.onPermissionsDenied();
            }).show();
        }
    }

    private void requestDisableBatteryOptimization() {
        new AlertDialog.Builder(activity).setTitle("ব্যাটারি অপটিমাইজেশন বন্ধ করুন").setMessage("রিমাইন্ডার সব সময় কাজ করার জন্য এই অ্যাপের ব্যাটারি অপটিমাইজেশন বন্ধ করুন।").setPositiveButton("সেটিংস খুলুন", (dialog, which) -> {
            Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + activity.getPackageName()));
            activity.startActivityForResult(intent, BATTERY_OPTIMIZATION_CODE);
        }).setNegativeButton("পরে করব", (dialog, which) -> {
            // Continue without disabling battery optimization
            if (callback != null) callback.onPermissionsGranted();
        }).show();
    }

    private boolean isBatteryOptimizationDisabled() {
        PowerManager powerManager = ContextCompat.getSystemService(activity, PowerManager.class);
        return powerManager != null && powerManager.isIgnoringBatteryOptimizations(activity.getPackageName());
    }

    public void handlePermissionResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Continue with next permission check
                checkAndRequestAllPermissions(callback);
            } else {
                Toast.makeText(activity, "নোটিফিকেশন অনুমতি প্রয়োজন", Toast.LENGTH_LONG).show();
                if (callback != null) callback.onPermissionsDenied();
            }
        }
    }

    public void handleActivityResult(int requestCode, int resultCode) {
        switch (requestCode) {
            case EXACT_ALARM_PERMISSION_CODE:
            case BATTERY_OPTIMIZATION_CODE:
                // Continue with permission check
                checkAndRequestAllPermissions(callback);
                break;
        }
    }

    public interface PermissionCallback {
        void onPermissionsGranted();

        void onPermissionsDenied();
    }
}
