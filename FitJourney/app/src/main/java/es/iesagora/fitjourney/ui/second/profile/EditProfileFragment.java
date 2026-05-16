package es.iesagora.fitjourney.ui.second.profile;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
 * Permite modificar datos personales como nombre, peso, altura, objetivo y contraseña.
 * También permite cambiar la foto de perfil (galería o URL) y eliminar la cuenta.
 */
public class EditProfileFragment extends Fragment {

    private FragmentEditProfileBinding binding;
    private SecondViewModel viewModel;
    private User currentUser;

    private static final String USERDATA_STRING = "user_data";

    private boolean isAdminEdit = false;
    private boolean passwordWasChanged = false;

    private Uri selectedLocalImageUri = null;
    private String currentImageBase64 = null;

    /** Lanzador para seleccionar imagen de la galería */
    private final ActivityResultLauncher<String> getContent =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null && binding != null) {
                    selectedLocalImageUri = uri;
                    currentImageBase64 = null;
                    binding.etAvatarUrl.setText("");
                    ImageUtils.loadCircle(requireContext(), uri.toString(),
                            binding.ivAvatar, R.drawable.ic_profile);
                }
            });

    /**
     * Infla la vista asociada al fragmento utilizando View Binding.
     *
     * @param inflater El objeto LayoutInflater que se usa para inflar las vistas.
     * @param container Si no es nulo, es la vista madre a la que se debe adjuntar la UI.
     * @param savedInstanceState Si no es nulo, el fragmento se está reconstruyendo a partir de este estado.
     * @return La vista raíz (Root View) del fragmento.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEditProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * Inicializa los componentes de la interfaz de usuario, procesa los argumentos de navegación,
     * carga los datos del usuario y configura los listeners y observadores necesarios.
     *
     * @param view La vista devuelta por {@link #onCreateView}.
     * @param savedInstanceState Si no es nulo, el fragmento se está reconstruyendo a partir de este estado.
     */
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
        setupImageListeners();
        setupListeners();
        setupObservers(prefs);
    }

    /**
     * Configura los escuchadores de eventos relacionados con la gestión de la imagen de perfil,
     * controlando tanto la selección desde la galería como la carga por URL cuando se pierde el foco del campo.
     */
    private void setupImageListeners() {
        binding.frameAvatar.setOnClickListener(v -> getContent.launch("image/*"));

        binding.btnSelectPhoto.setOnClickListener(v -> getContent.launch("image/*"));

        binding.etAvatarUrl.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus && binding != null) {
                String url = binding.etAvatarUrl.getText() != null
                        ? binding.etAvatarUrl.getText().toString().trim() : "";
                if (!url.isEmpty()) {
                    selectedLocalImageUri = null;
                    currentImageBase64 = null;
                    ImageUtils.loadCircle(requireContext(), url,
                            binding.ivAvatar, R.drawable.ic_profile);
                }
            }
        });
    }

    /**
     * Resuelve qué datos de imagen utilizar priorizando en el siguiente orden:
     * 1. Imagen local seleccionada de la galería (convertida a Base64).
     * 2. URL de imagen remota introducida en el campo de texto.
     * 3. Imagen en formato Base64 previamente existente del usuario.
     *
     * @return Una cadena de texto con los datos de la imagen (Base64 o URL), o null si ocurre un fallo.
     */
    @Nullable
    private String resolvePhotoData() {
        if (selectedLocalImageUri != null) {
            String base64 = ImageUtils.uriToBase64(
                    requireContext().getContentResolver(), selectedLocalImageUri);
            if (base64 == null) {
                Toast.makeText(getContext(), "Error al procesar la imagen", Toast.LENGTH_SHORT).show();
            }
            return base64;
        }

        String urlInput = binding.etAvatarUrl.getText() != null
                ? binding.etAvatarUrl.getText().toString().trim() : "";
        if (!urlInput.isEmpty()) return urlInput;

        return currentImageBase64;
    }

    /**
     * Configura el menú desplegable (Spinner/AutoCompleteTextView) con las opciones disponibles
     * para el objetivo principal de acondicionamiento físico del usuario.
     */
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

    /**
     * Adapta los elementos de la interfaz de usuario en función del rol de la edición actual.
     * Si la edición es realizada por un administrador, se ocultan los campos de contraseña
     * y el botón de eliminación de cuenta.
     */
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

    /**
     * Configura los escuchadores de clics para los botones de acción global del fragmento
     * (guardar cambios y eliminar cuenta).
     */
    private void setupListeners() {
        binding.btnSaveProfile.setOnClickListener(v -> submitChanges());

        if (!isAdminEdit) {
            binding.btnDeleteAccount.setOnClickListener(v -> showDeleteConfirmationDialog());
        }
    }

    /**
     * Carga y rellena los campos del formulario con la información actual del usuario
     * almacenada en el objeto {@code currentUser}.
     */
    private void loadUserData() {
        if (currentUser == null) return;

        binding.etName.setText(currentUser.getName() != null ? currentUser.getName() : "");

        if (currentUser.getHeight() != null) {
            binding.etHeight.setText(String.valueOf(currentUser.getHeight().intValue()));
        }

        if (currentUser.getWeight() != null) {
            binding.etWeight.setText(String.valueOf(currentUser.getWeight()));
        }

        String existing = currentUser.getDisplayAvatar();
        if (existing != null && !existing.startsWith("http") && !existing.isEmpty()) {
            currentImageBase64 = existing;
        }

        ImageUtils.loadCircle(
                requireContext(),
                existing,
                binding.ivAvatar,
                R.drawable.ic_profile
        );

        if (currentUser.getGoal() != null) {
            binding.actvGoal.setText(currentUser.getGoal(), false);
        }
    }

    /**
     * Recoge los datos introducidos en el formulario, realiza las validaciones pertinentes
     * (campos vacíos, coincidencia de contraseñas, formato numérico) y solicita la actualización
     * del perfil a través del ViewModel.
     */
    private void submitChanges() {
        String name = Objects.requireNonNull(binding.etName.getText()).toString().trim();
        String heightStr = Objects.requireNonNull(binding.etHeight.getText()).toString().trim();
        String weightStr = Objects.requireNonNull(binding.etWeight.getText()).toString().trim();
        String goalStr = binding.actvGoal.getText().toString().trim();

        String newPasswordStr = binding.etNewPassword.getText() != null
                ? binding.etNewPassword.getText().toString().trim()
                : "";
        String confirmPasswordStr = binding.etConfirmPassword.getText() != null
                ? binding.etConfirmPassword.getText().toString().trim()
                : "";

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

        String photoData = resolvePhotoData();
        if (photoData == null && selectedLocalImageUri != null) {
            return;
        }
        updatedUser.setPhotoUrl(photoData != null ? photoData : currentUser.getPhotoUrl());


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

    /**
     * Establece los observadores sobre los LiveData del ViewModel para reaccionar ante los cambios de estado
     * (Cargando, Éxito, Error) tanto de la edición de perfil como de la eliminación de cuenta.
     *
     * @param prefs Repositorio de preferencias compartidas para gestionar tokens de sesión.
     */
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

                        Toast.makeText(getContext(),
                                "Contraseña actualizada. Inicia sesión de nuevo.",
                                Toast.LENGTH_LONG).show();

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

                    Toast.makeText(getContext(),
                            "Error: " + resource.message,
                            Toast.LENGTH_LONG).show();

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

                    binding.tilCurrentPassword.setError(
                            "Error al desactivar cuenta: " + resource.message
                    );

                    LiveData<Resource<String>> deleteStatus = viewModel.getDeleteAccountStatus();
                    if (deleteStatus instanceof MutableLiveData) {
                        ((MutableLiveData<Resource<String>>) deleteStatus).setValue(null);
                    }
                    break;
            }
        });
    }

    /**
     * Valida que el campo de contraseña actual no esté vacío y muestra un cuadro de diálogo personalizado
     * solicitando la confirmación definitiva para dar de baja y desactivar la cuenta del usuario.
     */
    private void showDeleteConfirmationDialog() {
        String currentPassword = binding.etCurrentPassword.getText() != null
                ? binding.etCurrentPassword.getText().toString().trim()
                : "";

        if (currentPassword.isEmpty()) {
            binding.tilCurrentPassword.setError(
                    "Introduce tu contraseña actual para desactivar la cuenta"
            );
            binding.etCurrentPassword.requestFocus();
            return;
        }

        binding.tilCurrentPassword.setError(null);

        DialogConfirmDeleteBinding dialogBinding =
                DialogConfirmDeleteBinding.inflate(getLayoutInflater());

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
                Toast.makeText(getContext(),
                        "Error: No se encontró el email",
                        Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    /**
     * Se ejecuta cuando la vista jerárquica del fragmento está siendo eliminada.
     * Limpia la referencia al objeto binding para evitar fugas de memoria (Memory Leaks).
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}