package es.iesagora.fitjourney.ui.second.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import es.iesagora.fitjourney.R;
import es.iesagora.fitjourney.data.model.Routine;
import es.iesagora.fitjourney.utils.ImageUtils;

public class RoutineAdapter extends RecyclerView.Adapter<RoutineAdapter.ViewHolder> {

    private List<Routine> routines = new ArrayList<>();
    private final OnRoutineListener listener;
    private final int currentUserId;
    private final boolean isAdmin;

    public interface OnRoutineListener {
        void onRoutineClick(Routine routine);
        void onDeleteClick(Routine routine);
    }

    public RoutineAdapter(int currentUserId, boolean isAdmin, OnRoutineListener listener) {
        this.currentUserId = currentUserId;
        this.isAdmin = isAdmin;
        this.listener = listener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setRoutines(List<Routine> routines) {
        this.routines = routines;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_routine, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Routine routine = routines.get(position);

        // Asignamos el nombre
        holder.tvName.setText(routine.getName());

        ImageUtils.loadInto(
                holder.itemView.getContext(),
                routine.getPhotoUrl(),
                holder.ivImage,
                R.drawable.img_construccion
        );

        if (isAdmin || routine.getIdUser() == currentUserId) {
            holder.btnDelete.setVisibility(View.VISIBLE);
            holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(routine));
        } else {
            holder.btnDelete.setVisibility(View.GONE);
        }

        // Clic en la tarjeta entera
        holder.itemView.setOnClickListener(v -> listener.onRoutineClick(routine));
    }

    @Override
    public int getItemCount() {
        return routines != null ? routines.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        ImageView ivImage;
        ImageView btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvRoutineName);
            ivImage = itemView.findViewById(R.id.ivRoutineImage);
            btnDelete = itemView.findViewById(R.id.btnDeleteRoutine);
        }
    }
}