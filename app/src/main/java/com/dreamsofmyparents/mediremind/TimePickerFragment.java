package com.dreamsofmyparents.mediremind;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class TimePickerFragment extends DialogFragment {
    private final TimePickerDialog.OnTimeSetListener listener;
    private final int hour;
    private final int minute;

    public TimePickerFragment(TimePickerDialog.OnTimeSetListener listener, int hour, int minute) {
        this.listener = listener;
        this.hour = hour;
        this.minute = minute;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return new TimePickerDialog(getActivity(), listener, hour, minute, DateFormat.is24HourFormat(getActivity()));
    }
}