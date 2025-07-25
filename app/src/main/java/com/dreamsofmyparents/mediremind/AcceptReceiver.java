package com.dreamsofmyparents.mediremind;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class AcceptReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String medicineName = intent.getStringExtra("medicine");

        // Stop alarm sound and vibration
        ReminderReceiver.stopAlarmSoundAndVibration();

        // Show confirmation
        Toast.makeText(context, "✅ " + medicineName + " নেওয়া হয়েছে!", Toast.LENGTH_LONG).show();

        // Cancel notification
        android.app.NotificationManager notificationManager = (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancelAll();
        }
    }
}
