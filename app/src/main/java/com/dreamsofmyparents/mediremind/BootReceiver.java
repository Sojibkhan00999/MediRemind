package com.dreamsofmyparents.mediremind;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import java.util.List;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()) || 
            Intent.ACTION_MY_PACKAGE_REPLACED.equals(intent.getAction())) {
            
            // Reschedule all reminders after boot
            rescheduleAllReminders(context);
        }
    }
    
    private void rescheduleAllReminders(Context context) {
        try {
            ReminderPreferenceManager reminderManager = ReminderPreferenceManager.getInstance(context);
            List<ReminderModel> reminders = reminderManager.getAllReminders();
            
            for (ReminderModel reminder : reminders) {
                // Just reschedule the alarm, don't add duplicate reminder
                reminderManager.rescheduleReminder(reminder);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
