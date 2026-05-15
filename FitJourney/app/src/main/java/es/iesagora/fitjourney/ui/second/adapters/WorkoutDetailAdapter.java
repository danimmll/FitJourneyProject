package es.iesagora.fitjourney.ui.second.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import es.iesagora.fitjourney.R;
import es.iesagora.fitjourney.data.model.WorkoutDetail;

public class WorkoutDetailAdapter extends RecyclerView.Adapter<WorkoutDetailAdapter.ViewHolder> {

    private static final String TYPE_HIPERTROFIA = "Hipertrofia";
    private static final String TYPE_CARDIO = "Cardio";
    private static final String TYPE_ESTIRAMIENTO = "Estiramiento";

    private final List<WorkoutDetail> details;

    public WorkoutDetailAdapter(List<WorkoutDetail> details) {
        this.details = details;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.viewholder_workout_detail_item, parent, false);

        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WorkoutDetail detail = details.get(position);

        holder.tvName.setText(getExerciseName(detail));
        holder.tvStats.setText(buildStatisticsText(detail));
    }

    @Override
    public int getItemCount() {
        return details != null ? details.size() : 0;
    }

    /**
     * Devuelve el nombre del ejercicio.
     * Si no existe información del ejercicio,
     * se muestra un texto por defecto.
     */
    private String getExerciseName(WorkoutDetail detail) {
        if (detail.getExercise() != null
                && detail.getExercise().getName() != null) {

            return detail.getExercise().getName();
        }

        return "Ejercicio Desconocido";
    }

    /**
     * Construye el texto de estadísticas dependiendo
     * del tipo de ejercicio realizado.
     */
    private String buildStatisticsText(WorkoutDetail detail) {

        String type = getExerciseType(detail);

        if (type.equalsIgnoreCase(TYPE_CARDIO)) {
            return buildCardioStats(detail);
        }

        if (type.equalsIgnoreCase(TYPE_ESTIRAMIENTO)) {
            return buildStretchingStats(detail);
        }

        return buildStrengthStats(detail);
    }

    /**
     * Devuelve el tipo de ejercicio.
     * Por defecto será Hipertrofia.
     */
    private String getExerciseType(WorkoutDetail detail) {

        if (detail.getExercise() != null
                && detail.getExercise().getExerciseType() != null) {

            return detail.getExercise().getExerciseType();
        }

        return TYPE_HIPERTROFIA;
    }

    /**
     * Genera las estadísticas para ejercicios de cardio.
     */
    private String buildCardioStats(WorkoutDetail detail) {

        int sets = getSets(detail);

        double mins = detail.getDurationMinutes() != null
                ? detail.getDurationMinutes()
                : 0.0;

        double speed = detail.getSpeedKmh() != null
                ? detail.getSpeedKmh()
                : 0.0;

        return sets + " series • "
                + mins + " min • "
                + speed + " km/h";
    }

    /**
     * Genera las estadísticas para ejercicios de estiramiento.
     */
    private String buildStretchingStats(WorkoutDetail detail) {
        return getSets(detail) + " rondas completadas";
    }

    /**
     * Genera las estadísticas para ejercicios de hipertrofia.
     */
    private String buildStrengthStats(WorkoutDetail detail) {

        int sets = getSets(detail);

        String reps = detail.getRepsString() != null
                ? detail.getRepsString()
                : "0";

        double weight = detail.getMaxWeight() != null
                ? detail.getMaxWeight()
                : 0.0;

        if (weight > 0) {
            return sets + " series x ("
                    + reps + ") reps • "
                    + weight + " kg máx";
        }

        return sets + " series x ("
                + reps + ") reps";
    }

    /**
     * Devuelve el número de series realizadas.
     */
    private int getSets(WorkoutDetail detail) {
        return detail.getSetsPerformed() != null
                ? detail.getSetsPerformed()
                : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvName;
        TextView tvStats;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvName = itemView.findViewById(R.id.tvExerciseName);
            tvStats = itemView.findViewById(R.id.tvStats);
        }
    }
}