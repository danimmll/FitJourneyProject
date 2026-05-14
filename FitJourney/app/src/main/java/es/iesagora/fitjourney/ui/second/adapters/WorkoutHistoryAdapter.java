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

public class WorkoutHistoryAdapter extends RecyclerView.Adapter<WorkoutHistoryAdapter.ViewHolder> {

    private List<Workout> workouts = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

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

        if (workout.getUser() != null && workout.getUser().getName() != null) {
            routineName += " (" + workout.getUser().getName() + ")";
        }

        holder.tvName.setText(routineName);
        holder.tvDuration.setText(workout.getDurationMinutes() + " min");

        if (workout.getWorkoutDate() != null) {
            holder.tvDate.setText(dateFormat.format(workout.getWorkoutDate()));
        }
    }

    @Override
    public int getItemCount() {
        return workouts.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvDate;
        TextView tvDuration;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvHistoryRoutineName);
            tvDate = itemView.findViewById(R.id.tvHistoryDate);
            tvDuration = itemView.findViewById(R.id.tvHistoryDuration);
        }
    }
}