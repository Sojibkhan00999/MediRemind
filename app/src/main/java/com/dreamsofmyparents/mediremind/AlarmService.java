package com.dreamsofmyparents.mediremind;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;

public class AlarmService extends Service {
    private static final String CHANNEL_ID = "AlarmServiceChannel";
    private static final int NOTIFICATION_ID = 1001;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getStringExtra("action");
            
            if ("LAUNCH_ALARM".equals(action)) {
                String medName = intent.getStringExtra("medicine");
                String dose = intent.getStringExtra("dose");
                String meal = intent.getStringExtra("meal");
                int reminderId = intent.getIntExtra("reminder_id", -1);
                
                // Start foreground service
                startForeground(NOTIFICATION_ID, createServiceNotification());
                
                // Launch alarm activity from foreground service
                launchAlarmActivity(medName, dose, meal, reminderId);
                
                // Stop service after launching alarm
                stopSelf();
            }
        }
        
        return START_NOT_STICKY;
    }

    private void launchAlarmActivity(String medName, String dose, String meal, int reminderId) {
        try {
            Intent alarmIntent = new Intent(this, AlarmActivity.class);
            alarmIntent.putExtra("medicine", medName);
            alarmIntent.putExtra("dose", dose);
            alarmIntent.putExtra("meal", meal);
            alarmIntent.putExtra("reminder_id", reminderId);
            
            // Critical flags for service to activity launch
            alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | 
                               Intent.FLAG_ACTIVITY_CLEAR_TOP |
                               Intent.FLAG_ACTIVITY_SINGLE_TOP |
                               Intent.FLAG_ACTIVITY_NO_USER_ACTION);
            
            startActivity(alarmIntent);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Notification createServiceNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 
            PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("MediRemind Alarm Service")
                .setContentText("Preparing alarm...")
                .setSmallIcon(R.drawable.ic_medication)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                CHANNEL_ID,
                "Alarm Service Channel",
                NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
