package com.dreamsofmyparents.mediremind;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ViewHolder> {
    private final Context context;
    private final List<ReminderModel> list;
    private final ReminderPreferenceManager reminderManager;
    private final ReminderListFragment fragment;

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
        holder.btnDelete.setOnClickListener(v -> new AlertDialog.Builder(context).setTitle("রিমাইন্ডার মুছুন").setMessage("এই রিমাইন্ডারটি মুছে ফেলতে চান?").setPositiveButton("হ্যাঁ", (dialog, which) -> {
            reminderManager.deleteReminder(model.id);
            fragment.refreshList();
            Toast.makeText(context, "রিমাইন্ডার মুছে ফেলা হয়েছে", Toast.LENGTH_SHORT).show();
        }).setNegativeButton("না", null).show());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
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