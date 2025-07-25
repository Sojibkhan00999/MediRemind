package com.dreamsofmyparents.mediremind;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ReminderListFragment extends Fragment {
    private RecyclerView recyclerView;
    private ReminderPreferenceManager reminderManager;
    private TextView emptyStateText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reminder_list, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        emptyStateText = view.findViewById(R.id.emptyStateText);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        reminderManager = ReminderPreferenceManager.getInstance(getContext());

        loadReminders();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadReminders(); // Refresh data when fragment becomes visible
    }

    private void loadReminders() {
        List<ReminderModel> reminderList = reminderManager.getAllReminders();

        if (reminderList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            if (emptyStateText != null) {
                emptyStateText.setVisibility(View.VISIBLE);
                emptyStateText.setText("কোনো রিমাইন্ডার নেই\nনতুন রিমাইন্ডার যোগ করুন");
            }
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            if (emptyStateText != null) {
                emptyStateText.setVisibility(View.GONE);
            }

            ReminderAdapter adapter = new ReminderAdapter(getContext(), reminderList, reminderManager, this);
            recyclerView.setAdapter(adapter);
        }
    }

    public void refreshList() {
        loadReminders();
    }

}
