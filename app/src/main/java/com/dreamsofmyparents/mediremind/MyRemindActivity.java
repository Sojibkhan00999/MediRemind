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
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MyRemindActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ReminderPreferenceManager reminderManager;
    private TextView emptyStateText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_remind);

        recyclerView = findViewById(R.id.recyclerView);
        emptyStateText = findViewById(R.id.emptyStateText);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        reminderManager = ReminderPreferenceManager.getInstance(this);

        loadReminders();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadReminders(); // Refresh data when activity becomes visible
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

            ReminderAdapter adapter = new ReminderAdapter(this, reminderList);
            recyclerView.setAdapter(adapter);
        }
    }

    public void refreshList() {
        loadReminders();
    }

    public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder> {
        private final Context context;
        private final List<ReminderModel> reminderList;

        public ReminderAdapter(Context context, List<ReminderModel> reminderList) {
            this.context = context;
            this.reminderList = reminderList;
        }

        @NonNull
        @Override
        public ReminderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.my_reminder_item, parent, false);
            return new ReminderViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ReminderViewHolder holder, int position) {
            ReminderModel reminder = reminderList.get(position);

            // Set medicine name and type
            holder.name.setText(reminder.medicine + " (" + reminder.type + ")");

            // Set dose
            holder.dose.setText("ডোজ: " + reminder.dose);

            // Set meal and time
            holder.meal.setText(reminder.meal);
            holder.time.setText(reminder.time);

            // Set frequency
            holder.type.setText(reminder.frequency.equals("daily") ? "প্রতিদিন" : "একবার");

            holder.btnDelete.setOnClickListener(v -> new AlertDialog.Builder(context).setTitle("রিমাইন্ডার মুছুন").setMessage("এই রিমাইন্ডারটি মুছে ফেলতে চান?").setPositiveButton("হ্যাঁ", (dialog, which) -> {
                reminderManager.deleteReminder(reminder.id);
                ((MyRemindActivity) context).refreshList();
                Toast.makeText(context, "রিমাইন্ডার মুছে ফেলা হয়েছে", Toast.LENGTH_SHORT).show();
            }).setNegativeButton("না", null).show());
        }

        @Override
        public int getItemCount() {
            return reminderList.size();
        }

        public class ReminderViewHolder extends RecyclerView.ViewHolder {
            final TextView name;
            final TextView dose;
            final TextView time;
            final TextView type;
            final TextView meal;
            final ImageButton btnDelete;

            public ReminderViewHolder(@NonNull View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.reminderName);
                dose = itemView.findViewById(R.id.reminderDose);
                meal = itemView.findViewById(R.id.reminderMeal);
                time = itemView.findViewById(R.id.reminderTime);
                type = itemView.findViewById(R.id.reminderType);
                btnDelete = itemView.findViewById(R.id.btnDelete);
            }
        }
    }
}
