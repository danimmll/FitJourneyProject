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

import com.google.android.material.card.MaterialCardView;

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
 * <li>Nombre del ejercicio.</li>
 * <li>Grupo muscular.</li>
 * <li>Nivel de dificultad representado visualmente.</li>
 * <li>Estado (Borde rojo si está inactivo para los administradores).</li>
 * <li>Imagen del ejercicio (URL o Base64).</li>
 * <li>Navegación al detalle del ejercicio.</li>
 * </ul>
 */
public class ExerciseAdapter extends RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder> {

    private List<Exercise> exercises;
    private Context context;
    private final SecondViewModel viewModel;

    public ExerciseAdapter(Context context, ViewModel viewModel) {
        this.context = context;
        this.exercises = new ArrayList<>();
        this.viewModel = (SecondViewModel) viewModel;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setExercises(List<Exercise> exercises) {
        this.exercises = exercises;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ExerciseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context)
                .inflate(R.layout.viewholder_exercise, parent, false);
        return new ExerciseViewHolder(view);
    }

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

        boolean isInactive = exercise.getActive() != null && exercise.getActive() == 0;

        if (isInactive) {
            holder.cardExercise.setStrokeColor(Color.parseColor("#E52D27"));
            holder.cardExercise.setStrokeWidth(4);
            holder.tvName.setTextColor(Color.parseColor("#E52D27"));
        } else {
            holder.cardExercise.setStrokeColor(ContextCompat.getColor(context, R.color.stroke));
            holder.cardExercise.setStrokeWidth(2);
            holder.tvName.setTextColor(ContextCompat.getColor(context, R.color.text));
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

    @Override
    public int getItemCount() {
        return exercises != null ? exercises.size() : 0;
    }

    public static class ExerciseViewHolder extends RecyclerView.ViewHolder {

        MaterialCardView cardExercise;
        ImageView ivImage;
        TextView tvName;
        TextView tvMuscle;
        View diffLine1;
        View diffLine2;
        View diffLine3;

        public ExerciseViewHolder(@NonNull View itemView) {
            super(itemView);

            // Enlazamos el CardView raíz
            cardExercise = itemView.findViewById(R.id.cardExercise);

            ivImage = itemView.findViewById(R.id.ivExercise);
            tvName = itemView.findViewById(R.id.tvExerciseName);
            tvMuscle = itemView.findViewById(R.id.tvMuscleGroup);

            diffLine1 = itemView.findViewById(R.id.diffLine1);
            diffLine2 = itemView.findViewById(R.id.diffLine2);
            diffLine3 = itemView.findViewById(R.id.diffLine3);
        }
    }
}