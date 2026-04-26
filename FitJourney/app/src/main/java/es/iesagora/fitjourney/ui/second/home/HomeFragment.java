package es.iesagora.fitjourney.ui.second.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import es.iesagora.fitjourney.R;
import es.iesagora.fitjourney.data.model.User;
import es.iesagora.fitjourney.data.model.Workout;
import es.iesagora.fitjourney.data.repositories.PreferencesRepository;
import es.iesagora.fitjourney.databinding.FragmentHomeBinding;
import es.iesagora.fitjourney.utils.FitnessCalculator;
import es.iesagora.fitjourney.viewmodel.SecondViewModel;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private SecondViewModel viewModel;
    private PreferencesRepository prefs;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(SecondViewModel.class);
        prefs = new PreferencesRepository(requireContext());

        setupHeader();
        setupNavigationButtons();

        String token = prefs.getToken();
        if (token != null) {
            viewModel.fetchProfile(token);
            viewModel.fetchWorkoutHistory();
        }

        viewModel.getProfile().observe(getViewLifecycleOwner(), resource -> {
            if (resource != null && resource.data != null && resource.data.getUser() != null) {
                populatePhysicalData(resource.data.getUser());
            }
        });

        viewModel.getWorkoutHistory().observe(getViewLifecycleOwner(), resource -> {
            if (resource != null && resource.data != null) {
                calculateStreakAndStats(resource.data);
            }
        });
    }

    private void setupHeader() {
        SimpleDateFormat sdf = new SimpleDateFormat("d 'DE' MMMM, yyyy", new Locale("es", "ES"));
        binding.tvDate.setText(sdf.format(new Date()).toUpperCase());
    }

    private void setupNavigationButtons() {
        View.OnClickListener goToFitnessCenter = v -> Navigation.findNavController(v).navigate(R.id.fitnessCenterFragment);

        binding.btnTrainNow.setOnClickListener(goToFitnessCenter);
        binding.btnCardio.setOnClickListener(goToFitnessCenter);
        binding.btnStrength.setOnClickListener(goToFitnessCenter);
        binding.btnStretching.setOnClickListener(goToFitnessCenter);
    }

    private void populatePhysicalData(User user) {
        String firstName = user.getName().contains(" ") ? user.getName().split(" ")[0] : user.getName();
        binding.tvGreeting.setText("Hola, " + firstName + " 👋");

        try {
            float peso = Float.parseFloat(String.valueOf(user.getWeight()));
            float alturaCm = Float.parseFloat(String.valueOf(user.getHeight()));
            int edad = FitnessCalculator.calculateAge(user.getBirth_date());

            float imc = FitnessCalculator.calculateBMI(peso, alturaCm);
            float porcentajeGrasa = FitnessCalculator.calculateBodyFat(imc, edad, user.getGender());
            float masaMagra = FitnessCalculator.calculateLeanMass(peso, porcentajeGrasa);
            String categoriaImc = FitnessCalculator.getBMICategory(imc);

            binding.tvBMI.setText(String.format(Locale.US, "%.1f", imc));
            binding.tvBMICategory.setText(categoriaImc);

            binding.tvFat.setText(String.format(Locale.US, "%.1f%%", porcentajeGrasa));
            binding.tvLeanMass.setText(String.format(Locale.US, "%.1f kg", masaMagra));

        } catch (Exception e) {
            binding.tvBMI.setText("--");
            binding.tvBMICategory.setText("--");
            binding.tvFat.setText("--%");
            binding.tvLeanMass.setText("-- kg");
        }
    }

    private void calculateStreakAndStats(List<Workout> history) {
        if (history == null || history.isEmpty()) {
            binding.tvStreakTitle.setText("🔥  0 Días de Racha");
            binding.tvStreakMessage.setText("Aún no has registrado entrenamientos. ¡Empieza hoy!");
            binding.tvPeakCal.setText("0 kcal");
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        List<Date> workoutDates = new ArrayList<>();
        double maxCalories = 0.0;

        for (Workout w : history) {
            if (w.getCalories_burned() != null && w.getCalories_burned() > maxCalories) {
                maxCalories = w.getCalories_burned();
            }

            try {
                if (w.getWorkout_date() != null) {
                    String dateStr = w.getWorkout_date().length() >= 10 ? w.getWorkout_date().substring(0, 10) : w.getWorkout_date();
                    Date d = sdf.parse(dateStr);
                    if (d != null && !workoutDates.contains(d)) workoutDates.add(d);
                }
            } catch (Exception ignored) {}
        }

        binding.tvPeakCal.setText(String.format(Locale.US, "%.0f kcal", maxCalories));

        Collections.sort(workoutDates, Collections.reverseOrder());
        if (workoutDates.isEmpty()) return;

        Calendar todayCal = Calendar.getInstance();
        todayCal.set(Calendar.HOUR_OF_DAY, 0); todayCal.set(Calendar.MINUTE, 0); todayCal.set(Calendar.SECOND, 0); todayCal.set(Calendar.MILLISECOND, 0);

        long diffInMillis = todayCal.getTime().getTime() - workoutDates.get(0).getTime();
        long daysSinceLastWorkout = TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS);

        int streak = 0;

        if (daysSinceLastWorkout == 0) {
            binding.tvStreakMessage.setText("¡Entrenaste hoy! Sigue así 🔥");
            streak = 1;
        } else if (daysSinceLastWorkout == 1) {
            binding.tvStreakMessage.setText("Ayer entrenaste. ¡No pierdas el ritmo!");
            streak = 1;
        } else if (daysSinceLastWorkout == 2) {
            binding.tvStreakMessage.setText("⚠️ Llevas 2 días sin entrenar. ¡Hoy es crucial!");
            streak = 1;
        } else {
            binding.tvStreakMessage.setText("Has perdido la racha. ¡Es hora de volver a empezar!");
            streak = 0;
        }

        if (streak > 0) {
            for (int i = 0; i < workoutDates.size() - 1; i++) {
                long diff = workoutDates.get(i).getTime() - workoutDates.get(i + 1).getTime();
                long daysBetween = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
                if (daysBetween <= 3) {
                    streak++;
                } else {
                    break;
                }
            }
        }

        binding.tvStreakTitle.setText("🔥  " + streak + " Días de Racha");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}