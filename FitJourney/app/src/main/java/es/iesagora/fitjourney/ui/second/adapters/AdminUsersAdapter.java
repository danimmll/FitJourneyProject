package es.iesagora.fitjourney.ui.second.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import es.iesagora.fitjourney.data.model.User;
import es.iesagora.fitjourney.databinding.ViewholderAdminUserBinding;

public class AdminUsersAdapter extends RecyclerView.Adapter<AdminUsersAdapter.UserViewHolder> {

    private List<User> userList;
    private List<User> userListFull;
    private final OnUserClickListener listener;

    public interface OnUserClickListener {
        void onUserMenuClick(User user);
    }

    public AdminUsersAdapter(List<User> userList, OnUserClickListener listener) {
        this.userList = userList;
        this.userListFull = new ArrayList<>(userList);
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewholderAdminUserBinding binding = ViewholderAdminUserBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new UserViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.binding.tvAdminUserName.setText(user.getName());
        holder.binding.tvAdminUserEmail.setText(user.getEmail());

        Glide.with(holder.itemView).load(user.getDisplayAvatar()).into(holder.binding.ivAvatarUser);

        holder.binding.ivMenuUser.setOnClickListener(v -> listener.onUserMenuClick(user));
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void filter(String text) {
        if (text.isEmpty()) {
            userList = new ArrayList<>(userListFull);
        } else {
            String filterPattern = text.toLowerCase().trim();
            List<User> filtered = new ArrayList<>();
            for (User user : userListFull) {
                String name = user.getName() != null ? user.getName().toLowerCase() : "";
                String email = user.getEmail() != null ? user.getEmail().toLowerCase() : "";
                if (name.contains(filterPattern) || email.contains(filterPattern)) {
                    filtered.add(user);
                }
            }
            userList = filtered;
        }
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateList(List<User> newList) {
        this.userListFull = new ArrayList<>(newList);
        this.userList = newList;
        notifyDataSetChanged();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        ViewholderAdminUserBinding binding;
        UserViewHolder(ViewholderAdminUserBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
