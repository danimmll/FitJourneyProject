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

/**
 * Adapter encargado de mostrar la lista de rutinas
 * dentro de un RecyclerView.
 *
 * <p>Gestiona:</p>
 * <ul>
 *     <li>Renderizado de cada rutina</li>
 *     <li>Imagen representativa</li>
 *     <li>Control de permisos para borrar</li>
 *     <li>Eventos de click en item y botón eliminar</li>
 * </ul>
 */
public class RoutineAdapter extends RecyclerView.Adapter<RoutineAdapter.ViewHolder> {

    /**
     * Lista de rutinas a mostrar.
     */
    private List<Routine> routines = new ArrayList<>();

    /**
     * Listener para eventos de la UI.
     */
    private final OnRoutineListener listener;

    /**
     * ID del usuario actual.
     */
    private final int currentUserId;

    /**
     * Indica si el usuario es administrador.
     */
    private final boolean isAdmin;

    /**
     * Interfaz de comunicación con la UI.
     */
    public interface OnRoutineListener {

        /**
         * Click sobre una rutina.
         */
        void onRoutineClick(Routine routine);

        /**
         * Click en eliminar rutina.
         */
        void onDeleteClick(Routine routine);
    }

    /**
     * Constructor del adapter.
     *
     * @param currentUserId id del usuario actual.
     * @param isAdmin indica si es administrador.
     * @param listener listener de eventos.
     */
    public RoutineAdapter(int currentUserId, boolean isAdmin, OnRoutineListener listener) {
        this.currentUserId = currentUserId;
        this.isAdmin = isAdmin;
        this.listener = listener;
    }

    /**
     * Actualiza la lista de rutinas.
     *
     * @param routines nueva lista.
     */
    @SuppressLint("NotifyDataSetChanged")
    public void setRoutines(List<Routine> routines) {
        this.routines = routines;
        notifyDataSetChanged();
    }

    /**
     * Crea el ViewHolder.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.viewholder_routine, parent, false);

        return new ViewHolder(view);
    }

    /**
     * Vincula datos con la vista.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Routine routine = routines.get(position);

        holder.tvName.setText(routine.getName());

        ImageUtils.loadInto(
                holder.itemView.getContext(),
                routine.getDisplayImage(),
                holder.ivImage,
                R.drawable.img_construccion
        );

        // Mostrar botón de borrar solo si es admin o dueño
        if (isAdmin || routine.getIdUser() == currentUserId) {

            holder.btnDelete.setVisibility(View.VISIBLE);

            holder.btnDelete.setOnClickListener(v ->
                    listener.onDeleteClick(routine)
            );

        } else {
            holder.btnDelete.setVisibility(View.GONE);
        }

        // Click en toda la tarjeta
        holder.itemView.setOnClickListener(v ->
                listener.onRoutineClick(routine)
        );
    }

    /**
     * Número total de elementos.
     */
    @Override
    public int getItemCount() {
        return routines != null ? routines.size() : 0;
    }

    /**
     * ViewHolder de rutina.
     */
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