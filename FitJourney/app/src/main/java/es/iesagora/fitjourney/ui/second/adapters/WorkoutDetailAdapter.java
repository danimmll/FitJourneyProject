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

/**
 * Adapter encargado de mostrar los detalles de un entrenamiento (Workout)
 * en un RecyclerView.
 *
 * <p>Cada item representa un ejercicio realizado dentro del entrenamiento,
 * mostrando su nombre y las estadísticas asociadas según el tipo de ejercicio.</p>
 *
 * <p>Soporta tres tipos de ejercicios:</p>
 * <ul>
 *     <li>Hipertrofia (fuerza)</li>
 *     <li>Cardio</li>
 *     <li>Estiramiento</li>
 * </ul>
 */
public class WorkoutDetailAdapter extends RecyclerView.Adapter<WorkoutDetailAdapter.ViewHolder> {

    private static final String TYPE_HIPERTROFIA = "Hipertrofia";
    private static final String TYPE_CARDIO = "Cardio";
    private static final String TYPE_ESTIRAMIENTO = "Estiramiento";

    private final List<WorkoutDetail> details;

    /**
     * Constructor del adapter.
     *
     * @param details lista de detalles del entrenamiento a mostrar
     */
    public WorkoutDetailAdapter(List<WorkoutDetail> details) {
        this.details = details;
    }

    /**
     * Crea la vista de cada item del RecyclerView.
     *
     * @param parent ViewGroup padre
     * @param viewType tipo de vista
     * @return ViewHolder con la vista inflada
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.viewholder_workout_detail_item, parent, false);

        return new ViewHolder(view);
    }

    /**
     * Asocia los datos del WorkoutDetail con la vista.
     *
     * @param holder ViewHolder
     * @param position posición del elemento en la lista
     */
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        WorkoutDetail detail = details.get(position);

        holder.tvName.setText(getExerciseName(detail));
        holder.tvStats.setText(buildStatisticsText(detail));
    }

    /**
     * Devuelve el número total de elementos.
     *
     * @return tamaño de la lista de detalles
     */
    @Override
    public int getItemCount() {
        return details != null ? details.size() : 0;
    }

    /**
     * Obtiene el nombre del ejercicio asociado al detalle.
     *
     * @param detail detalle del workout
     * @return nombre del ejercicio o valor por defecto si no existe
     */
    private String getExerciseName(WorkoutDetail detail) {
        if (detail.getExercise() != null
                && detail.getExercise().getName() != null) {
            return detail.getExercise().getName();
        }

        return "Ejercicio Desconocido";
    }

    /**
     * Construye el texto de estadísticas según el tipo de ejercicio.
     *
     * @param detail detalle del workout
     * @return texto formateado con estadísticas
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
     * Obtiene el tipo de ejercicio.
     *
     * @param detail detalle del workout
     * @return tipo de ejercicio o hipertrofia por defecto
     */
    private String getExerciseType(WorkoutDetail detail) {

        if (detail.getExercise() != null
                && detail.getExercise().getExerciseType() != null) {
            return detail.getExercise().getExerciseType();
        }

        return TYPE_HIPERTROFIA;
    }

    /**
     * Genera estadísticas para ejercicios de cardio.
     *
     * @param detail detalle del workout
     * @return texto formateado
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
     * Genera estadísticas para ejercicios de estiramiento.
     *
     * @param detail detalle del workout
     * @return texto formateado
     */
    private String buildStretchingStats(WorkoutDetail detail) {
        return getSets(detail) + " rondas completadas";
    }

    /**
     * Genera estadísticas para ejercicios de fuerza/hipertrofia.
     *
     * @param detail detalle del workout
     * @return texto formateado
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
            return sets + " series x (" + reps + ") reps • "
                    + weight + " kg máx";
        }

        return sets + " series x (" + reps + ") reps";
    }

    /**
     * Obtiene el número de series realizadas.
     *
     * @param detail detalle del workout
     * @return número de sets o 0 si es null
     */
    private int getSets(WorkoutDetail detail) {
        return detail.getSetsPerformed() != null
                ? detail.getSetsPerformed()
                : 0;
    }

    /**
     * ViewHolder que representa cada item del RecyclerView.
     */
    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvName;
        TextView tvStats;

        /**
         * Constructor del ViewHolder.
         *
         * @param itemView vista del item
         */
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvName = itemView.findViewById(R.id.tvExerciseName);
            tvStats = itemView.findViewById(R.id.tvStats);
        }
    }
}