package es.iesagora.fitjourney.ui.access;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.InputType;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Toast;

import java.util.Calendar;

import es.iesagora.fitjourney.R;
import es.iesagora.fitjourney.databinding.FragmentRegisterBinding;

public class RegisterFragment extends Fragment {

    private FragmentRegisterBinding binding;
    private final int anioActual = Calendar.getInstance().get(Calendar.YEAR);
    private final int mesActual = Calendar.getInstance().get(Calendar.MONTH);
    private final int diaActual = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
    private String[] genders;
    private String[] goals;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        genders = new String[]{getString(R.string.masculine),getString(R.string.feminine),
                getString(R.string.other)
        };

        goals = new String[]{getString(R.string.lose_weight), getString(R.string.maintain_weight),
                getString(R.string.gain_muscle),getString(R.string.lose_fat),
                getString(R.string.gain_strength),
                getString(R.string.gain_endurance),getString(R.string.gain_flexibility),
                getString(R.string.recover_from_injury)};

        establecerListas();

        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @SuppressLint({"DefaultLocale"})
            @Override
            public void onDateSet(DatePicker datePicker, int anio, int mes, int dia) {
                if (binding.etBirthDate != null) {
                    binding.etBirthDate.setText(String.format("%02d/%02d/%04d", dia, mes + 1, anio));
                }
            }
        };

        if (binding.etBirthDate != null) {

            binding.etBirthDate.setFocusable(false);
            binding.etBirthDate.setInputType(InputType.TYPE_NULL);

            binding.etBirthDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    DatePickerDialog calendario = new DatePickerDialog(
                            requireContext(),
                            R.style.calendarioPersonalizado,
                            dateSetListener,
                            anioActual,
                            mesActual,
                            diaActual
                    );


                    calendario.getDatePicker().setMaxDate(System.currentTimeMillis());

                    Calendar minDate = Calendar.getInstance();
                    Calendar maxDate = Calendar.getInstance();
                    minDate.add(Calendar.YEAR, -110);
                    maxDate.add(Calendar.YEAR, -16);
                    calendario.getDatePicker().setMinDate(minDate.getTimeInMillis());
                    calendario.getDatePicker().setMaxDate(maxDate.getTimeInMillis());

                    calendario.show();
                }
            });
        }

        binding.btnRegisterSubmit.setOnClickListener( v -> {
            if (validarDatos()) {

            }
        });

    }

    private void establecerListas() {

        ArrayAdapter<String> adapterGender = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_expandable_list_item_1,
                genders
        );

        if (binding.actvGender != null) {
            binding.actvGender.setAdapter(adapterGender);
        }

        ArrayAdapter<String> adapterGoals = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_expandable_list_item_1,
                goals
        );

        if (binding.actvGoal != null) {
            binding.actvGoal.setAdapter(adapterGoals);
        }
    }

    private boolean validarDatos() {

        boolean valido = true;


        if (binding.etName.getText().toString().trim().isEmpty()) {
            binding.tlName.setError("El nombre no puede estar vacío");
            valido = false;
        } else {
            binding.tlName.setError(null);
        }

        String email = binding.etEmail.getText().toString().trim();
        if (email.isEmpty()) {
            binding.tlEmail.setError("El email es obligatorio");
            valido = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tlEmail.setError("Formato de email no válido");
            valido = false;
        } else {
            binding.tlEmail.setError(null);
        }

        String pass = binding.etPassword.getText().toString().trim();

        if (pass.isEmpty()) {
            binding.tlPassword.setError("La contraseña es obligatoria");
            valido = false;

        } else if (pass.length() < 8) {
            binding.tlPassword.setError("Debe tener al menos 8 caracteres");
            valido = false;

        } else if (!pass.matches(".*[A-Z].*")) {
            binding.tlPassword.setError("Debe contener al menos una letra mayúscula");
            valido = false;

        } else if (!pass.matches(".*\\d.*")) {
            binding.tlPassword.setError("Debe contener al menos un número");
            valido = false;

        } else {
            binding.tlPassword.setError(null);
        }


        if (binding.etBirthDate.getText().toString().trim().isEmpty()) {
            binding.tlBirthDate.setError("Selecciona una fecha válida");
            valido = false;
        } else {
            binding.tlBirthDate.setError(null);
        }

        if (binding.actvGender.getText().toString().trim().isEmpty()) {
            binding.tlGender.setError("Selecciona un género");
            valido = false;
        } else {
            binding.tlGender.setError(null);
        }

        if (binding.actvGoal.getText().toString().trim().isEmpty()) {
            binding.tLGoal.setError("Selecciona un objetivo");
            valido = false;
        } else {
            binding.tLGoal.setError(null);
        }

        String altura = binding.etHeight.getText().toString().trim();
        if (altura.isEmpty()) {
            binding.tlHeight.setError("Introduce tu altura");
            valido = false;
        } else {
            try {
                int h = Integer.parseInt(altura);
                if (h < 50 || h > 250) {
                    binding.tlHeight.setError("Altura fuera de rango");
                    valido = false;
                } else {
                    binding.tlHeight.setError(null);
                }
            } catch (Exception e) {
                binding.tlHeight.setError("Dato no válido");
                valido = false;
            }
        }

        String peso = binding.etWeight.getText().toString().trim();
        if (peso.isEmpty()) {
            binding.tlWeight.setError("Introduce tu peso");
            valido = false;
        } else {
            try {
                float weight = Float.parseFloat(peso);
                if (weight < 20 || weight > 300) {
                    binding.tlWeight.setError("Peso fuera de rango");
                    valido = false;
                } else {
                    binding.tlWeight.setError(null);
                }
            } catch (Exception e) {
                binding.tlWeight.setError("Dato no válido");
                valido = false;
            }
        }

        return valido;
    }

}