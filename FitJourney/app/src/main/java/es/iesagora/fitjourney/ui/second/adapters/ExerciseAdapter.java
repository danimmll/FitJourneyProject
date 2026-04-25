package es.iesagora.fitjourney.ui.second.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
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
        holder.tvDifficulty.setText(exercise.getDifficulty());

        holder.tvMuscle.setText(exercise.getMuscle_group());
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
            }
            else {
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
        TextView tvName, tvMuscle, tvDifficulty;

        public ExerciseViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivItemImage);
            tvName = itemView.findViewById(R.id.tvItemName);
            tvMuscle = itemView.findViewById(R.id.tvItemMuscle);
            tvDifficulty = itemView.findViewById(R.id.tvItemDifficulty);
        }
    }
}