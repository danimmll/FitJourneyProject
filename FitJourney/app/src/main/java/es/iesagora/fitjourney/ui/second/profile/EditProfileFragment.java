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
import es.iesagora.fitjourney.databinding.FragmentEditProfileBinding;
import es.iesagora.fitjourney.ui.main.MainActivity;
import es.iesagora.fitjourney.utils.ImageUtils;
import es.iesagora.fitjourney.viewmodel.SecondViewModel;

/**
 * Fragment encargado de la edición del perfil del usuario.
 * Este fragment permite al usuario autenticado modificar sus datos personales
 * dentro de la aplicación, incluyendo nombre, contraseña, altura, peso, objetivo fitness y foto.
 * * Gestiona el modo Administrador:
 * Si un admin edita a otro usuario, se ocultan opciones de seguridad y
 * se utiliza un endpoint específico de actualización.
 * * Incorpora la gestión dinámica de objetivos basada en el catálogo global de la aplicación,
 * sincronizando las opciones de edición con las disponibles durante el registro inicial.
 */
public class EditProfileFragment extends Fragment {

    private FragmentEditProfileBinding binding;
    private SecondViewModel viewModel;
    private User currentUser;

    private static final String USERDATA_STRING = "user_data";

    private boolean isAdminEdit = false;

    private Uri selectedLocalImageUri = null;
    private String currentImageBase64 = null;

    /**
     * Lanzador para abrir la galería y seleccionar una foto.
     * Al recibir un resultado, prioriza la imagen local y limpia el campo de URL.
     */
    private final ActivityResultLauncher<String> getContent =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedLocalImageUri = uri;
                    currentImageBase64 = null;
                    binding.etPhotoUrl.setText("");

                    ImageUtils.loadCircle(requireContext(), uri.toString(), binding.ivAvatar, R.drawable.ic_profile);
                }
            });

    /**
     * Infla la vista del fragmento de edición de perfil.
     *
     * @param inflater           Instancia de LayoutInflater para inflar la vista.
     * @param container          Vista padre a la que se adjuntará el fragment.
     * @param savedInstanceState Estado previamente guardado.
     * @return La vista raíz del fragment.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEditProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * Se llama después de crear la vista. Inicializa ViewModel, preferencias,
     * evalúa el rol del usuario que accede y configura la interfaz y los observadores.
     *
     * @param view               La vista creada.
     * @param savedInstanceState Estado previamente guardado.
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
        setupListeners();
        setupObservers(prefs);
    }

    /**
     * Configura el menú desplegable de objetivos utilizando la misma estructura
     * del proceso de registro, garantizando la consistencia en toda la plataforma.
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
     * Ajusta la visibilidad de ciertos campos dependiendo de si es un usuario normal o un administrador.
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
     * Configura los listeners de los botones, la imagen y la gestión del foco en la URL.
     */
    private void setupListeners() {
        binding.ivAvatar.setOnClickListener(v -> getContent.launch("image/*"));
        binding.btnSelectImage.setOnClickListener(v -> getContent.launch("image/*"));

        binding.etPhotoUrl.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String url = binding.etPhotoUrl.getText() != null ? binding.etPhotoUrl.getText().toString().trim() : "";
                if (!url.isEmpty()) {
                    selectedLocalImageUri = null;
                    currentImageBase64 = null;
                    ImageUtils.loadCircle(requireContext(), url, binding.ivAvatar, R.drawable.ic_profile);
                }
            }
        });

        binding.btnSaveProfile.setOnClickListener(v -> submitChanges());

        if (!isAdminEdit) {
            binding.btnDeleteAccount.setOnClickListener(v -> showDeleteConfirmationDialog());
        }
    }

    /**
     * Carga los datos del usuario en los campos del formulario.
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

        String existingImage = currentUser.getPhotoUrl() != null ?
                currentUser.getPhotoUrl() : currentUser.getDisplayAvatar();
        if (existingImage != null && !existingImage.isEmpty()) {
            if (existingImage.startsWith("http")) {
                binding.etPhotoUrl.setText(existingImage);
            } else {
                currentImageBase64 = existingImage;
            }
        }

        ImageUtils.loadCircle(requireContext(), currentUser.getDisplayAvatar(), binding.ivAvatar, R.drawable.ic_profile);

        if (currentUser.getGoal() != null) {
            binding.actvGoal.setText(currentUser.getGoal(), false);
        }
    }

    /**
     * Recoge los datos, los valida, procesa la imagen final y decide qué endpoint llamar.
     */
    private void submitChanges() {
        String name = Objects.requireNonNull(binding.etName.getText()).toString().trim();
        String heightStr = Objects.requireNonNull(binding.etHeight.getText()).toString().trim();
        String weightStr = Objects.requireNonNull(binding.etWeight.getText()).toString().trim();
        String photoUrlStr = Objects.requireNonNull(binding.etPhotoUrl.getText()).toString().trim();

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

        String finalPhotoData;
        if (selectedLocalImageUri != null) {
            finalPhotoData = ImageUtils.uriToBase64(requireContext().getContentResolver(), selectedLocalImageUri);
            if (finalPhotoData == null) {
                Toast.makeText(getContext(), "Error al procesar imagen de galería", Toast.LENGTH_SHORT).show();
                return;
            }
        } else if (!photoUrlStr.isEmpty()) {
            finalPhotoData = photoUrlStr;
        } else {
            finalPhotoData = currentImageBase64;
        }

        User updatedUser = new User();
        updatedUser.setName(name);

        if (!goalStr.isEmpty()) {
            updatedUser.setGoal(goalStr);
        }

        updatedUser.setPhotoUrl(finalPhotoData);

        if (!isAdminEdit && !newPasswordStr.isEmpty()) {
            if (!newPasswordStr.equals(confirmPasswordStr)) {
                binding.tilConfirmPassword.setError("Las contraseñas no coinciden");
                return;
            }
            binding.tilConfirmPassword.setError(null);
            updatedUser.setPassword(newPasswordStr);
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
     * Configura los observadores para las respuestas de red relacionadas
     * con la actualización del perfil y la desactivación de cuenta.
     * * @param prefs Gestor de preferencias para operaciones de sesión.
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
                    break;
                case SUCCESS:
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Cuenta desactivada", Toast.LENGTH_LONG).show();

                    prefs.clearSession();
                    Intent intent = new Intent(requireActivity(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    break;
                case ERROR:
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Error: " + resource.message, Toast.LENGTH_LONG).show();
                    break;
            }
        });
    }

    /**
     * Muestra un cuadro de diálogo para confirmar la intención del
     * usuario de desactivar su propia cuenta.
     */
    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("¿Desactivar cuenta?");
        builder.setMessage("Tu perfil se ocultará, pero tus datos permanecerán guardados. ¿Estás seguro?");
        builder.setPositiveButton("Desactivar", (dialog, which) -> {
            User userDesactivado = new User();
            userDesactivado.setActive(0);
            viewModel.updateProfile(userDesactivado);
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    /**
     * Libera las referencias a las vistas encoladas al destruir
     * la interfaz del fragmento.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}