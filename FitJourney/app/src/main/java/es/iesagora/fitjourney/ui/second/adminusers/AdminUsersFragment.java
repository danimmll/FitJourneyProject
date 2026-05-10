package es.iesagora.fitjourney.ui.second.adminusers;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;

import es.iesagora.fitjourney.R;
import es.iesagora.fitjourney.data.model.User;
import es.iesagora.fitjourney.databinding.FragmentAdminUsersBinding;
import es.iesagora.fitjourney.ui.second.adapters.AdminUsersAdapter;
import es.iesagora.fitjourney.viewmodel.SecondViewModel;

/**
 * Fragmento exclusivo para el administrador.
 * Muestra una lista completa de todos los usuarios registrados en la aplicación.
 * Permite buscar usuarios por nombre o correo y gestionarlos (editar, borrar, banear).
 */
public class AdminUsersFragment extends Fragment {

    private FragmentAdminUsersBinding binding;
    private AdminUsersAdapter adapter;
    private SecondViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAdminUsersBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(SecondViewModel.class);

        setupRecyclerView();
        setupSearchBar();
        loadUsers();
    }

    /**
     * Configura el RecyclerView y su Adaptador.
     */
    private void setupRecyclerView() {
        adapter = new AdminUsersAdapter(new ArrayList<>(), this::showUserOptionsDialog);

        binding.rvAdminUsers.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvAdminUsers.setAdapter(adapter);
    }

    /**
     * Configura la barra de búsqueda para filtrar la lista de usuarios en tiempo real.
     */
    private void setupSearchBar() {
        binding.etSearchUser.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (adapter != null) {
                    adapter.filter(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    /**
     * Realiza la petición al servidor para obtener todos los usuarios y actualizar la lista.
     */
    private void loadUsers() {
        viewModel.getAllUsers().observe(getViewLifecycleOwner(), resource -> {
            if (resource != null) {
                switch (resource.status) {
                    case LOADING:
                        binding.progressBarAdmin.setVisibility(View.VISIBLE);
                        binding.rvAdminUsers.setVisibility(View.GONE);
                        break;
                    case SUCCESS:
                        binding.progressBarAdmin.setVisibility(View.GONE);
                        binding.rvAdminUsers.setVisibility(View.VISIBLE);
                        if (resource.data != null) {
                            adapter.updateList(resource.data);
                        }
                        break;
                    case ERROR:
                        binding.progressBarAdmin.setVisibility(View.GONE);
                        binding.rvAdminUsers.setVisibility(View.VISIBLE);
                        Toast.makeText(getContext(), resource.message, Toast.LENGTH_LONG).show();
                        break;
                }
            }
        });
    }

    /**
     * Muestra el menú inferior con las opciones para el usuario seleccionado.
     */
    @SuppressLint("SetTextI18n")
    private void showUserOptionsDialog(User user) {
        @SuppressLint("InflateParams") View dialogView = getLayoutInflater().inflate(R.layout.dialog_admin_user_options, null);
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        dialog.setContentView(dialogView);

        TextView tvTitle = dialogView.findViewById(R.id.tvOptionsTitle);
        View btnEdit = dialogView.findViewById(R.id.btnEditUser);
        View btnBan = dialogView.findViewById(R.id.btnBanUser);
        View btnDelete = dialogView.findViewById(R.id.btnDeleteUser);

        tvTitle.setText("Opciones: " + user.getName());

        btnEdit.setOnClickListener(v -> {
            dialog.dismiss();
            navigateToEditUser(user);
        });

        btnBan.setOnClickListener(v -> {
            dialog.dismiss();
            banUser(user);
        });

        btnDelete.setOnClickListener(v -> {
            dialog.dismiss();
            confirmDeleteUser(user);
        });

        dialog.show();
    }

    /**
     * Navega a la pantalla de edición de perfil pasándole los datos del usuario.
     */
    private void navigateToEditUser(User user) {
        Bundle bundle = new Bundle();
        // Pasamos el correo o ID para que el Fragment de destino sepa a quién cargar
        bundle.putString("userEmail", user.getEmail());
        bundle.putBoolean("isAdminEdit", true); // Bandera para saber que entra un admin

        // Navegamos a la pantalla de edición (asegúrate de que R.id.editProfileFragment sea correcto en tu nav_graph)
        Navigation.findNavController(requireView()).navigate(R.id.editProfileFragment, bundle);
    }

    /**
     * Lógica para banear/suspender a un usuario.
     */
    private void banUser(User user) {
        // Asumiendo que crearás este método en el ViewModel
        viewModel.banUser(user.getEmail()).observe(getViewLifecycleOwner(), resource -> {
            if (resource != null) {
                switch (resource.status) {
                    case LOADING:
                        binding.progressBarAdmin.setVisibility(View.VISIBLE);
                        break;
                    case SUCCESS:
                        binding.progressBarAdmin.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Usuario baneado correctamente", Toast.LENGTH_SHORT).show();
                        loadUsers(); // Recargamos la lista para actualizar su estado
                        break;
                    case ERROR:
                        binding.progressBarAdmin.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Error al banear: " + resource.message, Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });
    }

    /**
     * Muestra un diálogo de confirmación antes de eliminar permanentemente a un usuario.
     */
    private void confirmDeleteUser(User user) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Eliminar Usuario")
                .setMessage("¿Estás seguro de que deseas eliminar permanentemente a " + user.getName() + "? Esta acción no se puede deshacer.")
                .setPositiveButton("Eliminar", (dialog, which) -> deleteUser(user))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    /**
     * Lógica para eliminar la cuenta de un usuario desde la administración.
     */
    private void deleteUser(User user) {
        viewModel.deleteUserByAdmin(user.getEmail()).observe(getViewLifecycleOwner(), resource -> {
            if (resource != null) {
                switch (resource.status) {
                    case LOADING:
                        binding.progressBarAdmin.setVisibility(View.VISIBLE);
                        break;
                    case SUCCESS:
                        binding.progressBarAdmin.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Usuario eliminado", Toast.LENGTH_SHORT).show();
                        loadUsers(); // Recargamos la lista para que desaparezca
                        break;
                    case ERROR:
                        binding.progressBarAdmin.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Error al eliminar: " + resource.message, Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}