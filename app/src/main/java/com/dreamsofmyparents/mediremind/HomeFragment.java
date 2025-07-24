package com.dreamsofmyparents.mediremind;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Calendar;
import java.util.List;

public class HomeFragment extends Fragment {

    private TextView todayReminderText, totalRemindersText, noRemindersText;
    private LinearLayout upcomingRemindersLayout, todayRemindersContainer;
    private Button btnAddReminder, btnViewAll;
    private ReminderPreferenceManager reminderManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize views
        todayReminderText = view.findViewById(R.id.todayReminder);
        totalRemindersText = view.findViewById(R.id.totalReminders);
        noRemindersText = view.findViewById(R.id.noRemindersText);
        upcomingRemindersLayout = view.findViewById(R.id.upcomingReminders);
        todayRemindersContainer = view.findViewById(R.id.todayRemindersContainer);
        btnAddReminder = view.findViewById(R.id.btnAddReminder);
        btnViewAll = view.findViewById(R.id.btnViewAll);

        // Initialize preference manager
        reminderManager = ReminderPreferenceManager.getInstance(getContext());

        // Set button click listeners
        setupButtonListeners();

        // Load and display data
        displayTodayReminder();
        displayUpcomingReminders();
        displayReminderStats();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh data when fragment becomes visible
        displayTodayReminder();
        displayUpcomingReminders();
        displayReminderStats();
    }

    private void setupButtonListeners() {
        if (btnAddReminder != null) {
            btnAddReminder.setOnClickListener(v -> {
                // Navigate to reminder fragment
                if (getActivity() != null && getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, new ReminderFragment())
                            .addToBackStack(null)
                            .commit();
                }
            });
            
            // Long click removed for production
        }

        if (btnViewAll != null) {
            btnViewAll.setOnClickListener(v -> {
                // Navigate to reminder list fragment
                if (getActivity() != null && getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, new ReminderListFragment())
                            .addToBackStack(null)
                            .commit();
                }
            });
        }
    }

    private void displayTodayReminder() {
        if (todayRemindersContainer == null) return;
        
        todayRemindersContainer.removeAllViews();
        List<ReminderModel> todayReminders = reminderManager.getTodayReminders();
        
        if (todayReminders.isEmpty()) {
            if (noRemindersText != null) {
                noRemindersText.setVisibility(View.VISIBLE);
            }
        } else {
            if (noRemindersText != null) {
                noRemindersText.setVisibility(View.GONE);
            }
            
            for (ReminderModel reminder : todayReminders) {
                View reminderView = createReminderItemView(reminder);
                todayRemindersContainer.addView(reminderView);
                // Alarm is already set by ReminderPreferenceManager, no need to set again
            }
        }
    }

    private void displayUpcomingReminders() {
        if (upcomingRemindersLayout == null) return;
        
        upcomingRemindersLayout.removeAllViews();
        List<ReminderModel> upcomingReminders = reminderManager.getUpcomingReminders();

        if (upcomingReminders.isEmpty()) {
            TextView noReminderView = new TextView(getContext());
            noReminderView.setText("কোনো আসন্ন রিমাইন্ডার নেই");
            noReminderView.setTextSize(14);
            noReminderView.setPadding(16, 16, 16, 16);
            noReminderView.setGravity(android.view.Gravity.CENTER);
            upcomingRemindersLayout.addView(noReminderView);
            return;
        }

        // Show next 3 upcoming reminders
        int count = Math.min(3, upcomingReminders.size());
        for (int i = 0; i < count; i++) {
            ReminderModel reminder = upcomingReminders.get(i);
            View reminderView = createReminderItemView(reminder);
            upcomingRemindersLayout.addView(reminderView);
        }
    }

    private View createReminderItemView(ReminderModel reminder) {
        LinearLayout itemLayout = new LinearLayout(getContext());
        itemLayout.setOrientation(LinearLayout.HORIZONTAL);
        itemLayout.setPadding(12, 8, 12, 8);
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 4, 0, 4);
        itemLayout.setLayoutParams(params);
        itemLayout.setBackgroundResource(android.R.drawable.list_selector_background);

        // Time
        TextView timeView = new TextView(getContext());
        timeView.setText(reminder.time);
        timeView.setTextSize(16);
        timeView.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
        timeView.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams timeParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
        );
        timeView.setLayoutParams(timeParams);

        // Medicine info
        LinearLayout infoLayout = new LinearLayout(getContext());
        infoLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 2f
        );
        infoLayout.setLayoutParams(infoParams);

        TextView medicineView = new TextView(getContext());
        medicineView.setText(reminder.medicine);
        medicineView.setTextSize(16);
        medicineView.setTextColor(getResources().getColor(android.R.color.black));
        medicineView.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView doseView = new TextView(getContext());
        doseView.setText(reminder.dose + " • " + reminder.meal);
        doseView.setTextSize(14);
        doseView.setTextColor(getResources().getColor(android.R.color.darker_gray));

        infoLayout.addView(medicineView);
        infoLayout.addView(doseView);

        itemLayout.addView(timeView);
        itemLayout.addView(infoLayout);

        return itemLayout;
    }

    private void displayReminderStats() {
        int totalReminders = reminderManager.getRemindersCount();
        int todayCount = reminderManager.getTodayReminders().size();
        
        if (totalRemindersText != null) {
            totalRemindersText.setText("মোট রিমাইন্ডার: " + totalReminders + "টি");
        }
        
        if (todayReminderText != null) {
            todayReminderText.setText("আজকের রিমাইন্ডার: " + todayCount + "টি");
        }
    }

    private void setExactAlarm(String timeStr, String medName, int reminderId) {
        try {
            String[] parts = timeStr.split(":");
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            // If time has passed today, set for tomorrow
            if (calendar.before(Calendar.getInstance())) {
                calendar.add(Calendar.DATE, 1);
            }

            Intent intent = new Intent(getContext(), ReminderReceiver.class);
            intent.putExtra("medicine", medName);
            intent.putExtra("reminder_id", reminderId);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    getContext(),
                    reminderId, // Use reminder ID as request code for unique alarms
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                // Check for exact alarm permission on Android 12+
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (!alarmManager.canScheduleExactAlarms()) {
                        Intent exactIntent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                        startActivity(exactIntent);
                        return;
                    }
                }

                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "অ্যালার্ম সেট করতে সমস্যা হয়েছে", Toast.LENGTH_SHORT).show();
        }
    }
}
