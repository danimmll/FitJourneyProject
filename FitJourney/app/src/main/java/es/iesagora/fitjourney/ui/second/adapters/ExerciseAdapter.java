package es.iesagora.fitjourney.ui.second.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.util.Base64;
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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import java.util.ArrayList;
import java.util.List;

import es.iesagora.fitjourney.R;
import es.iesagora.fitjourney.data.model.Exercise;
import es.iesagora.fitjourney.viewmodel.SecondViewModel;

public class ExerciseAdapter extends RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder> {

    private List<Exercise> exercises;
    private Context context;
    private SecondViewModel viewModel;

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
        View view = LayoutInflater.from(context).inflate(R.layout.viewholder_exercise, parent, false);
        return new ExerciseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExerciseViewHolder holder, int position) {
        Exercise exercise = exercises.get(position);

        holder.tvName.setText(exercise.getName());
        holder.tvMuscle.setText(exercise.getMuscle_group());

        String diff = exercise.getDifficulty() != null ? exercise.getDifficulty().toLowerCase() : "";

        int colorActive = ContextCompat.getColor(context, R.color.mainrosa);
        int colorInactive = Color.parseColor("#AAAAAA");

        holder.diffLine1.setBackgroundColor(colorInactive);
        holder.diffLine2.setBackgroundColor(colorInactive);
        holder.diffLine3.setBackgroundColor(colorActive);

        if (diff.contains("intermedio") || diff.equals("2")) {
            holder.diffLine2.setBackgroundColor(colorActive);
        }
        else if (diff.contains("difícil") || diff.contains("dificil") || diff.contains("avanzado") || diff.equals("3")) {
            holder.diffLine1.setBackgroundColor(colorActive);
            holder.diffLine2.setBackgroundColor(colorActive);
        }

        String imageUrl = exercise.getImage_url();

        if (imageUrl != null && !imageUrl.isEmpty()) {
            if (imageUrl.length() > 200 || !imageUrl.startsWith("http")) {
                try {
                    byte[] imageBytes = Base64.decode(imageUrl, Base64.DEFAULT);

                    Glide.with(holder.itemView.getContext())
                            .asBitmap()
                            .load(imageBytes)
                            .placeholder(R.drawable.img_construccion)
                            .error(android.R.drawable.ic_menu_report_image)
                            .centerCrop()
                            .into(holder.ivImage);

                } catch (Exception e) {
                    holder.ivImage.setImageResource(android.R.drawable.ic_menu_report_image);
                }
            } else {
                Glide.with(holder.itemView.getContext())
                        .load(imageUrl)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .placeholder(R.drawable.img_construccion)
                        .error(android.R.drawable.ic_menu_report_image)
                        .centerCrop()
                        .into(holder.ivImage);
            }
        } else {
            holder.ivImage.setImageResource(R.drawable.img_construccion);
        }

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
        ImageView ivImage;
        TextView tvName, tvMuscle;
        View diffLine1, diffLine2, diffLine3;

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