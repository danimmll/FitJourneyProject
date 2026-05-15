package es.iesagora.fitjourney.ui.second.home;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
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
    private User currentUser;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SecondViewModel viewModel = new ViewModelProvider(requireActivity()).get(SecondViewModel.class);
        PreferencesRepository prefs = new PreferencesRepository(requireContext());

        setupHeader();
        setupNavigationButtons();

        String token = prefs.getToken();
        if (token != null) {
            viewModel.fetchProfile(token);
            viewModel.fetchWorkoutHistory();
        }

        viewModel.getProfile().observe(getViewLifecycleOwner(), resource -> {
            if (resource != null && resource.data != null && resource.data.getUser() != null) {
                currentUser = resource.data.getUser();
                populatePhysicalData(currentUser);
            }
        });

        viewModel.getWorkoutHistory().observe(getViewLifecycleOwner(), resource -> {
            if (resource != null && resource.data != null) {
                calculateStreakAndStats(resource.data);
                calculateWeeklyActivity(resource.data);
            }
        });
    }

    private void setupHeader() {
        SimpleDateFormat sdf = new SimpleDateFormat("d 'DE' MMMM, yyyy", new Locale("es", "ES"));
        binding.tvDate.setText(sdf.format(new Date()).toUpperCase());
    }

    private void setupNavigationButtons() {
        View.OnClickListener goToFitnessCenter =
                v -> Navigation.findNavController(v).navigate(R.id.fitnessCenterFragment);

        binding.btnTrainNow.setOnClickListener(goToFitnessCenter);
        binding.btnCardio.setOnClickListener(goToFitnessCenter);
        binding.btnStrength.setOnClickListener(goToFitnessCenter);
        binding.btnStretching.setOnClickListener(goToFitnessCenter);
    }

    @SuppressLint("SetTextI18n")
    private void populatePhysicalData(User user) {
        String firstName = user.getName().contains(" ") ? user.getName().split(" ")[0] : user.getName();
        binding.tvGreeting.setText("Hola, " + firstName + " 👋");

        try {
            float peso = Float.parseFloat(String.valueOf(user.getWeight()));
            float alturaCm = Float.parseFloat(String.valueOf(user.getHeight()));
            int edad = FitnessCalculator.calculateAge(user.getBirthDate());

            float imc = FitnessCalculator.calculateBMI(peso, alturaCm);
            float porcentajeGrasa = FitnessCalculator.calculateBodyFat(imc, edad, user.getGender(), user.getGoal());
            float masaMagra = FitnessCalculator.calculateLeanMass(peso, porcentajeGrasa);
            String categoriaImc = FitnessCalculator.getBMICategory(imc, user.getGoal());

            binding.tvBMI.setText(String.format(Locale.US, "%.1f", imc));
            binding.tvBMICategory.setText(categoriaImc);
            binding.tvFat.setText(String.format(Locale.US, "%.1f%%", porcentajeGrasa));
            binding.tvLeanMass.setText(String.format(Locale.US, "%.1f kg", masaMagra));

        } catch (Exception e) {
            resetPhysicalDataUI();
        }
    }

    @SuppressLint("SetTextI18n")
    private void resetPhysicalDataUI() {
        binding.tvBMI.setText("--");
        binding.tvBMICategory.setText("--");
        binding.tvFat.setText("--%");
        binding.tvLeanMass.setText("-- kg");
    }

    // --- REFACTOR: Estadísticas y Racha ---

    @SuppressLint("SetTextI18n")
    private void calculateStreakAndStats(List<Workout> history) {
        if (history == null || history.isEmpty()) {
            showEmptyStreakUI();
            return;
        }

        updateCaloriesStats(history);

        List<Date> uniqueWorkoutDates = getSortedUniqueDates(history);
        if (uniqueWorkoutDates.isEmpty()) return;

        calculateAndDisplayStreak(uniqueWorkoutDates);
    }

    @SuppressLint("SetTextI18n")
    private void showEmptyStreakUI() {
        binding.tvStreakTitle.setText("🔥  0 Días de Racha");
        binding.tvStreakMessage.setText("Aún no has registrado entrenamientos. ¡Empieza hoy!");
        binding.tvPeakCal.setText("0 kcal");
        binding.tvTodayCalories.setText("0 kcal");
    }

    private void updateCaloriesStats(List<Workout> history) {
        String todayStr = dateFormat.format(new Date());
        double maxCalories = 0.0;
        float todayCalories = 0.0f;

        for (Workout w : history) {
            Double cal = w.getCaloriesBurned();
            if (cal != null && cal > maxCalories) {
                maxCalories = cal;
            }
            if (w.getWorkoutDate() != null && dateFormat.format(w.getWorkoutDate()).equals(todayStr) && cal != null) todayCalories += cal;

        }

        binding.tvPeakCal.setText(String.format(Locale.US, "%.0f kcal", maxCalories));
        binding.tvTodayCalories.setText(String.format(Locale.US, "%.0f kcal", todayCalories));
    }

    private List<Date> getSortedUniqueDates(List<Workout> history) {
        List<Date> dates = new ArrayList<>();
        for (Workout w : history) {
            if (w.getWorkoutDate() != null) {
                try {
                    Date dateWithoutTime = dateFormat.parse(dateFormat.format(w.getWorkoutDate()));
                    if (dateWithoutTime != null && !dates.contains(dateWithoutTime)) {
                        dates.add(dateWithoutTime);
                    }
                } catch (ParseException ignored) {}
            }
        }
        dates.sort(Collections.reverseOrder());
        return dates;
    }

    @SuppressLint("SetTextI18n")
    private void calculateAndDisplayStreak(List<Date> workoutDates) {
        long daysSinceLastWorkout = getDaysSince(workoutDates.get(0));
        int streak = evaluateStreakStatus(daysSinceLastWorkout);

        if (streak > 0) {
            streak = countConsecutiveDays(workoutDates, streak);
        }

        binding.tvStreakTitle.setText("🔥  " + streak + " Días de Racha");
    }

    private long getDaysSince(Date targetDate) {
        Calendar todayCal = Calendar.getInstance();
        todayCal.set(Calendar.HOUR_OF_DAY, 0);
        todayCal.set(Calendar.MINUTE, 0);
        todayCal.set(Calendar.SECOND, 0);
        todayCal.set(Calendar.MILLISECOND, 0);

        long diffInMillis = todayCal.getTime().getTime() - targetDate.getTime();
        return TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS);
    }

    @SuppressLint("SetTextI18n")
    private int evaluateStreakStatus(long daysSinceLastWorkout) {
        if (daysSinceLastWorkout == 0) {
            binding.tvStreakMessage.setText("¡Entrenaste hoy! Sigue así 🔥");
            return 1;
        } else if (daysSinceLastWorkout == 1) {
            binding.tvStreakMessage.setText("Ayer entrenaste. ¡No pierdas el ritmo!");
            return 1;
        } else if (daysSinceLastWorkout == 2) {
            binding.tvStreakMessage.setText("⚠️ Llevas 2 días sin entrenar. ¡Hoy es crucial!");
            return 1;
        } else {
            binding.tvStreakMessage.setText("Has perdido la racha. ¡Es hora de volver a empezar!");
            return 0;
        }
    }

    private int countConsecutiveDays(List<Date> workoutDates, int initialStreak) {
        int streak = initialStreak;
        for (int i = 0; i < workoutDates.size() - 1; i++) {
            long diff = workoutDates.get(i).getTime() - workoutDates.get(i + 1).getTime();
            long daysBetween = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
            if (daysBetween <= 2) {
                streak++;
            } else {
                break;
            }
        }
        return streak;
    }

    // --- REFACTOR: Actividad Semanal ---

    private void calculateWeeklyActivity(List<Workout> history) {
        View[] bars = {binding.bar1, binding.bar2, binding.bar3, binding.bar4, binding.bar5, binding.bar6, binding.bar7};
        TextView[] texts = {binding.tvDay1, binding.tvDay2, binding.tvDay3, binding.tvDay4, binding.tvDay5, binding.tvDay6, binding.tvDay7};
        String[] dayNames = {"D", "L", "M", "X", "J", "V", "S"};

        Set<String> activeDates = getActiveDatesSet(history);
        Calendar cal = Calendar.getInstance();
        int todayDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1;

        for (int i = 6; i >= 0; i--) {
            int currentDayIndex = (todayDayOfWeek - (6 - i)) % 7;
            if (currentDayIndex < 0) currentDayIndex += 7;

            texts[i].setText(dayNames[currentDayIndex]);

            String targetDate = dateFormat.format(cal.getTime());
            boolean hasWorkout = activeDates.contains(targetDate);

            updateBarUI(bars[i], texts[i], hasWorkout, i == 6);
            cal.add(Calendar.DAY_OF_YEAR, -1);
        }
    }

    private Set<String> getActiveDatesSet(List<Workout> history) {
        Set<String> activeDates = new HashSet<>();
        if (history != null) {
            for (Workout w : history) {
                if (w.getWorkoutDate() != null) {
                    activeDates.add(dateFormat.format(w.getWorkoutDate()));
                }
            }
        }
        return activeDates;
    }

    private void updateBarUI(View bar, TextView text, boolean isActive, boolean isToday) {
        float density = getResources().getDisplayMetrics().density;
        int colorRosa = ContextCompat.getColor(requireContext(), R.color.mainrosa);
        int colorStroke = ContextCompat.getColor(requireContext(), R.color.stroke);
        int colorText = ContextCompat.getColor(requireContext(), R.color.text);

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) bar.getLayoutParams();

        if (isActive) {
            params.height = (int) (40 * density);
            bar.setBackgroundColor(colorRosa);
            if (!isToday) text.setTextColor(colorRosa);
        } else {
            params.height = (int) (10 * density);
            bar.setBackgroundColor(colorStroke);
            if (!isToday) text.setTextColor(colorText);
        }
        bar.setLayoutParams(params);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}