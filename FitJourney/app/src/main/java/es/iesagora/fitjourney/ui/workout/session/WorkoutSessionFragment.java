package es.iesagora.fitjourney.ui.workout.session;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

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
import es.iesagora.fitjourney.databinding.ViewholderSetRowBinding;
import es.iesagora.fitjourney.databinding.ViewholderWorkoutInputBinding;
import es.iesagora.fitjourney.utils.FitnessCalculator;
import es.iesagora.fitjourney.viewmodel.SecondViewModel;

public class WorkoutSessionFragment extends Fragment {

    private static final String TYPE_HIPERTROFIA = "Hipertrofia";
    private static final String TYPE_CARDIO = "Cardio";
    private static final String TYPE_ESTIRAMIENTO = "Estiramiento";
    private static final String OBJETIVO_STRING = "Objetivo: ";

    private FragmentWorkoutSessionBinding binding;
    private SecondViewModel viewModel;

    private int routineId = -1;

    private int secondsElapsed = 0;
    private boolean isTimerRunning = true;

    private final Handler timerHandler = new Handler(Looper.getMainLooper());
    private Runnable timerRunnable;

    private final List<ExerciseBlock> exerciseBlocks = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentWorkoutSessionBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeViewModel();
        getRoutineIdFromIntent();
        setupListeners();
        setupBackPressed();
        setupObservers();
        loadRoutine();
    }

    private void initializeViewModel() {
        viewModel = new ViewModelProvider(requireActivity()).get(SecondViewModel.class);
    }

    private void getRoutineIdFromIntent() {
        if (getActivity() != null && getActivity().getIntent() != null) {
            routineId = getActivity().getIntent().getIntExtra("ROUTINE_ID", -1);
        }
    }

    private void setupListeners() {
        binding.toolbarWorkout.setNavigationOnClickListener(v -> showExitConfirmationDialog());
        binding.btnFinishWorkout.setOnClickListener(v -> finishWorkout());
    }

    private void setupBackPressed() {
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        showExitConfirmationDialog();
                    }
                });
    }

    private void loadRoutine() {
        if (routineId == -1) {
            Toast.makeText(getContext(), "Error al cargar rutina", Toast.LENGTH_SHORT).show();
            requireActivity().finish();
            return;
        }
        viewModel.fetchRoutineById(routineId);
        startTimer();
    }

    private void setupObservers() {
        observeRoutine();
        observeWorkoutCreation();
    }

    private void observeRoutine() {
        viewModel.getSingleRoutine().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;
            if (resource.status == Resource.Status.SUCCESS && resource.data != null) {
                binding.tvWorkoutRoutineName.setText(resource.data.getName());
                inflateExercises(resource.data);
            }
        });
    }

    private void observeWorkoutCreation() {
        viewModel.getWorkoutActionStatus().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;
            if (resource.status == Resource.Status.SUCCESS) {
                Toast.makeText(getContext(), "¡Entrenamiento guardado!", Toast.LENGTH_SHORT).show();
                requireActivity().finish();
            } else if (resource.status == Resource.Status.ERROR) {
                Toast.makeText(getContext(), resource.message, Toast.LENGTH_SHORT).show();
                binding.btnFinishWorkout.setEnabled(true);
            }
        });
    }

    private void startTimer() {
        isTimerRunning = true;
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isTimerRunning) return;
                if (binding == null) return;

                secondsElapsed++;
                int mins = secondsElapsed / 60;
                int secs = secondsElapsed % 60;

                binding.tvWorkoutTimer.setText(String.format(Locale.getDefault(), "%02d:%02d", mins, secs));
                timerHandler.postDelayed(this, 1000);
            }
        };
        timerHandler.postDelayed(timerRunnable, 1000);
    }

    @SuppressLint("SetTextI18n")
    private void inflateExercises(Routine routine) {
        binding.llExercisesContainer.removeAllViews();
        exerciseBlocks.clear();

        for (RoutineExercise re : routine.getExercises()) {
            ViewholderWorkoutInputBinding cardBinding = ViewholderWorkoutInputBinding.inflate(getLayoutInflater(), binding.llExercisesContainer, false);
            configureExerciseCard(cardBinding, re);
            List<SerieRow> rows = createExerciseRows(cardBinding, re);
            configureExerciseInfo(cardBinding, re);

            ExerciseBlock block = new ExerciseBlock(re, cardBinding, rows);
            setupExpandListener(block);
            exerciseBlocks.add(block);
            binding.llExercisesContainer.addView(cardBinding.getRoot());
        }
    }

    @SuppressLint("SetTextI18n")
    private void configureExerciseCard(ViewholderWorkoutInputBinding cardBinding, RoutineExercise re) {
        String type = TYPE_HIPERTROFIA;
        if (re.getExercise() != null) {
            cardBinding.tvWorkoutExerciseName.setText(re.getExercise().getName());
            if (re.getExercise().getExerciseType() != null) {
                type = re.getExercise().getExerciseType();
            }
        }
        setTargetText(cardBinding, re, type);
    }

    @SuppressLint("SetTextI18n")
    private void setTargetText(ViewholderWorkoutInputBinding cardBinding, RoutineExercise re, String type) {
        int targetSets = re.getTargetSets() != null ? re.getTargetSets() : 3;

        switch (type) {
            case TYPE_CARDIO:
                cardBinding.tvTargetGoal.setText(OBJETIVO_STRING + targetSets + " series • Cardio");
                break;
            case TYPE_ESTIRAMIENTO:
                cardBinding.tvTargetGoal.setText(OBJETIVO_STRING + targetSets + " series • Estiramiento");
                break;
            default:
                String targetText = getString(re, targetSets);
                cardBinding.tvTargetGoal.setText(targetText);
                break;
        }
    }

    private static String getString(RoutineExercise re, int targetSets) {
        int targetReps = re.getTargetReps() != null ? re.getTargetReps() : 10;
        String targetText = OBJETIVO_STRING + targetSets + "×" + targetReps + " reps";

        if (re.getTargetWeight() != null && re.getTargetWeight() > 0) {
            targetText += " @ " + re.getTargetWeight() + " kg";
        }
        return targetText;
    }

    private List<SerieRow> createExerciseRows(ViewholderWorkoutInputBinding cardBinding, RoutineExercise re) {
        List<SerieRow> rows = new ArrayList<>();
        String type = getExerciseType(re);
        int targetSets = re.getTargetSets() != null ? re.getTargetSets() : 3;

        for (int i = 0; i < targetSets; i++) {
            ViewholderSetRowBinding rowBinding = ViewholderSetRowBinding.inflate(getLayoutInflater(), cardBinding.llSeriesContainer, false);
            SerieRow row = setupSerieRow(rowBinding, i + 1, type, re);
            rows.add(row);
            cardBinding.llSeriesContainer.addView(rowBinding.getRoot());
        }
        return rows;
    }

    @SuppressLint("SetTextI18n")
    private SerieRow setupSerieRow(ViewholderSetRowBinding rowBinding, int serieNumber, String type, RoutineExercise re) {
        rowBinding.tvSerieLabel.setText("S" + serieNumber);

        switch (type) {
            case TYPE_CARDIO:
                configureCardioRow(rowBinding);
                break;
            case TYPE_ESTIRAMIENTO:
                configureStretchingRow(rowBinding);
                break;
            default:
                configureStrengthRow(rowBinding, re);
                break;
        }

        rowBinding.cbSerieCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> rowBinding.getRoot().setAlpha(isChecked ? 1f : 0.6f));
        rowBinding.getRoot().setAlpha(0.6f);
        return new SerieRow(rowBinding, type);
    }

    private void configureCardioRow(ViewholderSetRowBinding rowBinding) {
        rowBinding.tilPrimary.setHint("Min");
        rowBinding.tilSecondary.setHint("km/h");
        rowBinding.etPrimary.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        rowBinding.etSecondary.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
    }

    private void configureStretchingRow(ViewholderSetRowBinding rowBinding) {
        rowBinding.tilPrimary.setVisibility(View.GONE);
        rowBinding.tilSecondary.setVisibility(View.GONE);
    }

    private void configureStrengthRow(ViewholderSetRowBinding rowBinding, RoutineExercise re) {
        rowBinding.tilPrimary.setHint("Reps");
        rowBinding.tilSecondary.setHint("Peso (kg)");

        if (re.getTargetReps() != null && re.getTargetReps() > 0) {
            rowBinding.etPrimary.setText(String.valueOf(re.getTargetReps()));
        }

        if (re.getTargetWeight() != null && re.getTargetWeight() > 0) {
            rowBinding.etSecondary.setText(String.valueOf(re.getTargetWeight()));
        }
    }

    private void configureExerciseInfo(ViewholderWorkoutInputBinding cardBinding, RoutineExercise re) {
        if (re.getExercise() == null) {
            cardBinding.ivExerciseInfo.setVisibility(View.GONE);
            return;
        }

        Exercise exercise = re.getExercise();
        cardBinding.ivExerciseInfo.setOnClickListener(v -> {
            viewModel.selectExercise(exercise);
            try {
                Bundle args = new Bundle();
                args.putBoolean("readOnly", true);
                args.putSerializable("exercise_data", exercise);
                Navigation.findNavController(requireView()).navigate(R.id.exerciseDetailFragment, args);
            } catch (Exception e) {
                Toast.makeText(getContext(), "Falta añadir exerciseDetailFragment en nav_workout.xml", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupExpandListener(ExerciseBlock block) {
        block.cardBinding.llExerciseHeader.setOnClickListener(v -> toggleExpand(block));
    }

    private void toggleExpand(ExerciseBlock block) {
        block.expanded = !block.expanded;
        block.cardBinding.dividerExercise.setVisibility(block.expanded ? View.VISIBLE : View.GONE);
        block.cardBinding.llSeriesContainer.setVisibility(block.expanded ? View.VISIBLE : View.GONE);
        float targetRotation = block.expanded ? 90f : 270f;
        ObjectAnimator.ofFloat(block.cardBinding.ivExpandArrow, "rotation", targetRotation).setDuration(200).start();
    }

    private void finishWorkout() {
        if (!validarDatosEntrenamiento()) return;

        binding.btnFinishWorkout.setEnabled(false);
        isTimerRunning = false;

        int durationMinutes = Math.max(1, secondsElapsed / 60);
        float durationHours = durationMinutes / 60f;
        float userWeightKg = getUserWeight();
        float timePerExerciseHours = exerciseBlocks.isEmpty() ? durationHours : durationHours / exerciseBlocks.size();

        Workout workout = new Workout();
        workout.setIdRoutine(routineId);
        workout.setDurationMinutes(durationMinutes);

        List<WorkoutDetail> details = new ArrayList<>();
        float totalCalories = 0f;

        for (ExerciseBlock block : exerciseBlocks) {
            WorkoutDetail detail = buildWorkoutDetail(block);
            if (detail != null) {
                details.add(detail);
                float met = getMETForBlock(block, userWeightKg);
                totalCalories += FitnessCalculator.calculateCaloriesBurned(met, userWeightKg, timePerExerciseHours);
            }
        }

        workout.setDetails(details);
        workout.setCaloriesBurned((double) totalCalories);
        viewModel.createWorkout(workout);
    }

    @Nullable
    private WorkoutDetail buildWorkoutDetail(ExerciseBlock block) {
        if (block.re.getIdExercise() == 0) return null;

        WorkoutDetail detail = new WorkoutDetail();
        detail.setIdExercise(block.re.getIdExercise());
        List<SerieRow> checkedRows = getCheckedRows(block);
        detail.setSetsPerformed(checkedRows.size());

        if (checkedRows.isEmpty()) return detail;

        switch (block.rows.get(0).type) {
            case TYPE_CARDIO:
                buildCardioDetail(detail, checkedRows);
                break;
            case TYPE_ESTIRAMIENTO:
                break;
            default:
                buildStrengthDetail(detail, checkedRows);
                break;
        }
        return detail;
    }

    private void buildCardioDetail(WorkoutDetail detail, List<SerieRow> checkedRows) {
        SerieRow last = checkedRows.get(checkedRows.size() - 1);
        String mins = getText(last.rowBinding.etPrimary);
        String speed = getText(last.rowBinding.etSecondary);

        detail.setDurationMinutes(mins.isEmpty() ? 0.0 : Double.parseDouble(mins.replace(",", ".")));
        detail.setSpeedKmh(speed.isEmpty() ? 0.0 : Double.parseDouble(speed.replace(",", ".")));
    }

    private void buildStrengthDetail(WorkoutDetail detail, List<SerieRow> checkedRows) {
        StringBuilder repsBuilder = new StringBuilder();
        double maxWeight = 0.0;

        for (int i = 0; i < checkedRows.size(); i++) {
            SerieRow row = checkedRows.get(i);
            String reps = getText(row.rowBinding.etPrimary);
            String weight = getText(row.rowBinding.etSecondary);

            if (i > 0) repsBuilder.append(",");
            repsBuilder.append(reps.isEmpty() ? "0" : reps);

            if (!weight.isEmpty()) {
                double parsed = Double.parseDouble(weight.replace(",", "."));
                if (parsed > maxWeight) maxWeight = parsed;
            }
        }
        detail.setRepsString(repsBuilder.toString());
        detail.setMaxWeight(maxWeight);
    }

    private List<SerieRow> getCheckedRows(ExerciseBlock block) {
        List<SerieRow> checked = new ArrayList<>();
        for (SerieRow row : block.rows) {
            if (row.rowBinding.cbSerieCompleted.isChecked()) checked.add(row);
        }
        return checked;
    }

    private float getMETForBlock(ExerciseBlock block, float userWeightKg) {
        Exercise ex = block.re.getExercise();
        if (ex == null || ex.getExerciseType() == null) return 6.0f;

        List<SerieRow> checked = getCheckedRows(block);
        int sets = Math.max(1, checked.size());

        switch (ex.getExerciseType()) {
            case TYPE_CARDIO:
                return calculateCardioMET(checked);
            case TYPE_ESTIRAMIENTO:
                return FitnessCalculator.calculateStretchingMET(false, "Normal", "Intermedio");
            default:
                return calculateStrengthMET(checked, userWeightKg, sets);
        }
    }

    private float calculateCardioMET(List<SerieRow> checked) {
        float totalSpeed = 0f;
        int count = 0;

        for (SerieRow row : checked) {
            String speed = getText(row.rowBinding.etSecondary);
            if (!speed.isEmpty()) {
                totalSpeed += Float.parseFloat(speed.replace(",", "."));
                count++;
            }
        }

        float avgSpeed = count > 0 ? totalSpeed / count : 8f;
        return FitnessCalculator.calculateCardioMET(8.0f, avgSpeed, 0f);
    }

    private float calculateStrengthMET(List<SerieRow> checked, float userWeightKg, int sets) {
        float totalReps = 0f;
        float totalWeight = 0f;
        int count = 0;

        for (SerieRow row : checked) {
            String reps = getText(row.rowBinding.etPrimary);
            String weight = getText(row.rowBinding.etSecondary);

            if (!reps.isEmpty()) totalReps += Float.parseFloat(reps.replace(",", "."));
            if (!weight.isEmpty()) {
                totalWeight += Float.parseFloat(weight.replace(",", "."));
                count++;
            }
        }

        float bodyWeight = userWeightKg > 0 ? userWeightKg : 70f;
        float liftedWeight = count > 0 ? totalWeight / count : bodyWeight * 0.5f;
        int reps = sets > 0 && totalReps > 0 ? (int) (totalReps / sets) : 10;

        return FitnessCalculator.calculateStrengthMET(liftedWeight, bodyWeight, sets, reps, 90);
    }

    private float getUserWeight() {
        if (viewModel.getProfile().getValue() != null && viewModel.getProfile().getValue().data != null) {
            User user = viewModel.getProfile().getValue().data.getUser();
            if (user != null && user.getWeight() != null) {
                return user.getWeight().floatValue();
            }
        }
        return 70f;
    }

    private String getText(TextInputEditText et) {
        return et != null && et.getText() != null ? et.getText().toString().trim() : "";
    }

    private String getExerciseType(RoutineExercise re) {
        if (re.getExercise() != null && re.getExercise().getExerciseType() != null) {
            return re.getExercise().getExerciseType();
        }
        return TYPE_HIPERTROFIA;
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

    private boolean validarDatosEntrenamiento() {
        for (ExerciseBlock bloque : exerciseBlocks) {
            List<SerieRow> filasMarcadas = getCheckedRows(bloque);
            if (filasMarcadas.isEmpty()) continue;

            for (SerieRow fila : filasMarcadas) {
                String texto1 = getText(fila.rowBinding.etPrimary);
                String texto2 = getText(fila.rowBinding.etSecondary);

                try {
                    double peso = texto2.isEmpty() ? 0 : Double.parseDouble(texto2.replace(",", "."));
                    if (fila.type.equals(TYPE_CARDIO)) {
                        double minutos = texto1.isEmpty() ? 0 : Double.parseDouble(texto1.replace(",", "."));

                        if (minutos > 300) {
                            Toast.makeText(getContext(), "Máximo 300 min por serie", Toast.LENGTH_SHORT).show();
                            return false;
                        }
                        if (peso > 50) {
                            Toast.makeText(getContext(), "Pilla una bici. Máximo 50 km/h", Toast.LENGTH_SHORT).show();
                            return false;
                        }
                    } else if (!fila.type.equals(TYPE_ESTIRAMIENTO)) {
                        int repes = texto1.isEmpty() ? 0 : Integer.parseInt(texto1);

                        if (repes > 200) {
                            Toast.makeText(getContext(), "Demasiadas repes. Máximo 200", Toast.LENGTH_SHORT).show();
                            return false;
                        }
                        if (peso > 1000) {
                            Toast.makeText(getContext(), "Ni Ronnie Coleman. Máximo 1000 kg", Toast.LENGTH_SHORT).show();
                            return false;
                        }
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "Por favor, mete números normales", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isTimerRunning = false;
        timerHandler.removeCallbacks(timerRunnable);
        binding = null;
    }

    private static class ExerciseBlock {
        RoutineExercise re;
        ViewholderWorkoutInputBinding cardBinding;
        List<SerieRow> rows;
        boolean expanded = false;

        ExerciseBlock(RoutineExercise re, ViewholderWorkoutInputBinding cardBinding, List<SerieRow> rows) {
            this.re = re;
            this.cardBinding = cardBinding;
            this.rows = rows;
        }
    }

    private static class SerieRow {
        ViewholderSetRowBinding rowBinding;
        String type;

        SerieRow(ViewholderSetRowBinding rowBinding, String type) {
            this.rowBinding = rowBinding;
            this.type = type;
        }
    }
}