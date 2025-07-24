package com.dreamsofmyparents.mediremind;

import android.app.KeyguardManager;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class AlarmActivity extends AppCompatActivity {
    
    private MediaPlayer mediaPlayer;
    private Vibrator vibrator;
    private PowerManager.WakeLock wakeLock;
    private String medicineName;
    private String dose;
    private String meal;
    private int reminderId;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Setup window flags for lock screen and background operation
        setupWindowFlags();
        
        setContentView(R.layout.activity_alarm);
        
        // Get data from intent
        medicineName = getIntent().getStringExtra("medicine");
        dose = getIntent().getStringExtra("dose");
        meal = getIntent().getStringExtra("meal");
        reminderId = getIntent().getIntExtra("reminder_id", -1);
        
        // Acquire wake lock to ensure alarm works in background
        acquireWakeLock();
        
        initViews();
        startAlarmSoundAndVibration();
    }
    
    private void setupWindowFlags() {
        Window window = getWindow();
        
        // For Android 8.1+ (API 27+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
            
            // Additional flags for better lock screen handling
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                           WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
        } else {
            // For older versions
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                           WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                           WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                           WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                           WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
        }
        
        // Make it high priority and full screen
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        // For showing over other apps (if permission granted)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(this)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                window.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
            } else {
                window.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            }
        }
        
        // Dismiss keyguard
        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            keyguardManager.requestDismissKeyguard(this, null);
        }
    }
    
    private void acquireWakeLock() {
        try {
            PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
            if (powerManager != null) {
                wakeLock = powerManager.newWakeLock(
                    PowerManager.SCREEN_BRIGHT_WAKE_LOCK | 
                    PowerManager.ACQUIRE_CAUSES_WAKEUP | 
                    PowerManager.ON_AFTER_RELEASE, 
                    "MediRemind:AlarmWakeLock"
                );
                wakeLock.acquire(10*60*1000L /*10 minutes*/);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void initViews() {
        TextView tvMedicine = findViewById(R.id.tv_medicine_name);
        TextView tvDose = findViewById(R.id.tv_dose);
        TextView tvMeal = findViewById(R.id.tv_meal);
        Button btnTaken = findViewById(R.id.btn_taken);
        Button btnSnooze = findViewById(R.id.btn_snooze);
        Button btnDismiss = findViewById(R.id.btn_dismiss);
        
        tvMedicine.setText(medicineName != null ? medicineName : "ওষুধ");
        tvDose.setText("ডোজ: " + (dose != null ? dose : ""));
        tvMeal.setText(meal != null ? meal : "");
        
        btnTaken.setOnClickListener(v -> {
            stopAlarmSoundAndVibration();
            // Stop alarm from ReminderReceiver as well
            ReminderReceiver.stopAlarmSoundAndVibration();
            // Mark as taken
            finish();
        });
        
        btnSnooze.setOnClickListener(v -> {
            stopAlarmSoundAndVibration();
            // Stop alarm from ReminderReceiver as well
            ReminderReceiver.stopAlarmSoundAndVibration();
            // Snooze for 5 minutes
            scheduleSnooze();
            finish();
        });
        
        btnDismiss.setOnClickListener(v -> {
            stopAlarmSoundAndVibration();
            // Stop alarm from ReminderReceiver as well
            ReminderReceiver.stopAlarmSoundAndVibration();
            finish();
        });
    }
    
    private void startAlarmSoundAndVibration() {
        // Start alarm sound
        try {
            Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (alarmUri == null) {
                alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            }
            
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(this, alarmUri);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
            mediaPlayer.setLooping(true);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Start vibration
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            long[] pattern = {0, 1000, 1000}; // Wait 0ms, vibrate 1000ms, wait 1000ms
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0));
            } else {
                vibrator.vibrate(pattern, 0);
            }
        }
    }
    
    private void stopAlarmSoundAndVibration() {
        // Stop alarm sound
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        
        // Stop vibration
        if (vibrator != null) {
            vibrator.cancel();
        }
    }
    
    private void scheduleSnooze() {
        // Schedule alarm again after 5 minutes
        ReminderPreferenceManager reminderManager = ReminderPreferenceManager.getInstance(this);
        reminderManager.scheduleSnoozeAlarm(reminderId, medicineName, dose, meal);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAlarmSoundAndVibration();
        
        // Release wake lock
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
    }
    
    @Override
    protected void onPause() {
        // Call super but keep alarm running
        super.onPause();
    }
    
    @Override
    protected void onStop() {
        // Call super but keep alarm running
        super.onStop();
    }
    
    @Override
    public void onBackPressed() {
        // Prevent back button from dismissing alarm
        // User must use the buttons
    }
}
