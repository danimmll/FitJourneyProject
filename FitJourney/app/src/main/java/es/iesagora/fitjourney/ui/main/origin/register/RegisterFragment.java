package es.iesagora.fitjourney.ui.main.origin.register;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import java.util.Calendar;
import java.util.Objects;

import es.iesagora.fitjourney.R;
import es.iesagora.fitjourney.data.model.User;
import es.iesagora.fitjourney.databinding.FragmentRegisterBinding;
import es.iesagora.fitjourney.ui.second.SecondActivity;
import es.iesagora.fitjourney.viewmodel.MainViewModel;

/**
 * Fragment encargado del registro de nuevos usuarios.
 * Esta pantalla permite introducir todos los datos necesarios
 * para crear una cuenta dentro de la aplicación FitJourney.
 * Funcionalidades principales:
 * - Validación completa del formulario
 * - Selección de fecha de nacimiento
 * - Selección de género y objetivo físico
 * - Registro de usuario mediante ViewModel
 * - Reactivación de cuentas inactivas
 * - Navegación automática tras el registro
 */
public class RegisterFragment extends Fragment {

    /**
     * Binding asociado al layout del fragment.
     */
    private FragmentRegisterBinding binding;

    /**
     * ViewModel principal utilizado para gestionar
     * el flujo de autenticación y registro.
     */
    private MainViewModel viewModel;

    /**
     * Claves utilizadas para restaurar estado.
     */
    private static final String KEY_GENDER = "saved_gender";
    private static final String KEY_GOAL = "saved_goal";
    private static final String KEY_BIRTH = "saved_birth";

    /**
     * Texto reutilizable para errores de campos requeridos.
     */
    private static final String REQUERIDO_STRING = "Requerido";

    /**
     * Infla la vista del fragment.
     *
     * @param inflater inflater de Android.
     * @param container contenedor padre.
     * @param savedInstanceState estado guardado.
     * @return vista raíz del fragment.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * Inicializa el fragment una vez creada la vista.
     * Configura:
     * - ViewModel
     * - Dropdowns
     * - Selector de fecha
     * - Observadores
     * - Restauración de datos
     * - Botón de registro
     *
     * @param view vista del fragment.
     * @param savedInstanceState estado guardado.
     */
    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        setupUI();

        if (savedInstanceState != null) {
            binding.actvGender.setText(savedInstanceState.getString(KEY_GENDER, ""), false);
            binding.actvGoal.setText(savedInstanceState.getString(KEY_GOAL, ""), false);
            binding.etBirthDate.setText(savedInstanceState.getString(KEY_BIRTH, ""));
        }

        setupObservers();

        binding.btnRegisterSubmit.setOnClickListener(v -> {
            if (isFormValid()) {
                submitRegistration();
            }
        });
    }

    /**
     * Configura elementos generales de la interfaz.
     */
    private void setupUI() {
        setupDropdowns();
        setupDatePicker();

        binding.tvGoToLogin.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.loginFragment)
        );
    }

    /**
     * Configura los desplegables de género y objetivo.
     */
    private void setupDropdowns() {

        String[] genders = {
                getString(R.string.masculine),
                getString(R.string.feminine),
                getString(R.string.other)
        };

        ArrayAdapter<String> adapterGender =
                new ArrayAdapter<>(requireContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        genders);

        binding.actvGender.setAdapter(adapterGender);

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

        ArrayAdapter<String> adapterGoals =
                new ArrayAdapter<>(requireContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        goals);

        binding.actvGoal.setAdapter(adapterGoals);
    }

    /**
     * Configura el selector de fecha de nacimiento.
     * Limita la edad entre 16 y 110 años.
     */
    private void setupDatePicker() {

        binding.etBirthDate.setInputType(InputType.TYPE_NULL);

        binding.etBirthDate.setOnClickListener(v -> {

            Calendar cal = Calendar.getInstance();

            @SuppressLint("DefaultLocale")
            DatePickerDialog picker = new DatePickerDialog(
                    requireContext(),
                    R.style.calendarioPersonalizado,
                    (view, year, month, dayOfMonth) ->
                            binding.etBirthDate.setText(
                                    String.format("%02d/%02d/%04d",
                                            dayOfMonth,
                                            month + 1,
                                            year)
                            ),
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
            );

            Calendar minDate = Calendar.getInstance();
            minDate.add(Calendar.YEAR, -110);

            Calendar maxDate = Calendar.getInstance();
            maxDate.add(Calendar.YEAR, -16);

            picker.getDatePicker().setMinDate(minDate.getTimeInMillis());
            picker.getDatePicker().setMaxDate(maxDate.getTimeInMillis());

            picker.show();
        });
    }

    /**
     * Configura los observadores del estado de registro.
     */
    private void setupObservers() {

        viewModel.getRegisterState().observe(getViewLifecycleOwner(), resource -> {

            if (resource == null) return;

            switch (resource.status) {

                case LOADING:
                    setLoadingState(true);
                    break;

                case SUCCESS:
                    setLoadingState(false);
                    binding.tlEmail.setError(null);

                    Toast.makeText(requireContext(),
                            R.string.registration_completed,
                            Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(requireContext(), SecondActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK);

                    startActivity(intent);
                    requireActivity().finish();
                    viewModel.clearRegisterState();
                    break;

                case ERROR:
                    setLoadingState(false);

                    if ("PROMPT_REACTIVATION".equals(resource.message)) {
                        showReactivationDialog();
                    } else if ("ACCOUNT_DELETED_SUCCESS".equals(resource.message)) {
                        binding.tlEmail.setError(null);
                        Toast.makeText(requireContext(),
                                "Cuenta eliminada definitivamente. Ya puedes registrarte de nuevo.",
                                Toast.LENGTH_LONG).show();

                        Navigation.findNavController(requireView()).navigate(R.id.accessFragment);
                    } else {
                        binding.tlEmail.setError(getString(R.string.email_already_in_use));

                        Toast.makeText(getContext(),
                                resource.message,
                                Toast.LENGTH_LONG).show();
                    }
                    viewModel.clearRegisterState();
                    break;
            }
        });
    }

    /**
     * Activa o desactiva el estado de carga.
     *
     * @param isLoading true si se está procesando.
     */
    private void setLoadingState(boolean isLoading) {
        binding.btnRegisterSubmit.setEnabled(!isLoading);
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    /**
     * Envía el formulario de registro.
     */
    private void submitRegistration() {

        try {
            String name = Objects.requireNonNull(binding.etName.getText())
                    .toString()
                    .trim();

            String email = Objects.requireNonNull(binding.etEmail.getText())
                    .toString()
                    .trim();

            String pass = Objects.requireNonNull(binding.etPassword.getText())
                    .toString()
                    .trim();

            String gender = binding.actvGender.getText().toString().trim();
            String goal = binding.actvGoal.getText().toString().trim();

            double height = Double.parseDouble(
                    Objects.requireNonNull(binding.etHeight.getText())
                            .toString()
                            .trim()
            );

            double weight = Double.parseDouble(
                    Objects.requireNonNull(binding.etWeight.getText())
                            .toString()
                            .trim()
            );

            String birthDate = formatToDatabaseDate(
                    Objects.requireNonNull(binding.etBirthDate.getText())
                            .toString()
                            .trim()
            );

            User newUser = new User();
            newUser.setName(name);
            newUser.setEmail(email);
            newUser.setPassword(pass);
            newUser.setBirthDate(birthDate);
            newUser.setGender(gender);
            newUser.setGoal(goal);
            newUser.setHeight(height);
            newUser.setWeight(weight);

            viewModel.register(newUser);

        } catch (Exception e) {

            android.util.Log.e("RegisterFragment",
                    "Error al registrar usuario",
                    e);

            Toast.makeText(requireContext(),
                    getString(R.string.error_processing_data)
                            + ": " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Convierte una fecha visual dd/MM/yyyy
     * al formato yyyy-MM-dd.
     *
     * @param visualDate fecha visual.
     * @return fecha lista para base de datos.
     */
    private String formatToDatabaseDate(String visualDate) {

        String[] parts = visualDate.split("/");

        if (parts.length != 3) {
            throw new IllegalArgumentException("Formato de fecha inválido");
        }

        return parts[2] + "-" + parts[1] + "-" + parts[0];
    }

    /**
     * Muestra un diálogo para reactivar o eliminar
     * una cuenta previamente desactivada.
     */
    private void showReactivationDialog() {

        String email = Objects.requireNonNull(binding.etEmail.getText())
                .toString()
                .trim();

        String pass = Objects.requireNonNull(binding.etPassword.getText())
                .toString()
                .trim();

        new AlertDialog.Builder(requireContext())
                .setTitle("Cuenta inactiva")
                .setMessage("¿Deseas reactivar tu cuenta o borrarla definitivamente?")
                .setPositiveButton("Reactivar", (d, w) ->
                        viewModel.reactivate(email, pass)
                )
                .setNegativeButton("Borrar todo", (d, w) ->
                        viewModel.deletePhysical(email, pass)
                )
                .show();
    }

    /**
     * Valida el formulario completo.
     *
     * @return true si todos los datos son válidos.
     */
    private boolean isFormValid() {
        return validateName()
                && validateEmail()
                && validatePasswords()
                && validateDateAndSpinners()
                && validateBodyMetrics();
    }

    /**
     * Valida el nombre.
     */
    private boolean validateName() {

        if (Objects.requireNonNull(binding.etName.getText())
                .toString()
                .trim()
                .isEmpty()) {

            binding.tlName.setError(getString(R.string.name_cannot_be_empty));
            return false;
        }

        binding.tlName.setError(null);
        return true;
    }

    /**
     * Valida el email.
     */
    private boolean validateEmail() {

        String email = Objects.requireNonNull(binding.etEmail.getText())
                .toString()
                .trim();

        if (email.isEmpty() ||
                !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {

            binding.tlEmail.setError(getString(R.string.invalid_email_format));
            return false;
        }

        binding.tlEmail.setError(null);
        return true;
    }

    /**
     * Valida contraseña y confirmación.
     */
    private boolean validatePasswords() {

        String pass = Objects.requireNonNull(binding.etPassword.getText())
                .toString()
                .trim();

        String confirm = Objects.requireNonNull(binding.etConfirmPassword.getText())
                .toString()
                .trim();

        boolean valid = true;

        if (pass.length() < 8 ||
                !pass.matches(".*[A-Z].*") ||
                !pass.matches(".*\\d.*")) {

            binding.tlPassword.setError(
                    "Mínimo 8 caracteres, 1 mayúscula y 1 número"
            );

            valid = false;

        } else {
            binding.tlPassword.setError(null);
        }

        if (!confirm.equals(pass) || confirm.isEmpty()) {

            binding.tlConfirmPassword.setError(
                    getString(R.string.passwords_do_not_match)
            );

            valid = false;

        } else {
            binding.tlConfirmPassword.setError(null);
        }

        return valid;
    }

    /**
     * Valida fecha, género y objetivo.
     */
    private boolean validateDateAndSpinners() {

        boolean valid = true;

        if (Objects.requireNonNull(binding.etBirthDate.getText())
                .toString()
                .trim()
                .isEmpty()) {

            binding.tlBirthDate.setError(REQUERIDO_STRING);
            valid = false;

        } else {
            binding.tlBirthDate.setError(null);
        }

        if (binding.actvGender.getText().toString().trim().isEmpty()) {

            binding.tlGender.setError(REQUERIDO_STRING);
            valid = false;

        } else {
            binding.tlGender.setError(null);
        }

        if (binding.actvGoal.getText().toString().trim().isEmpty()) {

            binding.tLGoal.setError(REQUERIDO_STRING);
            valid = false;

        } else {
            binding.tLGoal.setError(null);
        }

        return valid;
    }

    /**
     * Valida altura y peso.
     */
    private boolean validateBodyMetrics() {

        boolean valid = true;

        try {
            double h = Double.parseDouble(
                    Objects.requireNonNull(binding.etHeight.getText())
                            .toString()
                            .trim()
            );

            if (h < 50 || h > 250) {
                throw new NumberFormatException();
            }

            binding.tlHeight.setError(null);

        } catch (Exception e) {
            binding.tlHeight.setError("Inválido");
            valid = false;
        }

        try {
            double w = Double.parseDouble(
                    Objects.requireNonNull(binding.etWeight.getText())
                            .toString()
                            .trim()
            );

            if (w < 20 || w > 300) {
                throw new NumberFormatException();
            }

            binding.tlWeight.setError(null);

        } catch (Exception e) {
            binding.tlWeight.setError("Inválido");
            valid = false;
        }

        return valid;
    }

    /**
     * Libera el binding para evitar fugas de memoria.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
