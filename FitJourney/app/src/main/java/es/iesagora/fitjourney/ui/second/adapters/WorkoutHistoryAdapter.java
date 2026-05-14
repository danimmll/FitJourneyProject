package es.iesagora.fitjourney.ui.second.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import es.iesagora.fitjourney.R;
import es.iesagora.fitjourney.data.model.Workout;
import es.iesagora.fitjourney.utils.FitnessCalculator;

public class WorkoutHistoryAdapter extends RecyclerView.Adapter<WorkoutHistoryAdapter.ViewHolder> {

    private List<Workout> workouts = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
    private final OnWorkoutClickListener listener;

    public interface OnWorkoutClickListener {
        void onWorkoutClick(Workout workout);
    }

    public WorkoutHistoryAdapter(OnWorkoutClickListener listener) {
        this.listener = listener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setWorkouts(List<Workout> workouts) {
        this.workouts = workouts;
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

        String routineName = (workout.getRoutine() != null) ? workout.getRoutine().getName() : "Entrenamiento Libre";

        double weight = 70.0;
        if (workout.getUser() != null && workout.getUser().getWeight() != null) {
            weight = workout.getUser().getWeight();
            routineName += " (" + workout.getUser().getName() + ")";
        }

        holder.tvName.setText(routineName);

        int duration = workout.getDurationMinutes() != null ? workout.getDurationMinutes() : 0;
        holder.tvDuration.setText(duration + " min");

        double calories = FitnessCalculator.calculateCaloriesBurned(6.0F, (float) weight,duration);
        holder.tvCalories.setText((int)calories + " kcal");

        if (workout.getWorkoutDate() != null) {
            holder.tvDate.setText(dateFormat.format(workout.getWorkoutDate()));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onWorkoutClick(workout);
        });
    }

    @Override
    public int getItemCount() {
        return workouts != null ? workouts.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvDate;
        TextView tvDuration;
        TextView tvCalories;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvHistoryRoutineName);
            tvDate = itemView.findViewById(R.id.tvHistoryDate);
            tvDuration = itemView.findViewById(R.id.tvHistoryDuration);
            tvCalories = itemView.findViewById(R.id.tvHistoryCalories);
        }
    }
}