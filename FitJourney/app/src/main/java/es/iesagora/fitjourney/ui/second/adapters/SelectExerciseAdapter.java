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

public class SelectExerciseAdapter extends RecyclerView.Adapter<AddedExerciseAdapter.ViewHolder> {

    public interface OnExerciseClickListener {
        void onExerciseClick(Exercise exercise);
    }

    private final List<Exercise> exercises = new ArrayList<>();
    private final OnExerciseClickListener listener;

    public SelectExerciseAdapter(OnExerciseClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public AddedExerciseAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.viewholder_added_exercise, parent, false);
        return new AddedExerciseAdapter.ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull AddedExerciseAdapter.ViewHolder holder, int position) {
        Exercise exercise = exercises.get(position);

        holder.tvName.setText(exercise.getName());
        ImageUtils.loadInto(holder.itemView.getContext(), exercise.getDisplayImage(),
                holder.ivExercise, R.drawable.ic_ejercicios);

        holder.tvDetails.setText(exercise.getMuscleGroup() + " · " + exercise.getDifficulty());
        holder.btnRemove.setImageResource(android.R.drawable.ic_input_add);
        holder.btnRemove.setOnClickListener(v -> listener.onExerciseClick(exercise));
    }

    @Override
    public int getItemCount() {
        return exercises.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void submitList(List<Exercise> newItems) {
        exercises.clear();
        exercises.addAll(newItems);
        notifyDataSetChanged();
    }
}
