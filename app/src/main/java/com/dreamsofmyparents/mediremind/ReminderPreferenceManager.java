package com.dreamsofmyparents.mediremind;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.jummania.DataManager;
import com.jummania.DataManagerFactory;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;

public class ReminderPreferenceManager {
    private static final String PREF_NAME = "reminder_prefs";
    private static final String KEY_REMINDERS = "reminders";
    private static final String KEY_REMINDER_ID_COUNTER = "reminder_id_counter";

    private static ReminderPreferenceManager instance;
    private final Context context;
    private final DataManager dataManager;

    private ReminderPreferenceManager(Context context) {
        this.context = context.getApplicationContext();
        dataManager = DataManagerFactory.create(context.getFilesDir());
    }

    public static ReminderPreferenceManager getInstance(Context context) {
        if (instance == null) {
            instance = new ReminderPreferenceManager(context.getApplicationContext());
        }
        return instance;
    }

    // Add new reminder
    public void addReminder(ReminderModel reminder) {
        List<ReminderModel> reminders = getAllReminders();

        // Generate unique ID
        reminder.id = getNextId();

        reminders.add(reminder);
        saveReminders(reminders);

        // Schedule alarm for this reminder
        scheduleAlarm(reminder);
    }

    // Get all reminders
    public List<ReminderModel> getAllReminders() {
        return dataManager.getFullList(KEY_REMINDERS, ReminderModel.class);
    }

    // Get upcoming reminders (sorted by time)
    public List<ReminderModel> getUpcomingReminders() {
        List<ReminderModel> allReminders = getAllReminders();
        List<ReminderModel> upcomingReminders = new ArrayList<>();

        // Get current time in HH:mm format
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
        String currentTime = sdf.format(new java.util.Date());

        for (ReminderModel reminder : allReminders) {
            if (reminder.time.compareTo(currentTime) > 0) {
                upcomingReminders.add(reminder);
            }
        }

        // Sort by time
        upcomingReminders.sort(Comparator.comparing(r -> r.time));

        return upcomingReminders;
    }

    // Delete reminder by ID
    public void deleteReminder(int id) {
        // Cancel alarm before deleting
        cancelAlarm(id);

        List<ReminderModel> reminders = getAllReminders();
        reminders.removeIf(reminder -> reminder.id == id);
        saveReminders(reminders);
    }

    // Update reminder
    public void updateReminder(ReminderModel updatedReminder) {
        List<ReminderModel> reminders = getAllReminders();
        for (int i = 0; i < reminders.size(); i++) {
            if (reminders.get(i).id == updatedReminder.id) {
                reminders.set(i, updatedReminder);
                break;
            }
        }
        saveReminders(reminders);
    }

    // Get reminder by ID
    public ReminderModel getReminderById(int id) {
        List<ReminderModel> reminders = getAllReminders();
        for (ReminderModel reminder : reminders) {
            if (reminder.id == id) {
                return reminder;
            }
        }
        return null;
    }

    // Private helper methods
    private void saveReminders(List<ReminderModel> reminders) {
        dataManager.saveList(KEY_REMINDERS, reminders);
    }

    private int getNextId() {
        int currentId = dataManager.getInt(KEY_REMINDER_ID_COUNTER, 0);
        int nextId = currentId + 1;
        dataManager.saveInt(KEY_REMINDER_ID_COUNTER, nextId);
        return nextId;
    }

    // Clear all reminders (for testing purposes)
    public void clearAllReminders() {
        dataManager.remove(KEY_REMINDERS);
    }

    // Get reminders count
    public int getRemindersCount() {
        return getAllReminders().size();
    }

    // Get today's reminders (all reminders for current day)
    public List<ReminderModel> getTodayReminders() {
        List<ReminderModel> allReminders = getAllReminders();
        List<ReminderModel> todayReminders = new ArrayList<>();

        // For daily reminders, all are considered today's reminders
        // For "once" reminders, we can add logic to check if they are for today
        for (ReminderModel reminder : allReminders) {
            if ("daily".equals(reminder.frequency) || "once".equals(reminder.frequency)) {
                todayReminders.add(reminder);
            }
        }

        // Sort by time
        todayReminders.sort(Comparator.comparing(r -> r.time));

        return todayReminders;
    }

    // Alarm Management Methods
    private void scheduleAlarm(ReminderModel reminder) {
        try {
            AlarmManager alarmManager = ContextCompat.getSystemService(context, AlarmManager.class);
            if (alarmManager == null) return;

            Intent intent = new Intent(context, ReminderReceiver.class);
            intent.putExtra("medicine", reminder.medicine != null ? reminder.medicine : reminder.name);
            intent.putExtra("dose", reminder.dose);
            intent.putExtra("meal", reminder.meal != null ? reminder.meal : "খাবার পরে");
            intent.putExtra("reminder_id", reminder.id);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, reminder.id, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            // Parse time (HH:mm format)
            String[] timeParts = reminder.time.split(":");
            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1]);

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            // If the time has already passed today, schedule for tomorrow
            if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }

            // Schedule alarm based on frequency
            if ("daily".equals(reminder.frequency)) {
                // For daily reminders, use repeating alarm
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            } else if ("once".equals(reminder.frequency)) {
                // For one-time reminders
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            }

            Toast.makeText(context, "রিমাইন্ডার সেট করা হয়েছে: " + reminder.time, Toast.LENGTH_SHORT).show();

        } catch (Exception ignored) {
            Toast.makeText(context, "রিমাইন্ডার সেট করতে সমস্যা হয়েছে", Toast.LENGTH_SHORT).show();
        }
    }

    private void cancelAlarm(int reminderId) {
        try {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager == null) return;

            Intent intent = new Intent(context, ReminderReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, reminderId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();

        } catch (Exception ignored) {
        }
    }

    // Method to reschedule daily reminders (call this from ReminderReceiver for daily reminders)
    public void rescheduleDailyReminder(int reminderId) {
        ReminderModel reminder = getReminderById(reminderId);
        if (reminder != null && "daily".equals(reminder.frequency)) {
            scheduleAlarm(reminder);
        }
    }

    // Method to reschedule a specific reminder (used by BootReceiver)
    public void rescheduleReminder(ReminderModel reminder) {
        if (reminder != null) {
            scheduleAlarm(reminder);
        }
    }

    // Method to schedule snooze alarm (5 minutes later)
    public void scheduleSnoozeAlarm(int reminderId, String medicine, String dose, String meal) {
        try {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager == null) return;

            Intent intent = new Intent(context, ReminderReceiver.class);
            intent.putExtra("medicine", medicine);
            intent.putExtra("dose", dose);
            intent.putExtra("meal", meal);
            intent.putExtra("reminder_id", reminderId);

            // Use a different request code for snooze to avoid conflicts
            int snoozeRequestCode = reminderId + 10000;
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, snoozeRequestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            // Schedule alarm for 5 minutes from now
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MINUTE, 5);

            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);

            Toast.makeText(context, "৫ মিনিট পরে আবার রিমাইন্ড করা হবে", Toast.LENGTH_SHORT).show();

        } catch (Exception ignored) {
        }
    }

}
