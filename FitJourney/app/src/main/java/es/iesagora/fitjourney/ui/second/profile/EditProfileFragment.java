package es.iesagora.fitjourney.ui.second.profile;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.android.material.chip.Chip;

import java.util.Objects;

import es.iesagora.fitjourney.R;
import es.iesagora.fitjourney.data.model.User;
import es.iesagora.fitjourney.data.repositories.PreferencesRepository;
import es.iesagora.fitjourney.databinding.FragmentEditProfileBinding;
import es.iesagora.fitjourney.ui.main.MainActivity;
import es.iesagora.fitjourney.utils.ImageUtils;
import es.iesagora.fitjourney.viewmodel.SecondViewModel;

/**
 * Fragment encargado de la edición del perfil del usuario.
 * Este fragment permite al usuario autenticado modificar sus datos personales
 * dentro de la aplicación, incluyendo nombre, altura, peso y objetivo fitness.
 * Además, gestiona el ciclo completo de edición del perfil:
 * - Carga de datos del usuario recibido por argumentos de navegación
 * - Visualización y edición de campos del formulario
 * - Selección de objetivos mediante Chips con feedback visual dinámico
 * - Validación básica de datos antes de enviarlos al servidor
 * - Comunicación con el {@link SecondViewModel} para actualizar el perfil
 * - Observación de estados (LOADING, SUCCESS, ERROR) tanto de actualización como de eliminación
 * - Gestión de desactivación de cuenta del usuario
 * - Redirección a la pantalla principal en caso de eliminación de cuenta
 */
public class EditProfileFragment extends Fragment {

    /**
     * ViewBinding asociado al layout del fragmento.
     * Permite acceder a las vistas del XML de forma segura sin usar findViewById.
     */
    private FragmentEditProfileBinding binding;

    /**
     * ViewModel compartido con la actividad secundaria.
     * Gestiona la lógica de negocio relacionada con el perfil del usuario.
     */
    private SecondViewModel viewModel;

    /**
     * Usuario actualmente cargado en el formulario de edición.
     * Contiene los datos originales antes de ser modificados.
     */
    private User currentUser;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentEditProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(SecondViewModel.class);
        PreferencesRepository prefs = new PreferencesRepository(requireContext());

        binding.toolbar.setNavigationOnClickListener(v ->
                Navigation.findNavController(v).navigateUp()
        );

        setupChips();

        if (getArguments() != null && getArguments().containsKey("user_data")) {

            currentUser = (User) getArguments().getSerializable("user_data");
            loadUserData();

        } else {

            Toast.makeText(
                    getContext(),
                    "Error al cargar datos",
                    Toast.LENGTH_SHORT
            ).show();

            Navigation.findNavController(view).navigateUp();
            return;
        }

        binding.btnSaveProfile.setOnClickListener(v -> submitChanges());

        binding.btnDeleteAccount.setOnClickListener(
                v -> showDeleteConfirmationDialog()
        );

        viewModel.getUpdateProfileStatus().observe(
                getViewLifecycleOwner(),
                resource -> {

                    if (resource == null) return;

                    switch (resource.status) {

                        case LOADING:
                            binding.progressBar.setVisibility(View.VISIBLE);
                            binding.btnSaveProfile.setEnabled(false);
                            break;

                        case SUCCESS:
                            binding.progressBar.setVisibility(View.GONE);
                            binding.btnSaveProfile.setEnabled(true);

                            Toast.makeText(
                                    getContext(),
                                    "¡Perfil actualizado con éxito!",
                                    Toast.LENGTH_SHORT
                            ).show();

                            String token = prefs.getToken();

                            if (token != null) {
                                viewModel.fetchProfile(token);
                            }

                            Navigation.findNavController(requireView())
                                    .navigateUp();
                            break;

                        case ERROR:
                            binding.progressBar.setVisibility(View.GONE);
                            binding.btnSaveProfile.setEnabled(true);

                            Toast.makeText(
                                    getContext(),
                                    "Error: " + resource.message,
                                    Toast.LENGTH_LONG
                            ).show();
                            break;
                    }
                });

        viewModel.getDeleteAccountStatus().observe(
                getViewLifecycleOwner(),
                resource -> {

                    if (resource == null) return;

                    switch (resource.status) {

                        case LOADING:
                            binding.progressBar.setVisibility(View.VISIBLE);
                            break;

                        case SUCCESS:
                            binding.progressBar.setVisibility(View.GONE);

                            Toast.makeText(
                                    getContext(),
                                    "Cuenta desactivada",
                                    Toast.LENGTH_LONG
                            ).show();

                            prefs.clearSession();

                            Intent intent = new Intent(
                                    requireActivity(),
                                    MainActivity.class
                            );

                            intent.setFlags(
                                    Intent.FLAG_ACTIVITY_NEW_TASK
                                            | Intent.FLAG_ACTIVITY_CLEAR_TASK
                            );

                            startActivity(intent);
                            break;

                        case ERROR:
                            binding.progressBar.setVisibility(View.GONE);

                            Toast.makeText(
                                    getContext(),
                                    "Error: " + resource.message,
                                    Toast.LENGTH_LONG
                            ).show();
                            break;
                    }
                });
    }

    /**
     * Configura el comportamiento visual de los Chips de objetivos.
     * Cambia el estilo del borde (stroke) según si están seleccionados o no,
     * proporcionando feedback visual al usuario sobre la selección actual.
     */
    private void setupChips() {

        int colorRosa = ContextCompat.getColor(
                requireContext(),
                R.color.mainrosa
        );

        int colorStroke = ContextCompat.getColor(
                requireContext(),
                R.color.stroke
        );

        binding.chipGroupGoal.setOnCheckedStateChangeListener(
                (group, checkedIds) -> {

                    for (int i = 0; i < group.getChildCount(); i++) {

                        Chip chip = (Chip) group.getChildAt(i);

                        if (chip.isChecked()) {

                            chip.setChipStrokeColor(
                                    ColorStateList.valueOf(colorRosa)
                            );

                            chip.setChipStrokeWidth(5f);

                        } else {

                            chip.setChipStrokeColor(
                                    ColorStateList.valueOf(colorStroke)
                            );

                            chip.setChipStrokeWidth(3f);
                        }
                    }
                });
    }

    /**
     * Carga los datos del usuario en los campos del formulario.
     * Se encarga de:
     * - Rellenar nombre, altura y peso
     * - Cargar avatar del usuario
     * - Seleccionar el chip de objetivo correspondiente
     */
    private void loadUserData() {

        if (currentUser == null) return;

        binding.etName.setText(
                currentUser.getName() != null
                        ? currentUser.getName()
                        : ""
        );

        if (currentUser.getHeight() != null) {

            binding.etHeight.setText(
                    String.valueOf(currentUser.getHeight().intValue())
            );
        }

        if (currentUser.getWeight() != null) {

            binding.etWeight.setText(
                    String.valueOf(currentUser.getWeight())
            );
        }

        ImageUtils.loadCircle(
                requireContext(),
                currentUser.getDisplayAvatar(),
                binding.ivAvatar,
                R.drawable.ic_profile
        );

        String goal = currentUser.getGoal() != null
                ? currentUser.getGoal().toLowerCase()
                : "";

        if (goal.contains("músculo") || goal.contains("muscle")) {

            binding.chipMuscle.setChecked(true);

        } else if (goal.contains("grasa") || goal.contains("fat")) {

            binding.chipFat.setChecked(true);

        } else if (goal.contains("fuerza") || goal.contains("strength")) {

            binding.chipStrength.setChecked(true);

        } else {

            binding.chipFit.setChecked(true);
        }
    }

    private void submitChanges() {

        String name = Objects.requireNonNull(
                binding.etName.getText()
        ).toString().trim();

        String heightStr = Objects.requireNonNull(
                binding.etHeight.getText()
        ).toString().trim();

        String weightStr = Objects.requireNonNull(
                binding.etWeight.getText()
        ).toString().trim();

        if (name.isEmpty()) {

            binding.tilName.setError(
                    "El nombre no puede estar vacío"
            );

            return;

        } else {

            binding.tilName.setError(null);
        }

        String goal = "Mantenerse en forma";

        if (binding.chipMuscle.isChecked()) {
            goal = "Ganar músculo";

        } else if (binding.chipFat.isChecked()) {
            goal = "Perder grasa";

        } else if (binding.chipStrength.isChecked()) {
            goal = "Fuerza";
        }

        User updatedUser = new User();

        updatedUser.setName(name);
        updatedUser.setGoal(goal);

        try {

            if (!heightStr.isEmpty()) {
                updatedUser.setHeight(
                        Double.parseDouble(heightStr)
                );
            }

            if (!weightStr.isEmpty()) {
                updatedUser.setWeight(
                        Double.parseDouble(weightStr)
                );
            }

        } catch (NumberFormatException e) {

            Toast.makeText(
                    getContext(),
                    "Revisa el peso o altura",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        viewModel.updateProfile(updatedUser);
    }

    /**
     * Muestra un diálogo de confirmación para desactivar la cuenta.
     */
    private void showDeleteConfirmationDialog() {

        AlertDialog.Builder builder =
                new AlertDialog.Builder(requireContext());

        builder.setTitle("¿Desactivar cuenta?");

        builder.setMessage(
                "Tu perfil se ocultará, pero tus datos permanecerán guardados. ¿Estás seguro?"
        );

        builder.setPositiveButton(
                "Desactivar",
                (dialog, which) -> {

                    User userDesactivado = new User();

                    userDesactivado.setActive(0);

                    viewModel.updateProfile(userDesactivado);
                });

        builder.setNegativeButton(
                "Cancelar",
                (dialog, which) -> dialog.cancel()
        );

        builder.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}