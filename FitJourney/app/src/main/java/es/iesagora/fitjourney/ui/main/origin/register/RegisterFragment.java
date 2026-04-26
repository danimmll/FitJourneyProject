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
import java.util.UUID;

import es.iesagora.fitjourney.R;
import es.iesagora.fitjourney.data.model.User;
import es.iesagora.fitjourney.data.remote.Resource;
import es.iesagora.fitjourney.data.repositories.PreferencesRepository;
import es.iesagora.fitjourney.databinding.FragmentRegisterBinding;
import es.iesagora.fitjourney.ui.second.SecondActivity;
import es.iesagora.fitjourney.viewmodel.MainViewModel;

public class RegisterFragment extends Fragment {

    private FragmentRegisterBinding binding;
    private MainViewModel viewModel;
    private PreferencesRepository prefs;
    private static final String KEY_GENDER = "saved_gender";
    private static final String KEY_GOAL = "saved_goal";
    private static final String KEY_BIRTH = "saved_birth";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        prefs = new PreferencesRepository(requireContext());

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

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (binding != null) {
            outState.putString(KEY_GENDER, binding.actvGender.getText().toString());
            outState.putString(KEY_GOAL, binding.actvGoal.getText().toString());
            outState.putString(KEY_BIRTH, binding.etBirthDate.getText().toString());
        }
    }


    private void setupUI() {
        setupDropdowns();
        setupDatePicker();

        binding.tvGoToLogin.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.loginFragment);
        });
    }

    private void setupDropdowns() {
        String[] genders = {getString(R.string.masculine), getString(R.string.feminine), getString(R.string.other)};
        ArrayAdapter<String> adapterGender = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, genders);
        binding.actvGender.setAdapter(adapterGender);

        String[] goals = {getString(R.string.lose_weight), getString(R.string.maintain_weight),
                getString(R.string.gain_muscle), getString(R.string.lose_fat),
                getString(R.string.gain_strength), getString(R.string.gain_endurance),
                getString(R.string.gain_flexibility), getString(R.string.recover_from_injury)};
        ArrayAdapter<String> adapterGoals = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, goals);
        binding.actvGoal.setAdapter(adapterGoals);
    }

    private void setupDatePicker() {
        binding.etBirthDate.setInputType(InputType.TYPE_NULL);
        binding.etBirthDate.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            @SuppressLint("DefaultLocale")
            DatePickerDialog picker = new DatePickerDialog(requireContext(), R.style.calendarioPersonalizado,
                    (view, year, month, dayOfMonth) -> binding.etBirthDate.setText(String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year)),
                    cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));

            Calendar minDate = Calendar.getInstance(); minDate.add(Calendar.YEAR, -110);
            Calendar maxDate = Calendar.getInstance(); maxDate.add(Calendar.YEAR, -16);
            picker.getDatePicker().setMinDate(minDate.getTimeInMillis());
            picker.getDatePicker().setMaxDate(maxDate.getTimeInMillis());
            picker.show();
        });
    }


    private void setupObservers() {
        viewModel.getRegisterState().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;

            switch (resource.status) {
                case LOADING:
                    setLoadingState(true);
                    break;
                case SUCCESS:
                    setLoadingState(false);
                    Toast.makeText(requireContext(), R.string.registration_completed, Toast.LENGTH_SHORT).show();
                    prefs.setToken(resource.data.getToken());

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
                        Toast.makeText(getContext(), resource.message, Toast.LENGTH_LONG).show();
                    }
                    break;
            }
        });
    }

    private void setLoadingState(boolean isLoading) {
        binding.btnRegisterSubmit.setEnabled(!isLoading);
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    private void submitRegistration() {
        try {
            String name = binding.etName.getText().toString().trim();
            String email = binding.etEmail.getText().toString().trim();
            String pass = binding.etPassword.getText().toString().trim();
            String gender = binding.actvGender.getText().toString().trim();
            String goal = binding.actvGoal.getText().toString().trim();
            double height = Double.parseDouble(binding.etHeight.getText().toString().trim());
            double weight = Double.parseDouble(binding.etWeight.getText().toString().trim());

            String birthDate = formatToDatabaseDate(binding.etBirthDate.getText().toString().trim());

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
            android.util.Log.e("RegisterFragment", "Error al preparar los datos de registro: ", e);
            Toast.makeText(requireContext(), getString(R.string.error_processing_data) + ": " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private String formatToDatabaseDate(String visualDate) {
        String[] parts = visualDate.split("/");
        return parts[2] + "-" + parts[1] + "-" + parts[0];
    }

    private void showReactivationDialog() {
        String email = binding.etEmail.getText().toString().trim();
        String pass = binding.etPassword.getText().toString().trim();

        new AlertDialog.Builder(requireContext())
                .setTitle("Cuenta inactiva")
                .setMessage("¿Deseas reactivar tu cuenta anterior o borrarla definitivamente?")
                .setPositiveButton("Reactivar", (d, w) -> {
                    viewModel.reactivate(email, pass);
                })
                .setNegativeButton("Borrar todo", (d, w) -> {
                    viewModel.deletePhysical(email, pass);
                }).show();
    }

    private boolean isFormValid() {
        boolean isValid = validateName() & validateEmail() & validatePasswords() & validateDateAndSpinners() & validateBodyMetrics();
        return isValid;
    }

    private boolean validateName() {
        if (binding.etName.getText().toString().trim().isEmpty()) {
            binding.tlName.setError(getString(R.string.name_cannot_be_empty));
            return false;
        }
        binding.tlName.setError(null);
        return true;
    }

    private boolean validateEmail() {
        String email = binding.etEmail.getText().toString().trim();
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tlEmail.setError(getString(R.string.invalid_email_format));
            return false;
        }
        binding.tlEmail.setError(null);
        return true;
    }

    private boolean validatePasswords() {
        String pass = binding.etPassword.getText().toString().trim();
        String confirm = binding.etConfirmPassword.getText().toString().trim();
        boolean valid = true;

        if (pass.length() < 8 || !pass.matches(".*[A-Z].*") || !pass.matches(".*\\d.*")) {
            binding.tlPassword.setError("Mínimo 8 chars, 1 mayúscula y 1 número");
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

    private boolean validateDateAndSpinners() {
        boolean valid = true;
        if (binding.etBirthDate.getText().toString().trim().isEmpty()) {
            binding.tlBirthDate.setError("Requerido"); valid = false;
        } else binding.tlBirthDate.setError(null);

        if (binding.actvGender.getText().toString().trim().isEmpty()) {
            binding.tlGender.setError("Requerido"); valid = false;
        } else binding.tlGender.setError(null);

        if (binding.actvGoal.getText().toString().trim().isEmpty()) {
            binding.tLGoal.setError("Requerido"); valid = false;
        } else binding.tLGoal.setError(null);

        return valid;
    }

    private boolean validateBodyMetrics() {
        boolean valid = true;
        try {
            int h = Integer.parseInt(binding.etHeight.getText().toString().trim());
            if (h < 50 || h > 250) throw new Exception();
            binding.tlHeight.setError(null);
        } catch (Exception e) {
            binding.tlHeight.setError("Inválido"); valid = false;
        }

        try {
            float w = Float.parseFloat(binding.etWeight.getText().toString().trim());
            if (w < 20 || w > 300) throw new Exception();
            binding.tlWeight.setError(null);
        } catch (Exception e) {
            binding.tlWeight.setError("Inválido"); valid = false;
        }
        return valid;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}