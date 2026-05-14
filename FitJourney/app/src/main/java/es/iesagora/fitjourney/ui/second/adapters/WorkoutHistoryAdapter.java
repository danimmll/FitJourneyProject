package es.iesagora.fitjourney.ui.second.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import es.iesagora.fitjourney.R;
import es.iesagora.fitjourney.data.model.Workout;

public class WorkoutHistoryAdapter extends RecyclerView.Adapter<WorkoutHistoryAdapter.ViewHolder> {

    private final List<Workout> workouts = new ArrayList<>();
    private final OnItemClickListener listener;
    private final OnDeleteClickListener deleteListener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy • HH:mm", Locale.getDefault());

    public interface OnItemClickListener {
        void onItemClick(Workout workout);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(Workout workout);
    }

    public WorkoutHistoryAdapter(OnItemClickListener listener, OnDeleteClickListener deleteListener) {
        this.listener = listener;
        this.deleteListener = deleteListener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setWorkouts(List<Workout> newWorkouts) {
        this.workouts.clear();
        this.workouts.addAll(newWorkouts);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_workout_history, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Workout workout = workouts.get(position);

        String routineName = workout.getRoutine() != null ? workout.getRoutine().getName() : "Entrenamiento Libre";
        holder.tvRoutineName.setText(routineName);

        if (workout.getWorkoutDate() != null) {
            holder.tvDate.setText(dateFormat.format(workout.getWorkoutDate()));
        } else {
            holder.tvDate.setText("Fecha desconocida");
        }

        int duration = workout.getDurationMinutes() != null ? workout.getDurationMinutes() : 0;
        holder.tvDuration.setText(duration + " min");

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(workout);
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) deleteListener.onDeleteClick(workout);
        });
    }

    @Override
    public int getItemCount() {
        return workouts.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRoutineName, tvDate, tvDuration;
        MaterialButton btnDelete;

        ViewHolder(View view) {
            super(view);
            tvRoutineName = view.findViewById(R.id.tvHistoryRoutineName);
            tvDate = view.findViewById(R.id.tvHistoryDate);
            tvDuration = view.findViewById(R.id.tvHistoryDuration);
            btnDelete = view.findViewById(R.id.btnDeleteWorkout);
        }
    }
}