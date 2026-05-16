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

/**
 * Adapter encargado de mostrar el historial de entrenamientos del usuario.
 *
 * <p>Cada elemento representa un workout realizado, mostrando información como:</p>
 * <ul>
 *     <li>Nombre de la rutina asociada (o entrenamiento libre)</li>
 *     <li>Fecha del entrenamiento</li>
 *     <li>Duración y calorías quemadas</li>
 * </ul>
 *
 * <p>Permite:</p>
 * <ul>
 *     <li>Abrir el detalle del entrenamiento</li>
 *     <li>Eliminar un entrenamiento del historial</li>
 * </ul>
 */
public class WorkoutHistoryAdapter extends RecyclerView.Adapter<WorkoutHistoryAdapter.ViewHolder> {

    /**
     * Lista de entrenamientos a mostrar.
     */
    private final List<Workout> workouts = new ArrayList<>();

    /**
     * Listener para click en el item.
     */
    private final OnItemClickListener listener;

    /**
     * Listener para eliminación de workouts.
     */
    private final OnDeleteClickListener deleteListener;

    /**
     * Formateador de fecha para mostrar el historial.
     */
    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("dd MMM yyyy • HH:mm", Locale.getDefault());

    /**
     * Listener para clicks en un workout.
     */
    public interface OnItemClickListener {
        void onItemClick(Workout workout);
    }

    /**
     * Listener para eliminar un workout.
     */
    public interface OnDeleteClickListener {
        void onDeleteClick(Workout workout);
    }

    /**
     * Constructor del adapter.
     *
     * @param listener listener para abrir detalle
     * @param deleteListener listener para eliminar workout
     */
    public WorkoutHistoryAdapter(OnItemClickListener listener, OnDeleteClickListener deleteListener) {
        this.listener = listener;
        this.deleteListener = deleteListener;
    }

    /**
     * Actualiza la lista de entrenamientos.
     *
     * @param newWorkouts nueva lista de workouts
     */
    @SuppressLint("NotifyDataSetChanged")
    public void setWorkouts(List<Workout> newWorkouts) {
        this.workouts.clear();
        this.workouts.addAll(newWorkouts);
        notifyDataSetChanged();
    }

    /**
     * Crea el ViewHolder del item.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.viewholder_workout_history, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Vincula los datos del workout con la vista.
     */
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Workout workout = workouts.get(position);

        String routineName = workout.getRoutine() != null
                ? workout.getRoutine().getName()
                : "Entrenamiento Libre";

        holder.tvRoutineName.setText(routineName);

        if (workout.getWorkoutDate() != null) {
            holder.tvDate.setText(dateFormat.format(workout.getWorkoutDate()));
        } else {
            holder.tvDate.setText("Fecha desconocida");
        }

        int duration = workout.getDurationMinutes() != null
                ? workout.getDurationMinutes()
                : 0;

        double cals = workout.getCaloriesBurned() != null
                ? workout.getCaloriesBurned()
                : 0.0;

        holder.tvDuration.setText(duration + " min • " + Math.round(cals) + " kcal");

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(workout);
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) deleteListener.onDeleteClick(workout);
        });
    }

    /**
     * Devuelve el número de elementos del RecyclerView.
     */
    @Override
    public int getItemCount() {
        return workouts.size();
    }

    /**
     * ViewHolder del historial de entrenamientos.
     */
    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvRoutineName;
        TextView tvDate;
        TextView tvDuration;
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