package es.iesagora.fitjourney.ui.second.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModel;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import es.iesagora.fitjourney.R;
import es.iesagora.fitjourney.data.model.Exercise;
import es.iesagora.fitjourney.utils.ImageUtils;
import es.iesagora.fitjourney.viewmodel.SecondViewModel;

/**
 * Adapter encargado de renderizar la lista de ejercicios en un RecyclerView.
 *
 * <p>Este adapter gestiona la visualización de cada ejercicio, incluyendo:</p>
 * <ul>
 *     <li>Nombre del ejercicio.</li>
 *     <li>Grupo muscular.</li>
 *     <li>Nivel de dificultad representado visualmente.</li>
 *     <li>Imagen del ejercicio (URL o Base64).</li>
 *     <li>Navegación al detalle del ejercicio.</li>
 * </ul>
 *
 * <p>También permite seleccionar un ejercicio y guardarlo en el ViewModel compartido.</p>
 */
public class ExerciseAdapter extends RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder> {

    /** Lista de ejercicios a mostrar */
    private List<Exercise> exercises;

    /** Contexto de la UI */
    private Context context;

    /** ViewModel compartido para seleccionar ejercicios */
    private final SecondViewModel viewModel;

    /**
     * Constructor del adapter.
     *
     * @param context contexto de la aplicación
     * @param viewModel ViewModel compartido para comunicación con el detalle
     */
    public ExerciseAdapter(Context context, ViewModel viewModel) {
        this.context = context;
        this.exercises = new ArrayList<>();
        this.viewModel = (SecondViewModel) viewModel;
    }

    /**
     * Actualiza la lista de ejercicios mostrados.
     *
     * @param exercises nueva lista de ejercicios
     */
    @SuppressLint("NotifyDataSetChanged")
    public void setExercises(List<Exercise> exercises) {
        this.exercises = exercises;
        notifyDataSetChanged();
    }

    /**
     * Infla el layout de cada item del RecyclerView.
     *
     * @param parent contenedor padre
     * @param viewType tipo de vista
     * @return ViewHolder del ejercicio
     */
    @NonNull
    @Override
    public ExerciseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context)
                .inflate(R.layout.viewholder_exercise, parent, false);
        return new ExerciseViewHolder(view);
    }

    /**
     * Vincula los datos del ejercicio con la vista.
     *
     * <p>Se encarga de:</p>
     * <ul>
     *     <li>Mostrar nombre y grupo muscular.</li>
     *     <li>Representar la dificultad con barras visuales.</li>
     *     <li>Cargar imagen (URL o Base64).</li>
     *     <li>Gestionar navegación al detalle.</li>
     * </ul>
     *
     * @param holder ViewHolder del item
     * @param position posición del elemento
     */
    @Override
    public void onBindViewHolder(@NonNull ExerciseViewHolder holder, int position) {

        Exercise exercise = exercises.get(position);

        holder.tvName.setText(exercise.getName());
        holder.tvMuscle.setText(exercise.getMuscleGroup());

        String diff = exercise.getDifficulty() != null
                ? exercise.getDifficulty().toLowerCase()
                : "";

        int colorActive = ContextCompat.getColor(context, R.color.mainrosa);
        int colorInactive = Color.parseColor("#AAAAAA");

        holder.diffLine1.setBackgroundColor(colorInactive);
        holder.diffLine2.setBackgroundColor(colorInactive);
        holder.diffLine3.setBackgroundColor(colorActive);

        if (diff.contains("intermedio") || diff.equals("2")) {
            holder.diffLine2.setBackgroundColor(colorActive);
            holder.diffLine3.setBackgroundColor(colorActive);

        } else if (diff.contains("difícil") || diff.contains("dificil")
                || diff.contains("avanzado") || diff.equals("3")) {
            holder.diffLine1.setBackgroundColor(colorActive);
            holder.diffLine2.setBackgroundColor(colorActive);
            holder.diffLine3.setBackgroundColor(colorActive);
        }

        ImageUtils.loadInto(
                context,
                exercise.getImageUrl(),
                holder.ivImage,
                R.drawable.ic_ejercicios
        );

        holder.itemView.setOnClickListener(v -> {
            if (viewModel != null) {
                viewModel.selectExercise(exercise);
            }
            Navigation.findNavController(v).navigate(R.id.exerciseDetailFragment);
        });
    }

    /**
     * Devuelve el número total de ejercicios en la lista.
     *
     * @return tamaño de la lista
     */
    @Override
    public int getItemCount() {
        return exercises != null ? exercises.size() : 0;
    }

    /**
     * ViewHolder que representa cada tarjeta de ejercicio.
     */
    public static class ExerciseViewHolder extends RecyclerView.ViewHolder {

        ImageView ivImage;
        TextView tvName;
        TextView tvMuscle;
        View diffLine1;
        View diffLine2;
        View diffLine3;

        /**
         * Inicializa las vistas del item.
         *
         * @param itemView vista inflada del ejercicio
         */
        public ExerciseViewHolder(@NonNull View itemView) {
            super(itemView);

            ivImage = itemView.findViewById(R.id.ivExercise);
            tvName = itemView.findViewById(R.id.tvExerciseName);
            tvMuscle = itemView.findViewById(R.id.tvMuscleGroup);

            diffLine1 = itemView.findViewById(R.id.diffLine1);
            diffLine2 = itemView.findViewById(R.id.diffLine2);
            diffLine3 = itemView.findViewById(R.id.diffLine3);
        }
    }
}