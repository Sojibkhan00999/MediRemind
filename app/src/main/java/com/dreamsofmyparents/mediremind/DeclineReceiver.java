package com.dreamsofmyparents.mediremind;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class DeclineReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String medicineName = intent.getStringExtra("medicine");

        // Stop alarm sound and vibration
        ReminderReceiver.stopAlarmSoundAndVibration();

        // Show snooze confirmation
        Toast.makeText(context, "⏰ " + medicineName + " পরে নেওয়া হবে!", Toast.LENGTH_LONG).show();

        // Cancel notification
        android.app.NotificationManager notificationManager = (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancelAll();
        }

        // Schedule snooze alarm for 5 minutes later
        ReminderPreferenceManager reminderManager = ReminderPreferenceManager.getInstance(context);
        int reminderId = intent.getIntExtra("reminder_id", -1);
        String dose = intent.getStringExtra("dose");
        String meal = intent.getStringExtra("meal");
        reminderManager.scheduleSnoozeAlarm(reminderId, medicineName, dose, meal);
    }
}
