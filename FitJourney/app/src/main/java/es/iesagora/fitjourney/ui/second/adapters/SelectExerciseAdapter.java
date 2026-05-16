package es.iesagora.fitjourney.ui.second.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import es.iesagora.fitjourney.R;
import es.iesagora.fitjourney.data.model.Exercise;
import es.iesagora.fitjourney.utils.ImageUtils;

/**
 * Adapter encargado de mostrar una lista de ejercicios seleccionables.
 *
 * <p>Se utiliza principalmente en pantallas donde el usuario puede añadir
 * ejercicios a una rutina o selección personalizada.</p>
 *
 * <p>Cada item permite seleccionar un ejercicio mediante un botón de acción.</p>
 */
public class SelectExerciseAdapter extends RecyclerView.Adapter<AddedExerciseAdapter.ViewHolder> {

    /**
     * Listener para detectar la selección de un ejercicio.
     */
    public interface OnExerciseClickListener {
        void onExerciseClick(Exercise exercise);
    }

    /**
     * Lista interna de ejercicios disponibles.
     */
    private final List<Exercise> exercises = new ArrayList<>();

    /**
     * Listener de eventos de selección.
     */
    private final OnExerciseClickListener listener;

    /**
     * Constructor del adapter.
     *
     * @param listener listener para eventos de selección de ejercicio
     */
    public SelectExerciseAdapter(OnExerciseClickListener listener) {
        this.listener = listener;
    }

    /**
     * Crea la vista de cada item del RecyclerView.
     *
     * @param parent ViewGroup padre
     * @param viewType tipo de vista
     * @return ViewHolder asociado al layout del item
     */
    @NonNull
    @Override
    public AddedExerciseAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.viewholder_added_exercise, parent, false);
        return new AddedExerciseAdapter.ViewHolder(view);
    }

    /**
     * Vincula los datos del ejercicio con la vista.
     *
     * @param holder ViewHolder del item
     * @param position posición del elemento en la lista
     */
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull AddedExerciseAdapter.ViewHolder holder, int position) {

        Exercise exercise = exercises.get(position);

        holder.tvName.setText(exercise.getName());

        ImageUtils.loadInto(
                holder.itemView.getContext(),
                exercise.getDisplayImage(),
                holder.ivExercise,
                R.drawable.ic_ejercicios
        );

        holder.tvDetails.setText(
                exercise.getMuscleGroup() + " · " + exercise.getDifficulty()
        );

        holder.btnRemove.setImageResource(android.R.drawable.ic_input_add);

        holder.btnRemove.setOnClickListener(v ->
                listener.onExerciseClick(exercise)
        );
    }

    /**
     * Devuelve el número total de ejercicios.
     *
     * @return tamaño de la lista
     */
    @Override
    public int getItemCount() {
        return exercises.size();
    }

    /**
     * Actualiza la lista de ejercicios mostrados.
     *
     * @param newItems nueva lista de ejercicios
     */
    @SuppressLint("NotifyDataSetChanged")
    public void submitList(List<Exercise> newItems) {
        exercises.clear();
        exercises.addAll(newItems);
        notifyDataSetChanged();
    }
}