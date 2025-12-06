package es.iesagora.fitjourney.ui.access;

import android.annotation.SuppressLint;
import android.content.Context;
import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.text.InputType;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Toast;

import java.util.Calendar;
import java.util.UUID;

import es.iesagora.fitjourney.R;
import es.iesagora.fitjourney.data.model.User;
import es.iesagora.fitjourney.databinding.FragmentRegisterBinding;
import es.iesagora.fitjourney.viewmodel.UserViewModel;

public class RegisterFragment extends Fragment {

    private FragmentRegisterBinding binding;
    private NavController navController;
    private UserViewModel viewModel;
    private final int anioActual = Calendar.getInstance().get(Calendar.YEAR);
    private final int mesActual = Calendar.getInstance().get(Calendar.MONTH);
    private final int diaActual = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
    private String[] genders;
    private String[] goals;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(UserViewModel.class);
        navController = Navigation.findNavController(view);

        viewModel.getRegisterState().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;

            switch (resource.status) {
                case LOADING:
                    binding.btnRegisterSubmit.setEnabled(false);
                    break;
                case SUCCESS:
                    binding.btnRegisterSubmit.setEnabled(true);
                    if (resource.data != null && resource.data.getToken() != null) {
                        requireContext().getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE)
                                .edit()
                                .putString("TOKEN", resource.data.getToken())
                                .apply();

                        Toast.makeText(requireContext(), R.string.registration_completed, Toast.LENGTH_SHORT).show();
                        navController.navigate(R.id.action_registerFragment_to_homeFragment);
                    }
                    break;
                case ERROR:
                    binding.btnRegisterSubmit.setEnabled(true);
                    binding.tlEmail.setError(getString(R.string.email_already_in_use));
                    break;
            }
        });

        genders = new String[]{getString(R.string.masculine), getString(R.string.feminine),
                getString(R.string.other)};
        goals = new String[]{getString(R.string.lose_weight), getString(R.string.maintain_weight),
                getString(R.string.gain_muscle), getString(R.string.lose_fat),
                getString(R.string.gain_strength), getString(R.string.gain_endurance),
                getString(R.string.gain_flexibility), getString(R.string.recover_from_injury)};
        establecerListas();

        DatePickerDialog.OnDateSetListener dateSetListener = (datePicker, anio, mes, dia) ->
                binding.etBirthDate.setText(String.format("%02d/%02d/%04d", dia, mes + 1, anio));

        binding.etBirthDate.setFocusable(false);
        binding.etBirthDate.setInputType(InputType.TYPE_NULL);
        binding.etBirthDate.setOnClickListener(v -> {
            DatePickerDialog calendario = new DatePickerDialog(
                    requireContext(),
                    R.style.calendarioPersonalizado,
                    dateSetListener,
                    anioActual,
                    mesActual,
                    diaActual
            );
            Calendar minDate = Calendar.getInstance();
            Calendar maxDate = Calendar.getInstance();
            minDate.add(Calendar.YEAR, -110);
            maxDate.add(Calendar.YEAR, -16);
            calendario.getDatePicker().setMinDate(minDate.getTimeInMillis());
            calendario.getDatePicker().setMaxDate(maxDate.getTimeInMillis());
            calendario.show();
        });

        binding.btnRegisterSubmit.setOnClickListener(v -> {
            if (validarDatos()) {
                realizarRegistro();
            }
        });
    }

    private void establecerListas() {
        ArrayAdapter<String> adapterGender = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_expandable_list_item_1, genders);
        binding.actvGender.setAdapter(adapterGender);

        ArrayAdapter<String> adapterGoals = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_expandable_list_item_1, goals);
        binding.actvGoal.setAdapter(adapterGoals);
    }

    private void realizarRegistro() {
        try {
            String name = binding.etName.getText().toString().trim();
            String email = binding.etEmail.getText().toString().trim();
            String pass = binding.etPassword.getText().toString().trim();
            String gender = binding.actvGender.getText().toString().trim();
            String goal = binding.actvGoal.getText().toString().trim();
            int height = Integer.parseInt(binding.etHeight.getText().toString().trim());
            float weight = Float.parseFloat(binding.etWeight.getText().toString().trim());
            String idUser = UUID.randomUUID().toString();
            String birthDate = convertirFecha(binding.etBirthDate.getText().toString().trim());

            User newUser = new User(idUser, name, email, pass, birthDate, gender, goal, height, weight);

            viewModel.register(newUser);

        } catch (Exception e) {
            Toast.makeText(requireContext(), getString(R.string.error_processing_data) + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private String convertirFecha(String fechaVisual) {
        try {
            String[] partes = fechaVisual.split("/");
            return partes[2] + "-" + partes[1] + "-" + partes[0];
        } catch (Exception e) {
            Toast.makeText(requireContext(), getString(R.string.error_processing_date) + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
            return fechaVisual;
        }
    }

    private boolean validarDatos() {
        boolean valido = true;

        if (binding.etName.getText().toString().trim().isEmpty()) {
            binding.tlName.setError(getString(R.string.name_cannot_be_empty));
            valido = false;
        } else {
            binding.tlName.setError(null);
        }

        String email = binding.etEmail.getText().toString().trim();
        if (email.isEmpty()) {
            binding.tlEmail.setError(getString(R.string.email_is_required));
            valido = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tlEmail.setError(getString(R.string.invalid_email_format));
            valido = false;
        } else {
            binding.tlEmail.setError(null);
        }

        String pass = binding.etPassword.getText().toString().trim();
        if (pass.isEmpty()) {
            binding.tlPassword.setError(getString(R.string.password_is_required));
            valido = false;
        } else if (pass.length() < 8) {
            binding.tlPassword.setError(getString(R.string.password_must_be_at_least_8_characters));
            valido = false;
        } else if (!pass.matches(".*[A-Z].*")) {
            binding.tlPassword.setError(getString(R.string.password_must_contain_at_least_one_uppercase_letter));
            valido = false;
        } else if (!pass.matches(".*\\d.*")) {
            binding.tlPassword.setError(getString(R.string.password_must_contain_at_least_one_number));
            valido = false;
        } else {
            binding.tlPassword.setError(null);
        }

        String repeatPass = binding.etConfirmPassword.getText().toString().trim();
        if (repeatPass.isEmpty()) {
            binding.tlConfirmPassword.setError(getString(R.string.repeat_password_required));
            valido = false;
        } else if (!repeatPass.equals(pass)) {
            binding.tlConfirmPassword.setError(getString(R.string.passwords_do_not_match));
            valido = false;
        } else {
            binding.tlConfirmPassword.setError(null);
        }

        if (binding.etBirthDate.getText().toString().trim().isEmpty()) {
            binding.tlBirthDate.setError(getString(R.string.select_a_valid_date));
            valido = false;
        } else {
            binding.tlBirthDate.setError(null);
        }

        if (binding.actvGender.getText().toString().trim().isEmpty()) {
            binding.tlGender.setError(getString(R.string.select_a_gender));
            valido = false;
        } else {
            binding.tlGender.setError(null);
        }

        if (binding.actvGoal.getText().toString().trim().isEmpty()) {
            binding.tLGoal.setError(getString(R.string.select_a_goal));
            valido = false;
        } else {
            binding.tLGoal.setError(null);
        }

        String altura = binding.etHeight.getText().toString().trim();
        if (altura.isEmpty()) {
            binding.tlHeight.setError(getString(R.string.enter_your_height));
            valido = false;
        } else {
            try {
                int h = Integer.parseInt(altura);
                if (h < 50 || h > 250) {
                    binding.tlHeight.setError(getString(R.string.height_out_of_range));
                    valido = false;
                } else {
                    binding.tlHeight.setError(null);
                }
            } catch (Exception e) {
                binding.tlHeight.setError(getString(R.string.invalid_value));
                valido = false;
            }
        }

        String peso = binding.etWeight.getText().toString().trim();
        if (peso.isEmpty()) {
            binding.tlWeight.setError(getString(R.string.enter_your_weight));
            valido = false;
        } else {
            try {
                float weight = Float.parseFloat(peso);
                if (weight < 20 || weight > 300) {
                    binding.tlWeight.setError(getString(R.string.weight_out_of_range));
                    valido = false;
                } else {
                    binding.tlWeight.setError(null);
                }
            } catch (Exception e) {
                binding.tlWeight.setError(getString(R.string.invalid_value));
                valido = false;
            }
        }

        return valido;
    }

}
