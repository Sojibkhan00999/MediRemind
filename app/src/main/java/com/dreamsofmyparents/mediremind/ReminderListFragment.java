package com.dreamsofmyparents.mediremind;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ReminderListFragment extends Fragment {
    private RecyclerView recyclerView;
    private ReminderAdapter adapter;
    private ReminderPreferenceManager reminderManager;
    private List<ReminderModel> reminderList;
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
        reminderList = reminderManager.getAllReminders();
        
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
            
            adapter = new ReminderAdapter(getContext(), reminderList, reminderManager, this);
            recyclerView.setAdapter(adapter);
        }
    }

    public void refreshList() {
        loadReminders();
    }

    public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ViewHolder> {
        private Context context;
        private List<ReminderModel> list;
        private ReminderPreferenceManager reminderManager;
        private ReminderListFragment fragment;

        public ReminderAdapter(Context context, List<ReminderModel> list, ReminderPreferenceManager reminderManager, ReminderListFragment fragment) {
            this.context = context;
            this.list = list;
            this.reminderManager = reminderManager;
            this.fragment = fragment;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.reminder_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ReminderModel model = list.get(position);
            
            // Set medicine name and type
            holder.name.setText(model.medicine + " (" + model.type + ")");
            
            // Set time and meal
            holder.time.setText(model.meal + " " + model.time);
            
            // Set dose
            holder.dose.setText("ডোজ: " + model.dose);
            
            // Set frequency
            holder.type.setText(model.frequency.equals("daily") ? "প্রতিদিন" : "একবার");

            // Delete button click listener
            holder.btnDelete.setOnClickListener(v -> {
                new AlertDialog.Builder(context)
                        .setTitle("রিমাইন্ডার মুছুন")
                        .setMessage("এই রিমাইন্ডারটি মুছে ফেলতে চান?")
                        .setPositiveButton("হ্যাঁ", (dialog, which) -> {
                            reminderManager.deleteReminder(model.id);
                            fragment.refreshList();
                            Toast.makeText(context, "রিমাইন্ডার মুছে ফেলা হয়েছে", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("না", null)
                        .show();
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView name, time, dose, type;
            ImageButton btnDelete;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.reminderName);
                time = itemView.findViewById(R.id.reminderTime);
                dose = itemView.findViewById(R.id.reminderDose);
                type = itemView.findViewById(R.id.reminderType);
                btnDelete = itemView.findViewById(R.id.btnDelete);
            }
        }
    }
}
