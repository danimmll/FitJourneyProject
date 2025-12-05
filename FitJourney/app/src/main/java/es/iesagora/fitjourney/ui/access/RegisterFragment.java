package es.iesagora.fitjourney.ui.access;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;

import java.util.Calendar;

import es.iesagora.fitjourney.databinding.FragmentRegisterBinding;

public class RegisterFragment extends Fragment {

    private FragmentRegisterBinding binding;
    private final int anioActual = Calendar.getInstance().get(Calendar.YEAR);
    private final int mesActual = Calendar.getInstance().get(Calendar.MONTH);
    private final int diaActual = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);

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

                    calendario.show();
                }
            });
        }

    }
}