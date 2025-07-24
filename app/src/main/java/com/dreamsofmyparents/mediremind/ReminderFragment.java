package com.dreamsofmyparents.mediremind;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;
import java.util.Locale;

public class ReminderFragment extends Fragment {

    private TextInputEditText etMedicineName, etDose, etTime;
    private RadioGroup rgMealTime, rgFrequency;
    private Button btnSelectTime, btnSaveReminder, btnCancel;
    private ReminderPreferenceManager reminderManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reminder, container, false);
        
        initViews(view);
        setupListeners();
        
        return view;
    }

    private void initViews(View view) {
        etMedicineName = view.findViewById(R.id.etMedicineName);
        etDose = view.findViewById(R.id.etDose);
        etTime = view.findViewById(R.id.etTime);
        
        rgMealTime = view.findViewById(R.id.rgMealTime);
        rgFrequency = view.findViewById(R.id.rgFrequency);
        
        btnSelectTime = view.findViewById(R.id.btnSelectTime);
        btnSaveReminder = view.findViewById(R.id.btnSaveReminder);
        btnCancel = view.findViewById(R.id.btnCancel);
        
        reminderManager = ReminderPreferenceManager.getInstance(requireContext());
    }

    private void setupListeners() {
        btnSelectTime.setOnClickListener(v -> showTimePicker());
        etTime.setOnClickListener(v -> showTimePicker());
        
        btnSaveReminder.setOnClickListener(v -> saveReminder());
        btnCancel.setOnClickListener(v -> clearForm());
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
            requireContext(),
            (view, selectedHour, selectedMinute) -> {
                String time = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute);
                etTime.setText(time);
            },
            hour, minute, true
        );
        timePickerDialog.show();
    }

    private void saveReminder() {
        String medicineName = etMedicineName.getText().toString().trim();
        String dose = etDose.getText().toString().trim();
        String time = etTime.getText().toString().trim();

        if (medicineName.isEmpty()) {
            etMedicineName.setError("ওষুধের নাম লিখুন");
            return;
        }

        if (dose.isEmpty()) {
            etDose.setError("ডোজ লিখুন");
            return;
        }

        if (time.isEmpty()) {
            etTime.setError("সময় নির্বাচন করুন");
            return;
        }

        // Get meal time
        String mealTime = "খাবার আগে"; // default
        int selectedMealId = rgMealTime.getCheckedRadioButtonId();
        if (selectedMealId == R.id.rbAfterMeal) {
            mealTime = "খাবার পরে";
        }

        // Get frequency
        String frequency = "daily"; // default
        int selectedFrequencyId = rgFrequency.getCheckedRadioButtonId();
        if (selectedFrequencyId == R.id.rbOnce) {
            frequency = "once";
        }

        // Create reminder model
        ReminderModel reminder = new ReminderModel(0, medicineName, dose, time, frequency, mealTime);

        // Save reminder
        reminderManager.addReminder(reminder);

        Toast.makeText(requireContext(), "রিমাইন্ডার সংরক্ষিত হয়েছে!", Toast.LENGTH_SHORT).show();
        clearForm();
    }

    private void clearForm() {
        etMedicineName.setText("");
        etDose.setText("");
        etTime.setText("");
        rgMealTime.check(R.id.rbBeforeMeal);
        rgFrequency.check(R.id.rbDaily);
    }
}
