package es.iesagora.fitjourney.ui.second.profile;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import java.util.Objects;

import es.iesagora.fitjourney.R;
import es.iesagora.fitjourney.data.model.User;
import es.iesagora.fitjourney.data.remote.Resource;
import es.iesagora.fitjourney.data.repositories.PreferencesRepository;
import es.iesagora.fitjourney.databinding.DialogConfirmDeleteBinding;
import es.iesagora.fitjourney.databinding.FragmentEditProfileBinding;
import es.iesagora.fitjourney.ui.main.MainActivity;
import es.iesagora.fitjourney.utils.ImageUtils;
import es.iesagora.fitjourney.viewmodel.SecondViewModel;

/**
 * Fragment encargado de la edición del perfil del usuario.
 * Este fragment permite al usuario autenticado modificar sus datos personales
 * dentro de la aplicación, incluyendo nombre, contraseña, altura, peso y objetivo fitness.
 * (La edición de la foto de perfil ha sido deshabilitada).
 */
public class EditProfileFragment extends Fragment {

    private FragmentEditProfileBinding binding;
    private SecondViewModel viewModel;
    private User currentUser;

    private static final String USERDATA_STRING = "user_data";

    private boolean isAdminEdit = false;
    private boolean passwordWasChanged = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEditProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(SecondViewModel.class);
        PreferencesRepository prefs = new PreferencesRepository(requireContext());

        setupDropdowns();

        if (getArguments() != null && getArguments().getBoolean("isAdminEdit", false)) {
            isAdminEdit = true;
            currentUser = (User) getArguments().getSerializable(USERDATA_STRING);
        } else if (getArguments() != null && getArguments().containsKey(USERDATA_STRING)) {
            currentUser = (User) getArguments().getSerializable(USERDATA_STRING);
        } else {
            Toast.makeText(getContext(), "Error al cargar datos", Toast.LENGTH_SHORT).show();
            Navigation.findNavController(view).navigateUp();
            return;
        }

        setupUIForRole();
        loadUserData();
        setupListeners();
        setupObservers(prefs);
    }

    private void setupDropdowns() {
        String[] goals = {
                getString(R.string.lose_weight),
                getString(R.string.maintain_weight),
                getString(R.string.gain_muscle),
                getString(R.string.lose_fat),
                getString(R.string.gain_strength),
                getString(R.string.gain_endurance),
                getString(R.string.gain_flexibility),
                getString(R.string.recover_from_injury)
        };

        ArrayAdapter<String> adapterGoals = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                goals
        );

        binding.actvGoal.setAdapter(adapterGoals);
    }

    @SuppressLint("SetTextI18n")
    private void setupUIForRole() {
        if (isAdminEdit) {
            binding.tilCurrentPassword.setVisibility(View.GONE);
            binding.tilNewPassword.setVisibility(View.GONE);
            binding.tilConfirmPassword.setVisibility(View.GONE);
            binding.btnDeleteAccount.setVisibility(View.GONE);
            binding.tvEditProfile.setText("Editando perfil como Administrador");
        }
    }

    private void setupListeners() {
        binding.btnSaveProfile.setOnClickListener(v -> submitChanges());

        if (!isAdminEdit) {
            binding.btnDeleteAccount.setOnClickListener(v -> showDeleteConfirmationDialog());
        }
    }

    private void loadUserData() {
        if (currentUser == null) return;

        binding.etName.setText(currentUser.getName() != null ? currentUser.getName() : "");

        if (currentUser.getHeight() != null) {
            binding.etHeight.setText(String.valueOf(currentUser.getHeight().intValue()));
        }

        if (currentUser.getWeight() != null) {
            binding.etWeight.setText(String.valueOf(currentUser.getWeight()));
        }

        ImageUtils.loadCircle(requireContext(), currentUser.getDisplayAvatar(), binding.ivAvatar, R.drawable.ic_profile);

        if (currentUser.getGoal() != null) {
            binding.actvGoal.setText(currentUser.getGoal(), false);
        }
    }

    private void submitChanges() {
        String name = Objects.requireNonNull(binding.etName.getText()).toString().trim();
        String heightStr = Objects.requireNonNull(binding.etHeight.getText()).toString().trim();
        String weightStr = Objects.requireNonNull(binding.etWeight.getText()).toString().trim();
        String goalStr = binding.actvGoal.getText().toString().trim();

        String newPasswordStr = binding.etNewPassword.getText() != null ? binding.etNewPassword.getText().toString().trim() : "";
        String confirmPasswordStr = binding.etConfirmPassword.getText() != null ? binding.etConfirmPassword.getText().toString().trim() : "";

        if (name.isEmpty()) {
            binding.tilName.setError("El nombre no puede estar vacío");
            return;
        } else {
            binding.tilName.setError(null);
        }

        binding.tLGoal.setError(null);

        User updatedUser = new User();
        updatedUser.setName(name);

        if (!goalStr.isEmpty()) {
            updatedUser.setGoal(goalStr);
        }

        // Conservamos la URL de la foto que ya tuviera el usuario
        updatedUser.setPhotoUrl(currentUser.getPhotoUrl());

        if (!isAdminEdit && !newPasswordStr.isEmpty()) {
            if (!newPasswordStr.equals(confirmPasswordStr)) {
                binding.tilConfirmPassword.setError("Las contraseñas no coinciden");
                return;
            }
            binding.tilConfirmPassword.setError(null);
            updatedUser.setPassword(newPasswordStr);
            passwordWasChanged = true;
        }

        try {
            if (!heightStr.isEmpty()) updatedUser.setHeight(Double.parseDouble(heightStr));
            if (!weightStr.isEmpty()) updatedUser.setWeight(Double.parseDouble(weightStr));
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Revisa el peso o altura", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isAdminEdit) {
            viewModel.updateUserByAdmin(currentUser.getIdUser(), updatedUser);
        } else {
            viewModel.updateProfile(updatedUser);
        }
    }

    private void setupObservers(PreferencesRepository prefs) {
        viewModel.getUpdateProfileStatus().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;

            switch (resource.status) {
                case LOADING:
                    binding.progressBar.setVisibility(View.VISIBLE);
                    binding.btnSaveProfile.setEnabled(false);
                    break;

                case SUCCESS:
                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnSaveProfile.setEnabled(true);

                    Toast.makeText(getContext(), "¡Perfil actualizado con éxito!", Toast.LENGTH_SHORT).show();

                    if (!isAdminEdit && prefs.getToken() != null) {
                        viewModel.fetchProfile(prefs.getToken());
                    }

                    if (!isAdminEdit && passwordWasChanged) {
                        viewModel.logout();
                        passwordWasChanged = false;
                        Toast.makeText(getContext(), "Contraseña actualizada. Inicia sesión de nuevo.", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(requireContext(), MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        requireActivity().finish();
                        return;
                    }

                    LiveData<Resource<User>> updateStatus = viewModel.getUpdateProfileStatus();
                    if (updateStatus instanceof MutableLiveData) {
                        ((MutableLiveData<Resource<User>>) updateStatus).setValue(null);
                    }

                    Navigation.findNavController(requireView()).navigateUp();
                    break;

                case ERROR:
                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnSaveProfile.setEnabled(true);

                    Toast.makeText(getContext(), "Error: " + resource.message, Toast.LENGTH_LONG).show();

                    LiveData<Resource<User>> errorStatus = viewModel.getUpdateProfileStatus();
                    if (errorStatus instanceof MutableLiveData) {
                        ((MutableLiveData<Resource<User>>) errorStatus).setValue(null);
                    }
                    break;
            }
        });

        viewModel.getDeleteAccountStatus().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;

            switch (resource.status) {
                case LOADING:
                    binding.progressBar.setVisibility(View.VISIBLE);
                    binding.btnDeleteAccount.setEnabled(false);
                    break;
                case SUCCESS:
                    binding.progressBar.setVisibility(View.GONE);
                    viewModel.logout();

                    Intent intent = new Intent(requireActivity(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    requireActivity().finish();
                    break;
                case ERROR:
                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnDeleteAccount.setEnabled(true);

                    binding.tilCurrentPassword.setError("Error al desactivar cuenta: " + resource.message);

                    LiveData<Resource<String>> deleteStatus = viewModel.getDeleteAccountStatus();
                    if (deleteStatus instanceof MutableLiveData) {
                        ((MutableLiveData<Resource<String>>) deleteStatus).setValue(null);
                    }
                    break;
            }
        });
    }

    private void showDeleteConfirmationDialog() {
        String currentPassword = binding.etCurrentPassword.getText() != null
                ? binding.etCurrentPassword.getText().toString().trim()
                : "";

        if (currentPassword.isEmpty()) {
            binding.tilCurrentPassword.setError("Introduce tu contraseña actual para desactivar la cuenta");
            binding.etCurrentPassword.requestFocus();
            return;
        }
        binding.tilCurrentPassword.setError(null);

        DialogConfirmDeleteBinding dialogBinding = DialogConfirmDeleteBinding.inflate(getLayoutInflater());

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogBinding.getRoot())
                .setCancelable(true)
                .create();

        dialogBinding.btnCancelDelete.setOnClickListener(v -> dialog.dismiss());

        dialogBinding.btnConfirmDelete.setOnClickListener(v -> {
            dialog.dismiss();
            String email = currentUser != null ? currentUser.getEmail() : null;
            if (email != null && !email.trim().isEmpty()) {
                viewModel.deactivateMyAccount(email, currentPassword);
            } else {
                Toast.makeText(getContext(), "Error: No se encontró el email", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}