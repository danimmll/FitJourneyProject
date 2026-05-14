package es.iesagora.fitjourney.ui.workout.session;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import es.iesagora.fitjourney.R;
import es.iesagora.fitjourney.data.model.Exercise;
import es.iesagora.fitjourney.data.model.Routine;
import es.iesagora.fitjourney.data.model.RoutineExercise;
import es.iesagora.fitjourney.data.model.User;
import es.iesagora.fitjourney.data.model.Workout;
import es.iesagora.fitjourney.data.model.WorkoutDetail;
import es.iesagora.fitjourney.data.remote.Resource;
import es.iesagora.fitjourney.databinding.FragmentWorkoutSessionBinding;
import es.iesagora.fitjourney.utils.FitnessCalculator;
import es.iesagora.fitjourney.viewmodel.SecondViewModel;

public class WorkoutSessionFragment extends Fragment {

    private FragmentWorkoutSessionBinding binding;
    private SecondViewModel viewModel;
    private int routineId = -1;

    private int secondsElapsed = 0;
    private boolean isTimerRunning = true;
    private final Handler timerHandler = new Handler(Looper.getMainLooper());
    private Runnable timerRunnable;

    private final List<ExerciseInputWrapper> inputViews = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentWorkoutSessionBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(SecondViewModel.class);

        if (getActivity() != null && getActivity().getIntent() != null) {
            routineId = getActivity().getIntent().getIntExtra("ROUTINE_ID", -1);
        }

        if (routineId != -1) {
            setupObservers();
            viewModel.fetchRoutineById(routineId);
            startTimer();
        } else {
            Toast.makeText(getContext(), "Error al cargar rutina", Toast.LENGTH_SHORT).show();
            requireActivity().finish();
        }

        binding.toolbarWorkout.setNavigationOnClickListener(v -> showExitConfirmationDialog());
        binding.btnFinishWorkout.setOnClickListener(v -> finishWorkout());

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        showExitConfirmationDialog();
                    }
                });
    }

    private void setupObservers() {
        viewModel.getSingleRoutine().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;
            if (resource.status == Resource.Status.SUCCESS && resource.data != null) {
                binding.tvWorkoutRoutineName.setText(resource.data.getName());
                inflateExercises(resource.data);
            }
        });

        viewModel.getWorkoutActionStatus().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;
            if (resource.status == Resource.Status.SUCCESS) {
                Toast.makeText(getContext(), "¡Entrenamiento guardado!", Toast.LENGTH_SHORT).show();
                requireActivity().finish();
            } else if (resource.status == Resource.Status.ERROR) {
                Toast.makeText(getContext(), resource.message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startTimer() {
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (isTimerRunning) {
                    secondsElapsed++;
                    int mins = secondsElapsed / 60;
                    int secs = secondsElapsed % 60;
                    binding.tvWorkoutTimer.setText(
                            String.format(Locale.getDefault(), "%02d:%02d", mins, secs));
                    timerHandler.postDelayed(this, 1000);
                }
            }
        };
        timerHandler.postDelayed(timerRunnable, 1000);
    }

    @SuppressLint("SetTextI18n")
    private void inflateExercises(Routine routine) {
        binding.llExercisesContainer.removeAllViews();
        inputViews.clear();

        for (RoutineExercise re : routine.getExercises()) {
            View exerciseView = getLayoutInflater().inflate(
                    R.layout.viewholder_workout_input, binding.llExercisesContainer, false);

            TextView tvName   = exerciseView.findViewById(R.id.tvWorkoutExerciseName);
            TextView tvTarget = exerciseView.findViewById(R.id.tvTargetGoal);
            TextInputEditText etSets   = exerciseView.findViewById(R.id.etActualSets);
            TextInputEditText etReps   = exerciseView.findViewById(R.id.etActualReps);
            TextInputEditText etWeight = exerciseView.findViewById(R.id.etActualWeight);

            if (re.getExercise() != null) {
                tvName.setText(re.getExercise().getName() + " ⓘ");
                tvName.setOnClickListener(v -> showExerciseInfo(re.getExercise()));
            }

            String targetText = "Objetivo: " + re.getTargetSets() + "x" + re.getTargetReps();
            if (re.getTargetWeight() != null && re.getTargetWeight() > 0) {
                targetText += " @ " + re.getTargetWeight() + " kg";
                etWeight.setText(String.valueOf(re.getTargetWeight()));
            }
            tvTarget.setText(targetText);
            etSets.setText(String.valueOf(re.getTargetSets()));
            etReps.setText(String.valueOf(re.getTargetReps()));

            binding.llExercisesContainer.addView(exerciseView);
            inputViews.add(new ExerciseInputWrapper(re, etSets, etReps, etWeight));
        }
    }

    private void showExerciseInfo(Exercise exercise) {
        if (exercise == null) return;

        String description = (exercise.getDescription() != null && !exercise.getDescription().isEmpty())
                ? exercise.getDescription()
                : "No hay instrucciones detalladas para este ejercicio.";

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(exercise.getName())
                .setMessage(description)
                .setPositiveButton("Entendido", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void finishWorkout() {
        isTimerRunning = false;

        int durationMinutes = Math.max(1, secondsElapsed / 60);
        float durationHours = durationMinutes / 60f;

        float userWeightKg = getUserWeight();
        float exerciseCount = !inputViews.isEmpty() ? inputViews.size() : 1f;
        float timePerExerciseHours = durationHours / exerciseCount;

        Workout workout = new Workout();
        workout.setIdRoutine(routineId);
        workout.setDurationMinutes(durationMinutes);

        List<WorkoutDetail> details = new ArrayList<>();
        float totalCalories = 0f;

        for (ExerciseInputWrapper wrapper : inputViews) {
            WorkoutDetail d = new WorkoutDetail();
            d.setIdExercise(wrapper.re.getIdExercise());

            String sSets   = wrapper.etSets.getText()   != null ? wrapper.etSets.getText().toString().trim()   : "";
            String sReps   = wrapper.etReps.getText()   != null ? wrapper.etReps.getText().toString().trim()   : "";
            String sWeight = wrapper.etWeight.getText() != null ? wrapper.etWeight.getText().toString().trim() : "";

            int    sets   = sSets.isEmpty()   ? 0    : Integer.parseInt(sSets);
            int    reps   = sReps.isEmpty()   ? 0    : Integer.parseInt(sReps);
            double weight = sWeight.isEmpty() ? 0.0  : Double.parseDouble(sWeight);

            d.setSetsPerformed(sets);
            d.setRepsPerformed(String.valueOf(reps));
            d.setMaxWeightUsed(weight);
            details.add(d);

            float met = getMETForExercise(wrapper.re.getExercise(), sets, reps, (float) weight, userWeightKg);
            totalCalories += FitnessCalculator.calculateCaloriesBurned(met, userWeightKg, timePerExerciseHours);
        }

        workout.setDetails(details);
        workout.setCaloriesBurned((double) totalCalories);

        viewModel.createWorkout(workout);
    }

    /**
     * Obtiene el peso del usuario desde el perfil cargado en el ViewModel.
     * Devuelve 70 kg como valor por defecto si no hay datos disponibles.
     */
    private float getUserWeight() {
        if (viewModel.getProfile().getValue() != null
                && viewModel.getProfile().getValue().data != null) {
            User user = viewModel.getProfile().getValue().data.getUser();
            if (user != null && user.getWeight() != null) {
                return user.getWeight().floatValue();
            }
        }
        return 70f;
    }


    private float getMETForExercise(Exercise exercise, int sets, int reps,
                                    float weightKg, float bodyWeightKg) {
        if (exercise == null || exercise.getExerciseType() == null) {
            return 6.0f;
        }

        switch (exercise.getExerciseType()) {
            case "Cardio":
                return FitnessCalculator.calculateCardioMET(8.0f, 8.0f, 0f);

            case "Estiramiento":
                return FitnessCalculator.calculateStretchingMET(false, "Normal", "Intermedio");

            case "Hipertrofia":
            default:
                float bw = bodyWeightKg > 0 ? bodyWeightKg : 70f;
                // Si no se registró peso, usar el 50% del peso corporal como estimación
                float lifted = weightKg > 0 ? weightKg : bw * 0.5f;
                int s = sets > 0 ? sets : 3;
                int r = reps > 0 ? reps : 10;
                return FitnessCalculator.calculateStrengthMET(lifted, bw, s, r, 90);
        }
    }

    private void showExitConfirmationDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("¿Abandonar Entrenamiento?")
                .setMessage("El cronómetro se detendrá y perderás el progreso de esta sesión. Esta acción no se puede deshacer.")
                .setPositiveButton("Salir", (dialog, which) -> {
                    isTimerRunning = false;
                    requireActivity().finish();
                })
                .setNegativeButton("Continuar entrenando", (dialog, which) -> dialog.dismiss())
                .setCancelable(false)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isTimerRunning = false;
        timerHandler.removeCallbacks(timerRunnable);
        binding = null;
    }

    private static class ExerciseInputWrapper {
        RoutineExercise re;
        TextInputEditText etSets;
        TextInputEditText etReps;
        TextInputEditText etWeight;

        ExerciseInputWrapper(RoutineExercise re, TextInputEditText s,
                             TextInputEditText r, TextInputEditText w) {
            this.re = re;
            this.etSets = s;
            this.etReps = r;
            this.etWeight = w;
        }
    }
}