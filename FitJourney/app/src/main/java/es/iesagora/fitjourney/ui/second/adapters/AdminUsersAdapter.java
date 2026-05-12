package es.iesagora.fitjourney.ui.second.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import es.iesagora.fitjourney.R;
import es.iesagora.fitjourney.data.model.User;
import es.iesagora.fitjourney.databinding.ViewholderAdminUserBinding;
import es.iesagora.fitjourney.utils.ImageUtils;

/**
 * Adapter encargado de mostrar la lista de usuarios para administración.
 *
 * <p>Permite:</p>
 * <ul>
 *     <li>Mostrar avatar, nombre y correo.</li>
 *     <li>Filtrar usuarios por nombre o email.</li>
 *     <li>Abrir el menú de acciones administrativas.</li>
 * </ul>
 */
public class AdminUsersAdapter
        extends RecyclerView.Adapter<AdminUsersAdapter.UserViewHolder> {

    /** Lista actualmente mostrada */
    private List<User> userList;

    /** Copia completa utilizada para filtrado */
    private List<User> userListFull;

    /** Listener de acciones sobre usuario */
    private final OnUserClickListener listener;

    /**
     * Listener para abrir el menú de opciones del usuario.
     */
    public interface OnUserClickListener {

        /**
         * Se ejecuta al pulsar el menú contextual del usuario.
         *
         * @param user usuario seleccionado
         */
        void onUserMenuClick(User user);
    }

    /**
     * Constructor del adapter.
     *
     * @param userList lista inicial de usuarios
     * @param listener listener de acciones
     */
    public AdminUsersAdapter(List<User> userList,
                             OnUserClickListener listener) {

        this.userList = userList;
        this.userListFull = new ArrayList<>(userList);
        this.listener = listener;
    }

    /**
     * Infla el layout de cada usuario.
     *
     * @param parent contenedor padre
     * @param viewType tipo de vista
     * @return ViewHolder del usuario
     */
    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                             int viewType) {

        ViewholderAdminUserBinding binding =
                ViewholderAdminUserBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                );

        return new UserViewHolder(binding);
    }

    /**
     * Vincula los datos del usuario con la interfaz.
     *
     * <p>Se encarga de:</p>
     * <ul>
     *     <li>Mostrar nombre y correo.</li>
     *     <li>Cargar avatar circular mediante ImageUtils.</li>
     *     <li>Gestionar el menú contextual del usuario.</li>
     * </ul>
     *
     * @param holder ViewHolder actual
     * @param position posición del elemento
     */
    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder,
                                 int position) {

        User user = userList.get(position);

        holder.binding.tvAdminUserName.setText(user.getName());
        holder.binding.tvAdminUserEmail.setText(user.getEmail());

        ImageUtils.loadCircle(
                holder.itemView.getContext(),
                user.getDisplayAvatar(),
                holder.binding.ivAvatarUser,
                R.drawable.ic_profile
        );

        holder.binding.ivMenuUser.setOnClickListener(
                v -> listener.onUserMenuClick(user)
        );
    }

    /**
     * Devuelve el número total de usuarios.
     *
     * @return tamaño de la lista
     */
    @Override
    public int getItemCount() {
        return userList.size();
    }

    /**
     * Filtra la lista de usuarios por nombre o email.
     *
     * @param text texto introducido en la búsqueda
     */
    @SuppressLint("NotifyDataSetChanged")
    public void filter(String text) {

        if (text.isEmpty()) {

            userList = new ArrayList<>(userListFull);

        } else {

            String filterPattern = text.toLowerCase().trim();

            List<User> filtered = new ArrayList<>();

            for (User user : userListFull) {

                String name = user.getName() != null
                        ? user.getName().toLowerCase()
                        : "";

                String email = user.getEmail() != null
                        ? user.getEmail().toLowerCase()
                        : "";

                if (name.contains(filterPattern)
                        || email.contains(filterPattern)) {

                    filtered.add(user);
                }
            }

            userList = filtered;
        }

        notifyDataSetChanged();
    }

    /**
     * Actualiza completamente la lista del adapter.
     *
     * @param newList nueva lista de usuarios
     */
    @SuppressLint("NotifyDataSetChanged")
    public void updateList(List<User> newList) {

        this.userListFull = new ArrayList<>(newList);
        this.userList = newList;

        notifyDataSetChanged();
    }

    /**
     * ViewHolder que representa cada usuario de la lista.
     */
    static class UserViewHolder extends RecyclerView.ViewHolder {

        /** Binding del item */
        ViewholderAdminUserBinding binding;

        /**
         * Constructor del ViewHolder.
         *
         * @param binding binding del layout
         */
        UserViewHolder(ViewholderAdminUserBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}