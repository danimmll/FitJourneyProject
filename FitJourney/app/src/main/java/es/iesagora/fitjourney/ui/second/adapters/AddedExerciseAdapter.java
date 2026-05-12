package es.iesagora.fitjourney.ui.second.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import es.iesagora.fitjourney.R;
import es.iesagora.fitjourney.data.model.RoutineExercise;
import es.iesagora.fitjourney.utils.ImageUtils;

/**
 * Adapter encargado de mostrar los ejercicios añadidos a una rutina.
 *
 * <p>Cada elemento representa un ejercicio configurado dentro de la rutina,
 * incluyendo:</p>
 * <ul>
 *     <li>Imagen del ejercicio.</li>
 *     <li>Nombre del ejercicio.</li>
 *     <li>Series y repeticiones objetivo.</li>
 *     <li>Botón para eliminar el ejercicio de la rutina.</li>
 * </ul>
 *
 * <p>Se utiliza normalmente durante la creación o edición de rutinas.</p>
 */
public class AddedExerciseAdapter
        extends RecyclerView.Adapter<AddedExerciseAdapter.ViewHolder> {

    /** Lista de ejercicios añadidos a la rutina */
    private final List<RoutineExercise> exercises;

    /** Listener para eliminar ejercicios */
    private final OnRemoveClickListener listener;

    /** Contexto de la UI */
    private Context context;

    /**
     * Interfaz utilizada para notificar la eliminación de un ejercicio.
     */
    public interface OnRemoveClickListener {

        /**
         * Se ejecuta cuando el usuario pulsa el botón eliminar.
         *
         * @param position posición del ejercicio en la lista
         */
        void onRemoveClick(int position);
    }

    /**
     * Constructor del adapter.
     *
     * @param exercises lista de ejercicios de la rutina
     * @param listener listener para manejar eliminaciones
     */
    public AddedExerciseAdapter(List<RoutineExercise> exercises,
                                OnRemoveClickListener listener) {
        this.exercises = exercises;
        this.listener = listener;
    }

    /**
     * Infla el layout de cada ejercicio añadido.
     *
     * @param parent contenedor padre
     * @param viewType tipo de vista
     * @return ViewHolder del ejercicio
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        context = parent.getContext();

        View view = LayoutInflater.from(context)
                .inflate(R.layout.viewholder_added_exercise, parent, false);

        return new ViewHolder(view);
    }

    /**
     * Vincula los datos del ejercicio con la interfaz.
     *
     * <p>Se encarga de:</p>
     * <ul>
     *     <li>Mostrar nombre del ejercicio.</li>
     *     <li>Cargar imagen del ejercicio.</li>
     *     <li>Mostrar series y repeticiones.</li>
     *     <li>Gestionar eliminación del ejercicio.</li>
     * </ul>
     *
     * @param holder ViewHolder del item
     * @param position posición actual
     */
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        RoutineExercise routineExercise = exercises.get(position);

        if (routineExercise.getExercise() != null) {

            holder.tvName.setText(
                    routineExercise.getExercise().getName()
            );

            ImageUtils.loadInto(
                    context,
                    routineExercise.getExercise().getImageUrl(),
                    holder.ivExercise,
                    R.drawable.ic_ejercicios
            );
        }

        holder.tvDetails.setText(
                routineExercise.getTargetSets()
                        + " series x "
                        + routineExercise.getTargetReps()
        );

        holder.btnRemove.setOnClickListener(
                v -> listener.onRemoveClick(position)
        );
    }

    /**
     * Devuelve el número total de ejercicios añadidos.
     *
     * @return tamaño de la lista
     */
    @Override
    public int getItemCount() {
        return exercises.size();
    }

    /**
     * ViewHolder encargado de representar un ejercicio dentro de la rutina.
     */
    static class ViewHolder extends RecyclerView.ViewHolder {

        /** Imagen del ejercicio */
        ImageView ivExercise;

        /** Botón para eliminar el ejercicio */
        ImageButton btnRemove;

        /** Nombre del ejercicio */
        TextView tvName;

        /** Información de series y repeticiones */
        TextView tvDetails;

        /**
         * Inicializa las vistas del item.
         *
         * @param itemView vista inflada del ejercicio
         */
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            ivExercise = itemView.findViewById(R.id.ivExercise);
            btnRemove = itemView.findViewById(R.id.btnRemove);
            tvName = itemView.findViewById(R.id.tvName);
            tvDetails = itemView.findViewById(R.id.tvDetails);
        }
    }
}