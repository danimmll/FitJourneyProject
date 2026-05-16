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

import java.text.SimpleDateFormat;
import java.util.Locale;

import es.iesagora.fitjourney.data.model.User;
import es.iesagora.fitjourney.data.model.Workout;
import es.iesagora.fitjourney.data.remote.Resource;
import es.iesagora.fitjourney.databinding.FragmentWorkoutHistoryDetailBinding;
import es.iesagora.fitjourney.ui.second.adapters.WorkoutDetailAdapter;
import es.iesagora.fitjourney.utils.FitnessCalculator;
import es.iesagora.fitjourney.viewmodel.SecondViewModel;

/**
 * Fragment que muestra el detalle de un entrenamiento realizado.
 * Permite visualizar información como rutina, duración, calorías y ejercicios realizados.
 */
public class WorkoutHistoryDetailFragment extends Fragment {

    private FragmentWorkoutHistoryDetailBinding binding;
    private SecondViewModel viewModel;
    private int workoutId = -1;

    private final SimpleDateFormat outputFormat =
            new SimpleDateFormat("EEEE, dd 'de' MMMM • HH:mm", new Locale("es", "ES"));

    /**
     * Infla la vista del fragmento.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentWorkoutHistoryDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * Inicializa ViewModel y carga el entrenamiento solicitado.
     */
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

    /**
     * Carga el entrenamiento desde caché o desde la API.
     */
    private void loadWorkoutData() {

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
                Toast.makeText(getContext(),
                        "No se pudo cargar el detalle",
                        Toast.LENGTH_SHORT).show();

                Navigation.findNavController(requireView()).navigateUp();
            }
        });
    }

    /**
     * Rellena la interfaz con los datos del entrenamiento.
     */
    @SuppressLint("SetTextI18n")
    private void fillUI(Workout workout) {

        String routineName = (workout.getRoutine() != null)
                ? workout.getRoutine().getName()
                : "Entrenamiento Libre";

        binding.tvDetailRoutineName.setText(routineName);

        if (workout.getWorkoutDate() != null) {
            binding.tvDetailDate.setText(outputFormat.format(workout.getWorkoutDate()));
        }

        int duration = workout.getDurationMinutes() != null
                ? workout.getDurationMinutes()
                : 0;

        binding.tvDetailDuration.setText(duration + " min");

        double calories = getCalories(workout, duration);
        binding.tvDetailCalories.setText((int) calories + " kcal");

        if (workout.getDetails() != null) {

            WorkoutDetailAdapter adapter =
                    new WorkoutDetailAdapter(workout.getDetails());

            binding.rvWorkoutDetails.setLayoutManager(
                    new LinearLayoutManager(getContext())
            );

            binding.rvWorkoutDetails.setAdapter(adapter);
        }
    }

    /**
     * Calcula las calorías estimadas del entrenamiento.
     */
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

        return FitnessCalculator.calculateCaloriesBurned(
                6.0F,
                (float) weight,
                durationHours
        );
    }

    /**
     * Limpia el binding al destruir la vista.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}