package es.iesagora.fitjourney.ui.second.fitnesscenter.routines;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import es.iesagora.fitjourney.R;
import es.iesagora.fitjourney.data.model.Exercise;
import es.iesagora.fitjourney.data.model.Routine;
import es.iesagora.fitjourney.data.model.RoutineExercise;
import es.iesagora.fitjourney.data.remote.Resource;
import es.iesagora.fitjourney.databinding.FragmentFormRoutineBinding;
import es.iesagora.fitjourney.ui.second.adapters.AddedExerciseAdapter;
import es.iesagora.fitjourney.viewmodel.SecondViewModel;

/**
 * Fragment encargado de la creación de nuevas rutinas de entrenamiento.
 *
 * <p>Permite al usuario administrador o autorizado crear una rutina
 * con nombre, descripción, imagen asociada y una lista de ejercicios.</p>
 */
public class FormRoutineFragment extends Fragment {

    private FragmentFormRoutineBinding binding;
    private SecondViewModel viewModel;
    private Uri selectedLocalImageUri;

    // Lista en memoria de los ejercicios que vamos añadiendo a la rutina
    private final List<RoutineExercise> addedExercises = new ArrayList<>();
    private AddedExerciseAdapter adapter;

    private final ActivityResultLauncher<String> getContent =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedLocalImageUri = uri;
                    binding.etRoutineImage.setText("");
                    Glide.with(this).load(uri).centerCrop().into(binding.ivPhotoPreview);
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

        // Pedimos los ejercicios a la API por si no estaban cargados
        if (viewModel.getExercises().getValue() == null) {
            viewModel.fetchExercises();
        }

        setupRecyclerView();
        setupListeners();
        setupObservers();
    }

    /**
     * Inicializa el RecyclerView para mostrar los ejercicios añadidos a la rutina.
     */
    private void setupRecyclerView() {
        adapter = new AddedExerciseAdapter(addedExercises, position -> {
            addedExercises.remove(position);
            adapter.notifyItemRemoved(position);
            updateEmptyState();
        });
        binding.rvExercises.setAdapter(adapter);
        updateEmptyState();
    }

    /**
     * Configura los listeners para los elementos de la interfaz de usuario.
     */
    private void setupListeners() {
        binding.toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        binding.ivPhotoPreview.setOnClickListener(v -> getContent.launch("image/*"));

        binding.etRoutineImage.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String url = Objects.requireNonNull(binding.etRoutineImage.getText()).toString().trim();
                if (!url.isEmpty()) {
                    selectedLocalImageUri = null;
                    Glide.with(this).load(url).centerCrop().placeholder(R.drawable.img_construccion).into(binding.ivPhotoPreview);
                }
            }
        });

        // Abrimos el buscador de ejercicios
        binding.btnAddExercise.setOnClickListener(v -> showAddExerciseDialog());

        binding.btnSaveRoutine.setOnClickListener(v -> saveRoutine());
    }

    /**
     * Muestra un cuadro de diálogo con un AutoCompleteTextView para buscar
     * un ejercicio por su nombre y pedir series/repeticiones.
     */
    private void showAddExerciseDialog() {
        if (viewModel.getExercises().getValue() == null || viewModel.getExercises().getValue().data == null) {
            Toast.makeText(getContext(), "Cargando catálogo de ejercicios, espera...", Toast.LENGTH_SHORT).show();
            return;
        }

        List<Exercise> allExercises = viewModel.getExercises().getValue().data;
        List<String> exerciseNames = allExercises.stream()
                .map(Exercise::getName)
                .collect(Collectors.toList());

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_routine_exercise, null);
        AutoCompleteTextView actvExercise = dialogView.findViewById(R.id.actvExercise);
        TextInputEditText etSets = dialogView.findViewById(R.id.etSets);
        TextInputEditText etReps = dialogView.findViewById(R.id.etReps);
        MaterialButton btnConfirmAdd = dialogView.findViewById(R.id.btnConfirmAdd);

        ArrayAdapter<String> searchAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, exerciseNames);
        actvExercise.setAdapter(searchAdapter);

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .create();
        dialog.show();

        btnConfirmAdd.setOnClickListener(v -> {
            String selectedName = actvExercise.getText().toString().trim();
            String setsStr = etSets.getText() != null ? etSets.getText().toString() : "";
            String repsStr = etReps.getText() != null ? etReps.getText().toString() : "";

            if (selectedName.isEmpty() || setsStr.isEmpty() || repsStr.isEmpty()) {
                Toast.makeText(getContext(), "Rellena todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            Exercise selectedExercise = allExercises.stream()
                    .filter(ex -> ex.getName().equalsIgnoreCase(selectedName))
                    .findFirst()
                    .orElse(null);

            if (selectedExercise == null) {
                Toast.makeText(getContext(), "El ejercicio no existe en el catálogo", Toast.LENGTH_SHORT).show();
                return;
            }

            RoutineExercise newRoutineEx = new RoutineExercise();
            newRoutineEx.setId_exercise(selectedExercise.getId_exercise());
            newRoutineEx.setExercise(selectedExercise);
            newRoutineEx.setTarget_sets(Integer.parseInt(setsStr));
            newRoutineEx.setTarget_reps(Integer.parseInt(repsStr));

            addedExercises.add(newRoutineEx);
            adapter.notifyItemInserted(addedExercises.size() - 1);
            updateEmptyState();

            dialog.dismiss();
        });
    }

    /**
     * Actualiza el estado visual según si hay o no ejercicios en la rutina.
     */
    private void updateEmptyState() {
        if (addedExercises.isEmpty()) {
            binding.tvEmptyState.setVisibility(View.VISIBLE);
            binding.rvExercises.setVisibility(View.GONE);
        } else {
            binding.tvEmptyState.setVisibility(View.GONE);
            binding.rvExercises.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Observa el estado del recurso proporcionado por el ViewModel al crear la rutina.
     */
    private void setupObservers() {
        viewModel.getRoutineActionStatus().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;
            if (resource.status == Resource.Status.LOADING) {
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.btnSaveRoutine.setEnabled(false);
            } else {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnSaveRoutine.setEnabled(true);
                if (resource.status == Resource.Status.SUCCESS) {
                    if (resource.message != null && resource.message.toLowerCase().contains("creada")) {
                        Toast.makeText(getContext(), "Rutina guardada correctamente", Toast.LENGTH_SHORT).show();
                        viewModel.getRoutineActionStatus().setValue(null);
                        Navigation.findNavController(requireView()).navigateUp();
                    }
                } else if (resource.status == Resource.Status.ERROR) {
                    Toast.makeText(getContext(), resource.message, Toast.LENGTH_SHORT).show();
                    viewModel.getRoutineActionStatus().setValue(null);
                }
            }
        });
    }

    /**
     * Recoge los datos del formulario, valida y llama al ViewModel para que la envíe.
     */
    private void saveRoutine() {
        String name = binding.etRoutineName.getText() != null ? binding.etRoutineName.getText().toString().trim() : "";
        String desc = binding.etRoutineDesc.getText() != null ? binding.etRoutineDesc.getText().toString().trim() : "";
        String imageUrl = binding.etRoutineImage.getText() != null ? binding.etRoutineImage.getText().toString().trim() : "";

        if (name.isEmpty()) {
            binding.tilRoutineName.setError("El nombre es obligatorio");
            return;
        }

        Routine routine = new Routine();
        routine.setName(name);
        routine.setDescription(desc.isEmpty() ? null : desc);

        if (selectedLocalImageUri != null) {
            routine.setPhoto_url(selectedLocalImageUri.toString());
        } else {
            routine.setPhoto_url(imageUrl.isEmpty() ? null : imageUrl);
        }

        routine.setExercises(addedExercises);

        viewModel.createRoutine(routine);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}