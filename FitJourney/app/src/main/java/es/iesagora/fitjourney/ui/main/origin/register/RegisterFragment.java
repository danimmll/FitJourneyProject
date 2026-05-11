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
import es.iesagora.fitjourney.data.repositories.PreferencesRepository;
import es.iesagora.fitjourney.databinding.FragmentRegisterBinding;
import es.iesagora.fitjourney.ui.second.SecondActivity;
import es.iesagora.fitjourney.viewmodel.MainViewModel;

/**
 * Fragment de registro de usuario.
 * Este fragmento gestiona todo el flujo de creación de cuentas dentro de la aplicación:
 * incluye la recogida de datos personales, validación del formulario, selección de
 * género, objetivo y fecha de nacimiento, así como la creación del usuario en el sistema.
 * Además, se encarga de:
 * - Observar el estado del registro a través del {@link MainViewModel}
 * - Mostrar estados de carga, éxito y error
 * - Gestionar la reactivación de cuentas inactivas o su eliminación definitiva
 * - Navegar a la pantalla principal tras un registro exitoso
 */
public class RegisterFragment extends Fragment {

    /**
     * ViewBinding asociado al layout del fragment.
     * Permite acceder de forma segura a las vistas del XML sin usar findViewById.
     */
    private FragmentRegisterBinding binding;

    /**
     * ViewModel compartido con la actividad.
     * Encargado de gestionar la lógica de registro y comunicación con los repositorios.
     */
    private MainViewModel viewModel;

    /**
     * Clave usada para guardar/restaurar el valor del género durante cambios de configuración.
     */
    private static final String KEY_GENDER = "saved_gender";

    /**
     * Clave usada para guardar/restaurar el objetivo del usuario durante cambios de configuración.
     */
    private static final String KEY_GOAL = "saved_goal";

    /**
     * Clave usada para guardar/restaurar la fecha de nacimiento durante cambios de configuración.
     */
    private static final String KEY_BIRTH = "saved_birth";

    private static final String REQUERIDO_STRING = "Requerido";

    /**
     * Infla la vista del fragmento de registro.
     *
     * @param inflater objeto utilizado para inflar el layout XML
     * @param container contenedor padre donde se insertará la vista
     * @param savedInstanceState estado guardado si el fragmento se recrea
     * @return vista raíz del fragmento
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * Inicializa la interfaz de usuario, observers y lógica del formulario.
     * Aquí se configuran:
     * - El ViewModel
     * - Las preferencias locales
     * - Restauración de estado
     * - Observadores del registro
     * - Acción del botón de registro
     *
     * @param view vista raíz del fragmento
     * @param savedInstanceState estado previo del fragmento
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
     * Configura la interfaz general del formulario.
     * Incluye la configuración de dropdowns, selector de fecha y navegación al login.
     */
    private void setupUI() {
        setupDropdowns();
        setupDatePicker();

        binding.tvGoToLogin.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.loginFragment)
        );
    }

    /**
     * Configura los menús desplegables de género y objetivo del usuario.
     * Los valores se cargan desde recursos string para soporte multi-idioma.
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
     * Restringe la selección a usuarios entre 16 y 110 años
     * para garantizar coherencia en los datos.
     */
    private void setupDatePicker() {
        binding.etBirthDate.setInputType(InputType.TYPE_NULL);

        binding.etBirthDate.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();

            @SuppressLint("DefaultLocale") DatePickerDialog picker = new DatePickerDialog(
                    requireContext(),
                    R.style.calendarioPersonalizado,
                    (view, year, month, dayOfMonth) ->
                            binding.etBirthDate.setText(
                                    String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year)
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
     * Observa el estado del registro desde el ViewModel.
     * Gestiona tres estados principales:
     * - LOADING: muestra progreso
     * - SUCCESS: guarda token y navega a la pantalla principal
     * - ERROR: muestra errores o diálogo de reactivación
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

                    Toast.makeText(requireContext(),
                            R.string.registration_completed,
                            Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(requireContext(), SecondActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    requireActivity().finish();
                    break;

                case ERROR:
                    setLoadingState(false);

                    if ("PROMPT_REACTIVATION".equals(resource.message)) {
                        showReactivationDialog();
                    } else {
                        binding.tlEmail.setError(getString(R.string.email_already_in_use));
                        Toast.makeText(getContext(),
                                resource.message,
                                Toast.LENGTH_LONG).show();
                    }
                    break;
            }
        });
    }

    /**
     * Activa o desactiva el estado de carga del formulario.
     *
     * @param isLoading true si se está procesando el registro
     */
    private void setLoadingState(boolean isLoading) {
        binding.btnRegisterSubmit.setEnabled(!isLoading);
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    /**
     * Recoge los datos introducidos por el usuario y crea el objeto User
     * para enviarlo al ViewModel.
     * Captura errores de conversión o campos inválidos.
     */
    private void submitRegistration() {
        try {
            String name = Objects.requireNonNull(binding.etName.getText()).toString().trim();
            String email = Objects.requireNonNull(binding.etEmail.getText()).toString().trim();
            String pass = Objects.requireNonNull(binding.etPassword.getText()).toString().trim();
            String gender = binding.actvGender.getText().toString().trim();
            String goal = binding.actvGoal.getText().toString().trim();

            double height = Double.parseDouble(Objects.requireNonNull(binding.etHeight.getText()).toString().trim());
            double weight = Double.parseDouble(Objects.requireNonNull(binding.etWeight.getText()).toString().trim());

            String birthDate = formatToDatabaseDate(
                    Objects.requireNonNull(binding.etBirthDate.getText()).toString().trim()
            );

            User newUser = new User();
            newUser.setName(name);
            newUser.setEmail(email);
            newUser.setPassword(pass);
            newUser.setBirth_date(birthDate);
            newUser.setGender(gender);
            newUser.setGoal(goal);
            newUser.setHeight(height);
            newUser.setWeight(weight);

            viewModel.register(newUser);

        } catch (Exception e) {
            android.util.Log.e("RegisterFragment", "Error al registrar usuario", e);

            Toast.makeText(requireContext(),
                    getString(R.string.error_processing_data) + ": " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Convierte una fecha de dd/MM/yyyy a yyyy-MM-dd.
     *
     * @param visualDate fecha en formato visual
     * @return fecha en formato de base de datos
     */
    private String formatToDatabaseDate(String visualDate) {
        String[] parts = visualDate.split("/");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Formato de fecha inválido");
        }
        return parts[2] + "-" + parts[1] + "-" + parts[0];
    }

    /**
     * Muestra un diálogo de reactivación o eliminación de cuenta.
     * Permite al usuario recuperar una cuenta inactiva o borrarla definitivamente.
     */
    private void showReactivationDialog() {
        String email = Objects.requireNonNull(binding.etEmail.getText()).toString().trim();
        String pass = Objects.requireNonNull(binding.etPassword.getText()).toString().trim();

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
     * Valida todos los campos del formulario.
     *
     * @return true si todos los campos son correctos
     */
    private boolean isFormValid() {
        return validateName()
                && validateEmail()
                && validatePasswords()
                && validateDateAndSpinners()
                && validateBodyMetrics();
    }

    /**
     * Valida que el nombre no esté vacío.
     */
    private boolean validateName() {
        if (Objects.requireNonNull(binding.etName.getText()).toString().trim().isEmpty()) {
            binding.tlName.setError(getString(R.string.name_cannot_be_empty));
            return false;
        }
        binding.tlName.setError(null);
        return true;
    }

    /**
     * Valida el formato del email.
     */
    private boolean validateEmail() {
        String email = Objects.requireNonNull(binding.etEmail.getText()).toString().trim();

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
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
        String pass = Objects.requireNonNull(binding.etPassword.getText()).toString().trim();
        String confirm = Objects.requireNonNull(binding.etConfirmPassword.getText()).toString().trim();
        boolean valid = true;

        if (pass.length() < 8 || !pass.matches(".*[A-Z].*") || !pass.matches(".*\\d.*")) {
            binding.tlPassword.setError("Mínimo 8 caracteres, 1 mayúscula y 1 número");
            valid = false;
        } else {
            binding.tlPassword.setError(null);
        }

        if (!confirm.equals(pass) || confirm.isEmpty()) {
            binding.tlConfirmPassword.setError(getString(R.string.passwords_do_not_match));
            valid = false;
        } else {
            binding.tlConfirmPassword.setError(null);
        }

        return valid;
    }

    /**
     * Valida fecha de nacimiento, género y objetivo.
     */
    private boolean validateDateAndSpinners() {
        boolean valid = true;

        if (Objects.requireNonNull(binding.etBirthDate.getText()).toString().trim().isEmpty()) {
            binding.tlBirthDate.setError(REQUERIDO_STRING);
            valid = false;
        } else binding.tlBirthDate.setError(null);

        if (binding.actvGender.getText().toString().trim().isEmpty()) {
            binding.tlGender.setError(REQUERIDO_STRING);
            valid = false;
        } else binding.tlGender.setError(null);

        if (binding.actvGoal.getText().toString().trim().isEmpty()) {
            binding.tLGoal.setError(REQUERIDO_STRING);
            valid = false;
        } else binding.tLGoal.setError(null);

        return valid;
    }

    /**
     * Valida altura y peso del usuario.
     */
    private boolean validateBodyMetrics() {
        boolean valid = true;

        try {
            int h = Integer.parseInt(Objects.requireNonNull(binding.etHeight.getText()).toString().trim());
            if (h < 50 || h > 250) throw new Exception();
            binding.tlHeight.setError(null);
        } catch (Exception e) {
            binding.tlHeight.setError("Inválido");
            valid = false;
        }

        try {
            float w = Float.parseFloat(Objects.requireNonNull(binding.etWeight.getText()).toString().trim());
            if (w < 20 || w > 300) {
                throw new Exception();
            }
            assert binding.tlWeight != null;
            binding.tlWeight.setError(null);
        } catch (Exception e) {
            assert binding.tlWeight != null;
            binding.tlWeight.setError("Inválido");
            valid = false;
        }

        return valid;
    }

    /**
     * Libera recursos del binding para evitar memory leaks.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}