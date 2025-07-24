package com.dreamsofmyparents.mediremind;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.os.VibrationEffect;
import android.os.Vibrator;

import androidx.core.app.NotificationCompat;

public class ReminderReceiver extends BroadcastReceiver {
    private static MediaPlayer mediaPlayer;
    private static Vibrator vibrator;
    private static PowerManager.WakeLock wakeLock;
    
    @Override
    public void onReceive(Context context, Intent intent) {
        String medName = intent.getStringExtra("medicine");
        String dose = intent.getStringExtra("dose");
        String meal = intent.getStringExtra("meal");
        int reminderId = intent.getIntExtra("reminder_id", -1);
        
        // Acquire wake lock to ensure alarm works
        acquireWakeLock(context);
        
        // Start alarm sound and vibration immediately
        startAlarmSoundAndVibration(context);

        // Create notification that launches AlarmActivity when clicked
        createAlarmNotification(context, medName, dose, meal, reminderId);
        
        // Reschedule daily reminder for next day
        if (reminderId != -1) {
            ReminderPreferenceManager reminderManager = ReminderPreferenceManager.getInstance(context);
            reminderManager.rescheduleDailyReminder(reminderId);
        }
    }
    
    private void acquireWakeLock(Context context) {
        try {
            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            if (powerManager != null && wakeLock == null) {
                wakeLock = powerManager.newWakeLock(
                    PowerManager.SCREEN_BRIGHT_WAKE_LOCK | 
                    PowerManager.ACQUIRE_CAUSES_WAKEUP | 
                    PowerManager.ON_AFTER_RELEASE, 
                    "MediRemind:AlarmWakeLock"
                );
                wakeLock.acquire(5*60*1000L /*5 minutes*/);
            }
        } catch (Exception e) {
            // Silent fail for production
        }
    }
    
    private void startAlarmSoundAndVibration(Context context) {
        // Start alarm sound
        try {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.release();
            }
            
            Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (alarmUri == null) {
                alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            }
            if (alarmUri == null) {
                alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            }
            
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(context, alarmUri);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
            mediaPlayer.setLooping(true);
            mediaPlayer.setVolume(1.0f, 1.0f);
            mediaPlayer.prepare();
            mediaPlayer.start();
            
        } catch (Exception e) {
            // Silent fail for production
        }
        
        // Start vibration
        try {
            vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null && vibrator.hasVibrator()) {
                long[] pattern = {0, 1000, 500, 1000, 500};
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0));
                } else {
                    vibrator.vibrate(pattern, 0);
                }
            }
        } catch (Exception e) {
            // Silent fail for production
        }
    }
    
    public static void stopAlarmSoundAndVibration() {
        // Stop alarm sound
        if (mediaPlayer != null) {
            try {
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
            } catch (Exception e) {
                // Silent fail for production
            }
        }
        
        // Stop vibration
        if (vibrator != null) {
            try {
                vibrator.cancel();
                vibrator = null;
            } catch (Exception e) {
                // Silent fail for production
            }
        }
        
        // Release wake lock
        if (wakeLock != null && wakeLock.isHeld()) {
            try {
                wakeLock.release();
                wakeLock = null;
            } catch (Exception e) {
                // Silent fail for production
            }
        }
    }
    
    private void createAlarmNotification(Context context, String medName, String dose, String meal, int reminderId) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "alarm_channel";

        // Create notification channel for Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                channelId, 
                "Medicine Alarms", 
                NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("ওষুধের এলার্ম নোটিফিকেশন");
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setBypassDnd(true);
            notificationManager.createNotificationChannel(channel);
        }

        // Intent to launch AlarmActivity when notification is clicked
        Intent alarmIntent = new Intent(context, AlarmActivity.class);
        alarmIntent.putExtra("medicine", medName);
        alarmIntent.putExtra("dose", dose);
        alarmIntent.putExtra("meal", meal);
        alarmIntent.putExtra("reminder_id", reminderId);
        alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        PendingIntent alarmPendingIntent = PendingIntent.getActivity(
            context, 
            reminderId, 
            alarmIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Accept action
        Intent acceptIntent = new Intent(context, AcceptReceiver.class);
        acceptIntent.putExtra("medicine", medName);
        acceptIntent.putExtra("dose", dose);
        acceptIntent.putExtra("meal", meal);
        acceptIntent.putExtra("reminder_id", reminderId);
        PendingIntent acceptPendingIntent = PendingIntent.getBroadcast(
            context, 
            1, 
            acceptIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Decline action  
        Intent declineIntent = new Intent(context, DeclineReceiver.class);
        declineIntent.putExtra("medicine", medName);
        declineIntent.putExtra("dose", dose);
        declineIntent.putExtra("meal", meal);
        declineIntent.putExtra("reminder_id", reminderId);
        PendingIntent declinePendingIntent = PendingIntent.getBroadcast(
            context, 
            2, 
            declineIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        String notificationText = medName + " (" + dose + ") " + meal + " খাওয়ার সময় হয়েছে";

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_medication)
            .setContentTitle("🚨 ওষুধের এলার্ম")
            .setContentText(notificationText)
            .setStyle(new NotificationCompat.BigTextStyle().bigText(notificationText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(alarmPendingIntent)
            .addAction(R.drawable.ic_medication, "নিয়েছি ✓", acceptPendingIntent)
            .addAction(R.drawable.ic_settings, "পরে নেব", declinePendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        notificationManager.notify(reminderId, builder.build());
    }
}
