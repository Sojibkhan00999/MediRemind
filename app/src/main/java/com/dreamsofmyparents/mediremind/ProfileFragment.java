package com.dreamsofmyparents.mediremind;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ProfileFragment extends Fragment {

    private TextView appTitle, appVersion, totalReminders, todayReminders, upcomingReminders;
    private ImageView appIcon;
    private Button btnMyReminders, btnClearAllReminders, btnAbout;
    private ReminderPreferenceManager reminderManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize views
        appTitle = view.findViewById(R.id.appTitle);
        appVersion = view.findViewById(R.id.appVersion);
        totalReminders = view.findViewById(R.id.totalReminders);
        todayReminders = view.findViewById(R.id.todayReminders);
        upcomingReminders = view.findViewById(R.id.upcomingReminders);
        appIcon = view.findViewById(R.id.appIcon);
        btnMyReminders = view.findViewById(R.id.btnMyReminders);
        btnClearAllReminders = view.findViewById(R.id.btnClearAllReminders);
        btnAbout = view.findViewById(R.id.btnAbout);

        // Initialize preference manager
        reminderManager = ReminderPreferenceManager.getInstance(getContext());

        // Set app info
        appTitle.setText("MediRemind");
        appVersion.setText("সংস্করণ ১.০");

        // Load reminder statistics
        loadReminderStats();

        // Set button listeners
        btnMyReminders.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), MyRemindActivity.class);
            startActivity(intent);
        });

        btnClearAllReminders.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(getContext())
                    .setTitle("সব রিমাইন্ডার মুছুন")
                    .setMessage("আপনি কি সব রিমাইন্ডার মুছে ফেলতে চান? এই কাজটি পূর্বাবস্থায় ফেরানো যাবে না।")
                    .setPositiveButton("হ্যাঁ", (dialog, which) -> {
                        reminderManager.clearAllReminders();
                        loadReminderStats();
                        Toast.makeText(getContext(), "সব রিমাইন্ডার মুছে ফেলা হয়েছে", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("না", null)
                    .show();
        });

        btnAbout.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(getContext())
                    .setTitle("MediRemind সম্পর্কে")
                    .setMessage("MediRemind একটি ওষুধের রিমাইন্ডার অ্যাপ যা আপনাকে সময়মতো ওষুধ খেতে সাহায্য করে।\n\n" +
                            "বৈশিষ্ট্যসমূহ:\n" +
                            "• ওষুধের রিমাইন্ডার সেট করুন\n" +
                            "• বিভিন্ন ধরনের ওষুধ সাপোর্ট\n" +
                            "• সময়মতো নোটিফিকেশন\n" +
                            "• সহজ ব্যবহার\n\n" +
                            "সংস্করণ: ১.০\n" +
                            "তৈরি করেছেন: MediRemind Team")
                    .setPositiveButton("ঠিক আছে", null)
                    .show();
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadReminderStats(); // Refresh stats when fragment becomes visible
    }

    private void loadReminderStats() {
        int total = reminderManager.getRemindersCount();
        int upcoming = reminderManager.getUpcomingReminders().size();
        int today = upcoming > 0 ? 1 : 0; // Simplified logic for today's reminders

        totalReminders.setText("মোট রিমাইন্ডার: " + total + "টি");
        todayReminders.setText("আজকের রিমাইন্ডার: " + today + "টি");
        upcomingReminders.setText("আসন্ন রিমাইন্ডার: " + upcoming + "টি");
    }
}
