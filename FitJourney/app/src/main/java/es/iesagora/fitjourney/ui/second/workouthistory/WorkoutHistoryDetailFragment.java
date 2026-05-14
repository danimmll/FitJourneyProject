package es.iesagora.fitjourney.ui.second.workouthistory;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import es.iesagora.fitjourney.data.model.User;
import es.iesagora.fitjourney.data.model.Workout;
import es.iesagora.fitjourney.data.remote.Resource;
import es.iesagora.fitjourney.databinding.FragmentWorkoutHistoryDetailBinding;
import es.iesagora.fitjourney.ui.second.adapters.WorkoutDetailAdapter;
import es.iesagora.fitjourney.utils.FitnessCalculator;
import es.iesagora.fitjourney.viewmodel.SecondViewModel;

public class WorkoutHistoryDetailFragment extends Fragment {

    private FragmentWorkoutHistoryDetailBinding binding;
    private SecondViewModel viewModel;
    private int workoutId = -1;

    private final SimpleDateFormat outputFormat =
            new SimpleDateFormat("EEEE, dd 'de' MMMM • HH:mm", new Locale("es", "ES"));

    private static final SimpleDateFormat[] INPUT_FORMATS = {
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()),
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()),
            new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()),
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentWorkoutHistoryDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(SecondViewModel.class);

        if (getArguments() != null) {
            workoutId = getArguments().getInt("workout_id", -1);
        }

        if (workoutId != -1) {
            loadWorkoutData();
        } else {
            Navigation.findNavController(requireView()).navigateUp();
        }
    }

    private void loadWorkoutData() {
        // Buscar primero en el caché del LiveData para evitar una petición extra
        if (viewModel.getWorkoutHistory().getValue() != null
                && viewModel.getWorkoutHistory().getValue().data != null) {
            for (Workout w : viewModel.getWorkoutHistory().getValue().data) {
                if (w.getIdWorkout() == workoutId) {
                    fillUI(w);
                    return;
                }
            }
        }

        binding.progressBarDetail.setVisibility(View.VISIBLE);
        viewModel.fetchWorkoutById(workoutId);

        viewModel.getSingleWorkout().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;
            binding.progressBarDetail.setVisibility(View.GONE);

            if (resource.status == Resource.Status.SUCCESS && resource.data != null) {
                fillUI(resource.data);
            } else if (resource.status == Resource.Status.ERROR) {
                Toast.makeText(getContext(), "No se pudo cargar el detalle", Toast.LENGTH_SHORT).show();
                Navigation.findNavController(requireView()).navigateUp();
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void fillUI(Workout workout) {
        String routineName = (workout.getRoutine() != null)
                ? workout.getRoutine().getName()
                : "Entrenamiento Libre";
        binding.tvDetailRoutineName.setText(routineName);

        if (workout.getWorkoutDate() != null) {
            Date parsed = tryParseDate(workout.getWorkoutDate());
            if (parsed != null) {
                binding.tvDetailDate.setText(outputFormat.format(parsed));
            } else {
                binding.tvDetailDate.setText(workout.getWorkoutDate());
            }
        }

        int duration = workout.getDurationMinutes() != null ? workout.getDurationMinutes() : 0;
        binding.tvDetailDuration.setText(duration + " min");

        double calories = getCalories(workout, duration);
        binding.tvDetailCalories.setText((int) calories + " kcal");

        if (workout.getDetails() != null) {
            WorkoutDetailAdapter adapter = new WorkoutDetailAdapter(workout.getDetails());
            binding.rvWorkoutDetails.setLayoutManager(new LinearLayoutManager(getContext()));
            binding.rvWorkoutDetails.setAdapter(adapter);
        }
    }

    /**
     * Intenta parsear una fecha con los formatos más comunes del servidor.
     * Devuelve null si ningún formato encaja.
     */
    private Date tryParseDate(String dateStr) {
        for (SimpleDateFormat fmt : INPUT_FORMATS) {
            try {
                return fmt.parse(dateStr);
            } catch (ParseException ignored) {}
        }
        return null;
    }

    private double getCalories(Workout workout, int duration) {
        double weight = 70.0;

        if (workout.getUser() != null && workout.getUser().getWeight() != null) {
            weight = workout.getUser().getWeight();
        } else if (viewModel.getProfile().getValue() != null
                && viewModel.getProfile().getValue().data != null) {
            User user = viewModel.getProfile().getValue().data.getUser();
            if (user != null && user.getWeight() != null) {
                weight = user.getWeight();
            }
        }

        float durationHours = duration / 60f;
        return FitnessCalculator.calculateCaloriesBurned(6.0F, (float) weight, durationHours);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}