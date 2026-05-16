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
 * Adapter encargado de mostrar la lista de ejercicios
 * dentro de un RecyclerView.
 * Este adapter se ocupa de:
 * - Mostrar nombre y grupo muscular
 * - Representar la dificultad visualmente
 * - Cargar imágenes desde URL o Base64
 * - Gestionar la navegación al detalle
 * - Comunicar el ejercicio seleccionado al ViewModel
 */
public class ExerciseAdapter
        extends RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder> {

    /**
     * Lista de ejercicios mostrados.
     */
    private List<Exercise> exercises;

    /**
     * Contexto utilizado para inflar layouts
     * y acceder a recursos.
     */
    private Context context;

    /**
     * ViewModel compartido utilizado para guardar
     * el ejercicio seleccionado.
     */
    private final SecondViewModel viewModel;

    /**
     * Constructor del adapter.
     *
     * @param context contexto de la aplicación.
     * @param viewModel ViewModel compartido.
     */
    public ExerciseAdapter(Context context, ViewModel viewModel) {

        this.context = context;
        this.exercises = new ArrayList<>();
        this.viewModel = (SecondViewModel) viewModel;
    }

    /**
     * Actualiza la lista de ejercicios mostrados.
     *
     * @param exercises nueva lista de ejercicios.
     */
    @SuppressLint("NotifyDataSetChanged")
    public void setExercises(List<Exercise> exercises) {

        this.exercises = exercises;
        notifyDataSetChanged();
    }

    /**
     * Crea un nuevo ViewHolder.
     *
     * @param parent contenedor padre.
     * @param viewType tipo de vista.
     * @return nuevo ExerciseViewHolder.
     */
    @NonNull
    @Override
    public ExerciseViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                 int viewType) {

        context = parent.getContext();

        View view = LayoutInflater.from(context)
                .inflate(R.layout.viewholder_exercise, parent, false);

        return new ExerciseViewHolder(view);
    }

    /**
     * Asocia los datos de un ejercicio concreto
     * con el ViewHolder correspondiente.
     *
     * @param holder holder actual.
     * @param position posición dentro de la lista.
     */
    @Override
    public void onBindViewHolder(@NonNull ExerciseViewHolder holder,
                                 int position) {

        Exercise exercise = exercises.get(position);

        holder.tvName.setText(exercise.getName());
        holder.tvMuscle.setText(exercise.getMuscleGroup());

        String diff = exercise.getDifficulty() != null
                ? exercise.getDifficulty().toLowerCase()
                : "";

        int colorActive =
                ContextCompat.getColor(context, R.color.mainrosa);

        int colorInactive =
                Color.parseColor("#AAAAAA");

        holder.diffLine1.setBackgroundColor(colorInactive);
        holder.diffLine2.setBackgroundColor(colorInactive);
        holder.diffLine3.setBackgroundColor(colorActive);

        /*
         * Configuración visual de la dificultad:
         *
         * Fácil -> 1 línea
         * Intermedio -> 2 líneas
         * Difícil -> 3 líneas
         */
        if (diff.contains("intermedio") || diff.equals("2")) {

            holder.diffLine2.setBackgroundColor(colorActive);
            holder.diffLine3.setBackgroundColor(colorActive);

        } else if (diff.contains("difícil")
                || diff.contains("dificil")
                || diff.contains("avanzado")
                || diff.equals("3")) {

            holder.diffLine1.setBackgroundColor(colorActive);
            holder.diffLine2.setBackgroundColor(colorActive);
            holder.diffLine3.setBackgroundColor(colorActive);
        }

        /*
         * Carga la imagen del ejercicio.
         * Puede ser URL remota o imagen Base64.
         */
        ImageUtils.loadInto(
                context,
                exercise.getDisplayImage(),
                holder.ivImage,
                R.drawable.ic_ejercicios
        );

        /*
         * Navega al detalle del ejercicio
         * y guarda el ejercicio seleccionado.
         */
        holder.itemView.setOnClickListener(v -> {

            if (viewModel != null) {
                viewModel.selectExercise(exercise);
            }

            Navigation.findNavController(v)
                    .navigate(R.id.exerciseDetailFragment);
        });
    }

    /**
     * Devuelve el número total de elementos.
     *
     * @return cantidad de ejercicios.
     */
    @Override
    public int getItemCount() {
        return exercises != null ? exercises.size() : 0;
    }

    /**
     * ViewHolder que representa un ejercicio individual.
     */
    public static class ExerciseViewHolder
            extends RecyclerView.ViewHolder {

        /**
         * Imagen principal del ejercicio.
         */
        ImageView ivImage;

        /**
         * Nombre del ejercicio.
         */
        TextView tvName;

        /**
         * Grupo muscular trabajado.
         */
        TextView tvMuscle;

        /**
         * Barras visuales de dificultad.
         */
        View diffLine1;
        View diffLine2;
        View diffLine3;

        /**
         * Constructor del ViewHolder.
         *
         * @param itemView vista raíz del item.
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