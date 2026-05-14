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

public class WorkoutDetailAdapter extends RecyclerView.Adapter<WorkoutDetailAdapter.ViewHolder> {

    private final List<WorkoutDetail> details;

    public WorkoutDetailAdapter(List<WorkoutDetail> details) {
        this.details = details;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_workout_detail_item, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WorkoutDetail detail = details.get(position);

        if (detail.getExercise() != null) {
            holder.tvName.setText(detail.getExercise().getName());
        } else {
            holder.tvName.setText("Ejercicio");
        }

        String stats = detail.getSetsPerformed() + " series x (" + detail.getRepsPerformed() + ") reps • " + detail.getMaxWeightUsed() + " kg";
        holder.tvStats.setText(stats);
    }

    @Override
    public int getItemCount() {
        return details != null ? details.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvStats;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvExerciseName);
            tvStats = itemView.findViewById(R.id.tvStats);
        }
    }
}