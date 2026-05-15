package es.iesagora.fitjourney.ui.second.fitnesscenter.routines;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import es.iesagora.fitjourney.R;
import es.iesagora.fitjourney.data.model.Exercise;
import es.iesagora.fitjourney.data.model.Routine;
import es.iesagora.fitjourney.data.model.RoutineExercise;
import es.iesagora.fitjourney.data.remote.Resource;
import es.iesagora.fitjourney.databinding.DialogAddRoutineExerciseBinding;
import es.iesagora.fitjourney.databinding.FragmentFormRoutineBinding;
import es.iesagora.fitjourney.ui.second.adapters.AddedExerciseAdapter;
import es.iesagora.fitjourney.ui.second.adapters.SelectExerciseAdapter;
import es.iesagora.fitjourney.utils.ImageUtils;
import es.iesagora.fitjourney.viewmodel.SecondViewModel;

public class FormRoutineFragment extends Fragment {

    private FragmentFormRoutineBinding binding;
    private SecondViewModel viewModel;

    private Uri selectedLocalImageUri = null;

    private final List<RoutineExercise> addedExercises = new ArrayList<>();
    private AddedExerciseAdapter adapter;

    private static final String TYPE_HIPERTROFIA = "Hipertrofia";
    private static final String TYPE_CARDIO = "Cardio";
    private static final String TYPE_ESTIRAMIENTO = "Estiramiento";

    private static final String ROUTINE_DATA_STRING = "routine_data";

    private final ActivityResultLauncher<String> getContent =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedLocalImageUri = uri;
                    binding.etRoutineImage.setText("");
                    ImageUtils.loadInto(requireContext(), uri.toString(), binding.ivPhotoPreview, R.drawable.img_construccion);
                }
            });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFormRoutineBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(SecondViewModel.class);

        viewModel.getRoutineActionStatus().setValue(null);

        if (viewModel.getExercises().getValue() == null) {
            viewModel.fetchExercises();
        }

        if (getArguments() != null && getArguments().containsKey(ROUTINE_DATA_STRING)) {
            Routine routineToEdit = (Routine) getArguments().getSerializable(ROUTINE_DATA_STRING);
            assert routineToEdit != null;
            fillFieldsForEdit(routineToEdit);
        }

        setupRecyclerView();
        setupListeners();
        setupObservers();
    }

    private void setupRecyclerView() {
        adapter = new AddedExerciseAdapter(addedExercises, position -> {
            addedExercises.remove(position);
            adapter.notifyItemRemoved(position);
            updateEmptyState();
        });
        binding.rvExercises.setAdapter(adapter);
        updateEmptyState();
    }

    private void setupListeners() {
        binding.toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        binding.btnSelectRoutineImage.setOnClickListener(v -> getContent.launch("image/*"));
        binding.ivPhotoPreview.setOnClickListener(v -> getContent.launch("image/*"));

        binding.etRoutineImage.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String url = Objects.requireNonNull(binding.etRoutineImage.getText()).toString().trim();
                if (!url.isEmpty()) {
                    selectedLocalImageUri = null;
                    ImageUtils.loadInto(requireContext(), url, binding.ivPhotoPreview, R.drawable.img_construccion);
                }
            }
        });

        binding.btnAddExercise.setOnClickListener(v -> showAddExerciseDialog());
        binding.btnSaveRoutine.setOnClickListener(v -> saveRoutine());
    }

    private void showAddExerciseDialog() {
        List<Exercise> allExercises = getExerciseCatalog();
        if (allExercises == null) return;

        DialogAddRoutineExerciseBinding dialogBinding =
                DialogAddRoutineExerciseBinding.inflate(getLayoutInflater());

        final Exercise[] selectedExercise = {null};

        @SuppressLint("SetTextI18n") SelectExerciseAdapter pickerAdapter = new SelectExerciseAdapter(exercise -> {
            selectedExercise[0] = exercise;
            dialogBinding.tvSelectedExercise.setText("Seleccionado: " + exercise.getName());

            String type = exercise.getExerciseType() != null ? exercise.getExerciseType() : TYPE_HIPERTROFIA;
            if (type.equals(TYPE_CARDIO) || type.equals(TYPE_ESTIRAMIENTO)) {
                dialogBinding.tilReps.setVisibility(View.GONE);
                dialogBinding.tilSets.setHint("Series/Rondas");
            } else {
                dialogBinding.tilReps.setVisibility(View.VISIBLE);
                dialogBinding.tilSets.setHint("Series");
            }
        });

        dialogBinding.rvExercisePicker.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        dialogBinding.rvExercisePicker.setAdapter(pickerAdapter);
        pickerAdapter.submitList(allExercises);

        dialogBinding.searchExercise.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }

            @Override
            public boolean onQueryTextChange(String newText) {
                List<Exercise> filtered = new ArrayList<>();
                String q = newText == null ? "" : newText.trim().toLowerCase();
                for (Exercise e : allExercises) {
                    if (q.isEmpty() || e.getName().toLowerCase().contains(q)) {
                        filtered.add(e);
                    }
                }
                pickerAdapter.submitList(filtered);
                return true;
            }
        });

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogBinding.getRoot())
                .create();
        dialog.show();

        dialogBinding.btnConfirmAdd.setOnClickListener(v -> {
            if (selectedExercise[0] == null) {
                toast("Por favor, selecciona un ejercicio deslizando en la lista.");
                return;
            }
            onConfirmAddExercise(selectedExercise[0], dialogBinding, dialog);
        });
    }

    private List<Exercise> getExerciseCatalog() {
        Resource<List<Exercise>> resource = viewModel.getExercises().getValue();
        if (resource == null || resource.data == null) {
            toast("Cargando catálogo de ejercicios, espera...");
            return null;
        }
        return resource.data;
    }

    private void onConfirmAddExercise(Exercise selectedExercise, DialogAddRoutineExerciseBinding dialogBinding, AlertDialog dialog) {
        String type = selectedExercise.getExerciseType() != null ? selectedExercise.getExerciseType() : TYPE_HIPERTROFIA;
        boolean isHipertrofia = type.equals(TYPE_HIPERTROFIA);

        String setsStr = dialogBinding.etSets.getText() != null ? dialogBinding.etSets.getText().toString().trim() : "";
        String repsStr = dialogBinding.etReps.getText() != null ? dialogBinding.etReps.getText().toString().trim() : "";

        if (setsStr.isEmpty() || (isHipertrofia && repsStr.isEmpty())) {
            toast("Rellena los campos obligatorios");
            return;
        }

        int sets;
        int reps = 0;

        try {
            sets = Integer.parseInt(setsStr);
            if (isHipertrofia) {
                reps = Integer.parseInt(repsStr);
            }
        } catch (NumberFormatException e) {
            toast("Introduce números válidos");
            return;
        }

        if (sets <= 0 || (isHipertrofia && reps <= 0)) {
            toast("Los valores deben ser mayores que 0");
            return;
        }

        addRoutineExercise(selectedExercise, sets, reps);
        dialog.dismiss();
    }

    private void addRoutineExercise(Exercise exercise, int sets, int reps) {
        RoutineExercise newRoutineEx = new RoutineExercise();
        newRoutineEx.setIdExercise(exercise.getIdExercise());
        newRoutineEx.setExercise(exercise);
        newRoutineEx.setTargetSets(sets);
        newRoutineEx.setTargetReps(reps);

        addedExercises.add(newRoutineEx);
        adapter.notifyItemInserted(addedExercises.size() - 1);
        updateEmptyState();
    }

    private void updateEmptyState() {
        boolean isEmpty = addedExercises.isEmpty();
        binding.tvEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        binding.rvExercises.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void setupObservers() {
        viewModel.getRoutineActionStatus().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;

            if (resource.status == Resource.Status.LOADING) {
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.btnSaveRoutine.setEnabled(false);
                return;
            }

            binding.progressBar.setVisibility(View.GONE);
            binding.btnSaveRoutine.setEnabled(true);

            if (resource.status == Resource.Status.SUCCESS) {
                boolean isEditing = getArguments() != null && getArguments().containsKey(ROUTINE_DATA_STRING);
                toast(isEditing ? "¡Rutina actualizada con éxito!" : "¡Rutina creada con éxito!");
                viewModel.getRoutineActionStatus().setValue(null);
                viewModel.fetchRoutines();
                Navigation.findNavController(requireView()).popBackStack();
            } else if (resource.status == Resource.Status.ERROR) {
                toast(resource.message);
                viewModel.getRoutineActionStatus().setValue(null);
            }
        });
    }

    private void saveRoutine() {
        String name = binding.etRoutineName.getText() != null ? binding.etRoutineName.getText().toString().trim() : "";
        String desc = binding.etRoutineDesc.getText() != null ? binding.etRoutineDesc.getText().toString().trim() : "";
        String imageUrl = binding.etRoutineImage.getText() != null ? binding.etRoutineImage.getText().toString().trim() : "";

        if (name.isEmpty()) {
            binding.tilRoutineName.setError("El nombre es obligatorio");
            return;
        }

        String finalPhotoData = null;

        if (selectedLocalImageUri != null) {
            finalPhotoData = ImageUtils.uriToBase64(requireContext().getContentResolver(), selectedLocalImageUri);
            if (finalPhotoData == null) {
                toast("Error al procesar la imagen seleccionada");
                return;
            }
        } else if (!imageUrl.isEmpty()) {
            finalPhotoData = imageUrl;
        }

        Routine routine = getRoutine(name, desc, finalPhotoData);

        viewModel.createRoutine(routine);
    }

    private Routine getRoutine(String name, String desc, String finalPhotoData) {
        List<RoutineExercise> cleanExercises = new ArrayList<>();
        for (RoutineExercise re : addedExercises) {
            RoutineExercise clean = new RoutineExercise();
            clean.setIdExercise(re.getIdExercise());
            clean.setTargetSets(re.getTargetSets());
            clean.setTargetReps(re.getTargetReps());
            clean.setTargetWeight(re.getTargetWeight());
            cleanExercises.add(clean);
        }

        Routine routine = new Routine();
        routine.setName(name);
        routine.setDescription(desc.isEmpty() ? null : desc);
        routine.setPhotoUrl(finalPhotoData);
        routine.setExercises(cleanExercises);
        routine.setIsPublic(binding.switchPublic.isChecked() ? 1 : 0);
        return routine;
    }

    private void updateRoutine(int routineId) {
        String name = binding.etRoutineName.getText() != null ? binding.etRoutineName.getText().toString().trim() : "";
        String desc = binding.etRoutineDesc.getText() != null ? binding.etRoutineDesc.getText().toString().trim() : "";
        String imageUrl = binding.etRoutineImage.getText() != null ? binding.etRoutineImage.getText().toString().trim() : "";

        if (name.isEmpty()) {
            binding.tilRoutineName.setError("El nombre es obligatorio");
            return;
        }

        String finalPhotoData = null;

        if (selectedLocalImageUri != null) {
            finalPhotoData = ImageUtils.uriToBase64(requireContext().getContentResolver(), selectedLocalImageUri);
            if (finalPhotoData == null) {
                toast("Error al procesar la imagen");
                return;
            }
        } else if (!imageUrl.isEmpty()) {
            finalPhotoData = imageUrl;
        }

        List<RoutineExercise> cleanExercises = new ArrayList<>();
        for (RoutineExercise re : addedExercises) {
            RoutineExercise clean = new RoutineExercise();
            clean.setIdExercise(re.getIdExercise());
            clean.setTargetSets(re.getTargetSets());
            clean.setTargetReps(re.getTargetReps());
            clean.setTargetWeight(re.getTargetWeight());
            cleanExercises.add(clean);
        }

        Routine routine = new Routine();
        routine.setIdRoutine(routineId);
        routine.setName(name);
        routine.setDescription(desc.isEmpty() ? null : desc);
        routine.setPhotoUrl(finalPhotoData);
        routine.setExercises(cleanExercises);
        routine.setIsPublic(binding.switchPublic.isChecked() ? 1 : 0);

        viewModel.updateRoutine(routineId, routine);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void fillFieldsForEdit(Routine routine) {
        binding.etRoutineName.setText(routine.getName());
        binding.etRoutineDesc.setText(routine.getDescription());
        binding.etRoutineImage.setText(routine.getPhotoUrl());
        binding.switchPublic.setChecked(routine.getIsPublic() == 1);

        addedExercises.clear();
        addedExercises.addAll(routine.getExercises());
        adapter.notifyDataSetChanged();
        updateEmptyState();

        binding.btnSaveRoutine.setOnClickListener(v -> updateRoutine(routine.getIdRoutine()));
    }

    private void toast(String msg) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}