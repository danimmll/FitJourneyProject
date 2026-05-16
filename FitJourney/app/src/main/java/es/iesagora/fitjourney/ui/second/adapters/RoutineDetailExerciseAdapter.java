package es.iesagora.fitjourney.ui.second.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import es.iesagora.fitjourney.R;
import es.iesagora.fitjourney.data.model.Exercise;
import es.iesagora.fitjourney.data.model.RoutineExercise;
import es.iesagora.fitjourney.utils.ImageUtils;

public class RoutineDetailExerciseAdapter extends RecyclerView.Adapter<RoutineDetailExerciseAdapter.ViewHolder> {

    private final List<RoutineExercise> exercises;
    private final OnExerciseClickListener listener;

    public interface OnExerciseClickListener {
        void onExerciseClick(Exercise exercise);
    }

    public RoutineDetailExerciseAdapter(List<RoutineExercise> exercises,
                                        OnExerciseClickListener listener) {
        this.exercises = exercises;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.viewholder_routine_detail_exercise, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        RoutineExercise routineExercise = exercises.get(position);
        Exercise exercise = routineExercise.getExercise();

        if (exercise == null) return;

        holder.tvName.setText(exercise.getName());

        String sets = routineExercise.getTargetSets() != null
                ? String.valueOf(routineExercise.getTargetSets())
                : "0";

        String reps = routineExercise.getTargetReps() != null
                ? String.valueOf(routineExercise.getTargetReps())
                : "0";

        holder.tvTarget.setText("Objetivo: " + sets + "x" + reps);

        ImageUtils.loadInto(
                holder.itemView.getContext(),
                exercise.getDisplayImage(),
                holder.ivImage,
                R.drawable.ic_launcher_background
        );

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onExerciseClick(exercise);
            }
        });
    }

    @Override
    public int getItemCount() {
        return exercises != null ? exercises.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvName;
        TextView tvTarget;
        ImageView ivImage;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvExerciseNameDetail);
            tvTarget = itemView.findViewById(R.id.tvTargetDetail);
            ivImage = itemView.findViewById(R.id.ivExerciseDetail);
        }
    }
}