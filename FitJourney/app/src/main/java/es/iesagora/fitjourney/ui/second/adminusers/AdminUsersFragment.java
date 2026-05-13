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
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;

import es.iesagora.fitjourney.R;
import es.iesagora.fitjourney.data.model.User;
import es.iesagora.fitjourney.databinding.DialogAdminUserOptionsBinding;
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

        setupAdminObservers();
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
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { /* TODO document why this method is empty */ }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (adapter != null) {
                    adapter.filter(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) { /* TODO document why this method is empty */ }
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
     * Registra los observers de acciones admin una sola vez.
     */
    private void setupAdminObservers() {

        viewModel.getBanUserLiveData().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;
            switch (resource.status) {
                case LOADING:
                    binding.progressBarAdmin.setVisibility(View.VISIBLE);
                    break;
                case SUCCESS:
                    binding.progressBarAdmin.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Usuario baneado correctamente", Toast.LENGTH_SHORT).show();
                    loadUsers();
                    break;
                case ERROR:
                    binding.progressBarAdmin.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Error al banear: " + resource.message, Toast.LENGTH_SHORT).show();
                    break;
            }
        });

        viewModel.getDeleteUserLiveData().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;
            switch (resource.status) {
                case LOADING:
                    binding.progressBarAdmin.setVisibility(View.VISIBLE);
                    break;
                case SUCCESS:
                    binding.progressBarAdmin.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Usuario eliminado", Toast.LENGTH_SHORT).show();
                    loadUsers();
                    break;
                case ERROR:
                    binding.progressBarAdmin.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Error al eliminar: " + resource.message, Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }

    /**
     * Muestra el menú inferior con las opciones para el usuario seleccionado.
     */
    @SuppressLint("SetTextI18n")
    private void showUserOptionsDialog(User user) {
        DialogAdminUserOptionsBinding dialogBinding = DialogAdminUserOptionsBinding.inflate(getLayoutInflater());

        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());

        dialog.setContentView(dialogBinding.getRoot());

        dialogBinding.tvOptionsTitle.setText("Opciones para " + user.getName());

        boolean isActive = user.getActive() != null && user.getActive() == 1;

        if (!isActive) {
            dialogBinding.btnToggleStatus.setVisibility(View.GONE);
        }

        dialogBinding.btnEditUser.setOnClickListener(v -> {
            dialog.dismiss();
            navigateToEditUser(user);
        });

        dialogBinding.btnToggleStatus.setOnClickListener(v -> {
            dialog.dismiss();
            new AlertDialog.Builder(requireContext())
                    .setTitle("SUSPENDER USUARIO")
                    .setMessage("¿Estás seguro de que quieres suspender a " + user.getName() + "?")
                    .setPositiveButton("Sí", (d, which) -> deleteUser(user))
                    .setNegativeButton("Cancelar", null)
                    .show();
        });

        dialog.show();
    }

    /**
     * Navega a la pantalla de edición de perfil pasándole los datos del usuario.
     */
    private void navigateToEditUser(User user) {
        Bundle bundle = new Bundle();

        bundle.putSerializable("user_data", user);
        bundle.putBoolean("isAdminEdit", true);

        Navigation.findNavController(requireView())
                .navigate(R.id.editProfileFragment, bundle);
    }

    /**
     * Lanza la acción para eliminar la cuenta de un usuario.
     */
    private void deleteUser(User user) {
        viewModel.deleteUserByAdmin(user.getIdUser());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}