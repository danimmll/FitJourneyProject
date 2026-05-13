package es.iesagora.fitjourney.ui.second.fitnesscenter.routines;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
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
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import es.iesagora.fitjourney.R;
import es.iesagora.fitjourney.data.model.Exercise;
import es.iesagora.fitjourney.data.model.Routine;
import es.iesagora.fitjourney.data.model.RoutineExercise;
import es.iesagora.fitjourney.data.remote.Resource;
import es.iesagora.fitjourney.databinding.FragmentFormRoutineBinding;
import es.iesagora.fitjourney.ui.second.adapters.AddedExerciseAdapter;
import es.iesagora.fitjourney.ui.second.adapters.SelectExerciseAdapter;
import es.iesagora.fitjourney.utils.ImageUtils;
import es.iesagora.fitjourney.viewmodel.SecondViewModel;

/**
 * Fragment encargado de la creación de nuevas rutinas de entrenamiento.
 *
 * Permite al usuario crear una rutina con nombre, descripción,
 * imagen asociada (vía URL o galería) y una lista de ejercicios seleccionados del catálogo.
 */
public class FormRoutineFragment extends Fragment {

    private FragmentFormRoutineBinding binding;
    private SecondViewModel viewModel;

    private Uri selectedLocalImageUri = null;

    private final List<RoutineExercise> addedExercises = new ArrayList<>();
    private AddedExerciseAdapter adapter;

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

    /**
     * Muestra el diálogo para buscar un ejercicio del catálogo con un buscador dinámico
     * e introducir series y repeticiones.
     */
    private void showAddExerciseDialog() {
        List<Exercise> allExercises = getExerciseCatalog();
        if (allExercises == null) return;

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_routine_exercise, null);

        RecyclerView rvExercisePicker = dialogView.findViewById(R.id.rvExercisePicker);
        SearchView searchExercise = dialogView.findViewById(R.id.searchExercise);
        TextView tvSelectedExercise = dialogView.findViewById(R.id.tvSelectedExercise);
        TextInputEditText etSets = dialogView.findViewById(R.id.etSets);
        TextInputEditText etReps = dialogView.findViewById(R.id.etReps);
        MaterialButton btnConfirmAdd = dialogView.findViewById(R.id.btnConfirmAdd);

        final Exercise[] selectedExercise = {null};

        SelectExerciseAdapter pickerAdapter = new SelectExerciseAdapter(exercise -> {
            selectedExercise[0] = exercise;
            tvSelectedExercise.setText("Seleccionado: " + exercise.getName());
        });

        rvExercisePicker.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        rvExercisePicker.setAdapter(pickerAdapter);
        pickerAdapter.submitList(allExercises);

        searchExercise.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

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
                .setView(dialogView)
                .create();
        dialog.show();

        btnConfirmAdd.setOnClickListener(v -> {
            if (selectedExercise[0] == null) {
                toast("Por favor, selecciona un ejercicio deslizando en la lista.");
                return;
            }
            onConfirmAddExercise(selectedExercise[0], etSets, etReps, dialog);
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

    private void onConfirmAddExercise(Exercise selectedExercise, TextInputEditText etSets, TextInputEditText etReps, AlertDialog dialog) {
        String setsStr = etSets.getText() != null ? etSets.getText().toString() : "";
        String repsStr = etReps.getText() != null ? etReps.getText().toString() : "";

        if (setsStr.isEmpty() || repsStr.isEmpty()) {
            toast("Rellena las series y repeticiones");
            return;
        }

        int[] parsed = parseSetsAndReps(setsStr, repsStr);
        if (parsed == null) return;

        int sets = parsed[0];
        int reps = parsed[1];

        if (sets <= 0 || reps <= 0) {
            toast("Series y repeticiones deben ser mayores que 0");
            return;
        }

        addRoutineExercise(selectedExercise, sets, reps);
        dialog.dismiss();
    }

    private int[] parseSetsAndReps(String setsStr, String repsStr) {
        try {
            return new int[]{ Integer.parseInt(setsStr), Integer.parseInt(repsStr) };
        } catch (NumberFormatException e) {
            toast("Series y repeticiones deben ser números válidos");
            return null;
        }
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
                if (resource.message != null && resource.message.toLowerCase().contains("creada")) {
                    toast("Rutina guardada correctamente");
                    viewModel.getRoutineActionStatus().setValue(null);
                    Navigation.findNavController(requireView()).navigateUp();
                }
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

        Routine routine = new Routine();
        routine.setName(name);
        routine.setDescription(desc.isEmpty() ? null : desc);
        routine.setPhotoUrl(finalPhotoData);
        routine.setExercises(addedExercises);

        boolean isPublic = binding.switchPublic.isChecked();
        routine.setIsPublic(isPublic ? 1 : 0);

        viewModel.createRoutine(routine);
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