package com.johna_sarat_bayanb.fittrack.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.johna_sarat_bayanb.fittrack.R;
import com.johna_sarat_bayanb.fittrack.models.Workout;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WorkoutAdapter extends RecyclerView.Adapter<WorkoutAdapter.ViewHolder> {

    private List<Workout> list;

    public WorkoutAdapter(List<Workout> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_workout, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Workout w = list.get(position);

        try {
            SimpleDateFormat inputFormat =
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

            SimpleDateFormat outputFormat =
                    new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());

            Date date = inputFormat.parse(w.getDate());

            if (date != null) {
                holder.tvDate.setText(outputFormat.format(date));
            } else {
                holder.tvDate.setText(w.getDate());
            }

        } catch (Exception e) {
            holder.tvDate.setText(w.getDate());
        }

        int minutes = w.getDuration() / 60;
        int seconds = w.getDuration() % 60;

        holder.tvDetails.setText("Reps: " + w.getReps() + " | Time: " + String.format("%02d:%02d", minutes, seconds));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvDate, tvDetails;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvDate = itemView.findViewById(R.id.tvDate);
            tvDetails = itemView.findViewById(R.id.tvDetails);
        }
    }
}
