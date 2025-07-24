package com.dreamsofmyparents.mediremind;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

public class SettingsFragment extends Fragment {

    private SharedPreferences sharedPreferences;
    private Switch darkModeSwitch, vibrationSwitch;
    private LinearLayout languageOption, privacyPolicyOption, developerInfoOption, otherAppsOption;
    private LinearLayout alarmToneOption, snoozeDurationOption, testAlarmOption;
    private TextView selectedToneText, snoozeDurationText;
    private SeekBar volumeSeekBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        
        initViews(view);
        setupClickListeners();
        loadSettings();
        
        return view;
    }
    
    private void initViews(View view) {
        sharedPreferences = getActivity().getSharedPreferences("MediRemindSettings", getContext().MODE_PRIVATE);
        
        // Alarm Settings
        alarmToneOption = view.findViewById(R.id.option_alarm_tone);
        vibrationSwitch = view.findViewById(R.id.switch_vibration);
        volumeSeekBar = view.findViewById(R.id.seekbar_volume);
        snoozeDurationOption = view.findViewById(R.id.option_snooze_duration);
        testAlarmOption = view.findViewById(R.id.option_test_alarm);
        selectedToneText = view.findViewById(R.id.text_selected_tone);
        snoozeDurationText = view.findViewById(R.id.text_snooze_duration);
        
        // App Settings
        darkModeSwitch = view.findViewById(R.id.switch_dark_mode);
        languageOption = view.findViewById(R.id.option_language);
        privacyPolicyOption = view.findViewById(R.id.option_privacy_policy);
        developerInfoOption = view.findViewById(R.id.option_developer_info);
        otherAppsOption = view.findViewById(R.id.option_other_apps);
    }
    
    private void setupClickListeners() {
        // Alarm Tone Selection
        alarmToneOption.setOnClickListener(v -> {
            showAlarmToneSelector();
        });
        
        // Vibration Toggle
        vibrationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("alarm_vibration", isChecked);
            editor.apply();
            
            Toast.makeText(getContext(), isChecked ? "ভাইব্রেশন চালু করা হয়েছে" : "ভাইব্রেশন বন্ধ করা হয়েছে", Toast.LENGTH_SHORT).show();
        });
        
        // Volume SeekBar
        volumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt("alarm_volume", progress);
                    editor.apply();
                }
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Toast.makeText(getContext(), "ভলিউম সেট করা হয়েছে: " + seekBar.getProgress() + "%", Toast.LENGTH_SHORT).show();
            }
        });
        
        // Snooze Duration
        snoozeDurationOption.setOnClickListener(v -> {
            showSnoozeDurationSelector();
        });
        
        // Test Alarm - Removed for production
        testAlarmOption.setOnClickListener(v -> {
            Toast.makeText(getContext(), "টেস্ট এলার্ম বৈশিষ্ট্য প্রোডাকশনে উপলব্ধ নয়", Toast.LENGTH_SHORT).show();
        });
        
        // Dark Mode Toggle
        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("dark_mode", isChecked);
            editor.apply();
            
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
            
            Toast.makeText(getContext(), isChecked ? "ডার্ক মোড চালু করা হয়েছে" : "লাইট মোড চালু করা হয়েছে", Toast.LENGTH_SHORT).show();
        });
        
        // Language Change
        languageOption.setOnClickListener(v -> {
            Toast.makeText(getContext(), "ভাষা পরিবর্তন বৈশিষ্ট্য শীঘ্রই আসছে", Toast.LENGTH_SHORT).show();
        });
        
        // Privacy Policy
        privacyPolicyOption.setOnClickListener(v -> {
            try {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://example.com/privacy-policy"));
                startActivity(browserIntent);
            } catch (Exception e) {
                Toast.makeText(getContext(), "ব্রাউজার খুলতে সমস্যা হয়েছে", Toast.LENGTH_SHORT).show();
            }
        });
        
        // Developer Info
        developerInfoOption.setOnClickListener(v -> {
            showDeveloperInfo();
        });
        
        // Other Apps
        otherAppsOption.setOnClickListener(v -> {
            try {
                Intent playStoreIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/developer?id=DREAMS OF MY PARENTS"));
                startActivity(playStoreIntent);
            } catch (Exception e) {
                Toast.makeText(getContext(), "প্লে স্টোর খুলতে সমস্যা হয়েছে", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void loadSettings() {
        // Load alarm settings
        boolean isVibrationEnabled = sharedPreferences.getBoolean("alarm_vibration", true);
        vibrationSwitch.setChecked(isVibrationEnabled);
        
        int alarmVolume = sharedPreferences.getInt("alarm_volume", 80);
        volumeSeekBar.setProgress(alarmVolume);
        
        String selectedTone = sharedPreferences.getString("alarm_tone_name", "ডিফল্ট টোন");
        selectedToneText.setText(selectedTone);
        
        int snoozeDuration = sharedPreferences.getInt("snooze_duration", 5);
        snoozeDurationText.setText(snoozeDuration + " মিনিট");
        
        // Load app settings
        boolean isDarkMode = sharedPreferences.getBoolean("dark_mode", false);
        darkModeSwitch.setChecked(isDarkMode);
    }
    
    private void showAlarmToneSelector() {
        String[] toneOptions = {
            "ডিফল্ট টোন",
            "মৃদু এলার্ম",
            "জোরালো এলার্ম", 
            "ক্লাসিক বেল",
            "ডিজিটাল বিপ",
            "সিস্টেম রিংটোন নির্বাচন করুন"
        };
        
        int currentSelection = sharedPreferences.getInt("alarm_tone_index", 0);
        
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("এলার্ম টোন নির্বাচন করুন");
        builder.setSingleChoiceItems(toneOptions, currentSelection, (dialog, which) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("alarm_tone_index", which);
            editor.putString("alarm_tone_name", toneOptions[which]);
            editor.apply();
            
            selectedToneText.setText(toneOptions[which]);
            
            if (which == 5) { // System ringtone selection
                try {
                    Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM);
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "এলার্ম টোন নির্বাচন করুন");
                    startActivityForResult(intent, 100);
                } catch (Exception e) {
                    Toast.makeText(getContext(), "রিংটোন নির্বাচক খুলতে সমস্যা হয়েছে", Toast.LENGTH_SHORT).show();
                }
            }
            
            Toast.makeText(getContext(), "এলার্ম টোন পরিবর্তন করা হয়েছে", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
        
        builder.setNegativeButton("বাতিল", null);
        builder.show();
    }
    
    private void showSnoozeDurationSelector() {
        String[] durationOptions = {
            "১ মিনিট",
            "৩ মিনিট", 
            "৫ মিনিট",
            "১০ মিনিট",
            "১৫ মিনিট",
            "৩০ মিনিট"
        };
        
        int[] durationValues = {1, 3, 5, 10, 15, 30};
        int currentDuration = sharedPreferences.getInt("snooze_duration", 5);
        int currentSelection = 2; // Default 5 minutes
        
        // Find current selection index
        for (int i = 0; i < durationValues.length; i++) {
            if (durationValues[i] == currentDuration) {
                currentSelection = i;
                break;
            }
        }
        
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("স্নুজ সময় নির্বাচন করুন");
        builder.setSingleChoiceItems(durationOptions, currentSelection, (dialog, which) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("snooze_duration", durationValues[which]);
            editor.apply();
            
            snoozeDurationText.setText(durationOptions[which]);
            Toast.makeText(getContext(), "স্নুজ সময় পরিবর্তন করা হয়েছে", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
        
        builder.setNegativeButton("বাতিল", null);
        builder.show();
    }
    
    private void showDeveloperInfo() {
        Toast.makeText(getContext(), 
            "ডেভেলপার: মোঃ সজিব হোসেন\n" +
            "ইমেইল: dreamsofparents@gmail.com\n" +
            "অ্যাপ ভার্সন: 1.0.0\n" +
            "MediRemind - আপনার ওষুধের সঙ্গী", 
            Toast.LENGTH_LONG).show();
    }
}
