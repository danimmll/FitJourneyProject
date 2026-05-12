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
import es.iesagora.fitjourney.utils.ImageUtils;
import es.iesagora.fitjourney.viewmodel.SecondViewModel;

/**
 * Fragment encargado de la creación de nuevas rutinas de entrenamiento.
 *
 * <p>Permite al usuario crear una rutina con nombre, descripción,
 * imagen asociada (vía URL o galería) y una lista de ejercicios seleccionados del catálogo.</p>
 */
public class FormRoutineFragment extends Fragment {

    private FragmentFormRoutineBinding binding;
    private SecondViewModel viewModel;

    // Variable para almacenar la imagen local seleccionada de la galería
    private Uri selectedLocalImageUri = null;

    private final List<RoutineExercise> addedExercises = new ArrayList<>();
    private AddedExerciseAdapter adapter;

    /**
     * Lanzador para la galería. Carga la imagen, limpia la URL y muestra el preview.
     */
    private final ActivityResultLauncher<String> getContent =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedLocalImageUri = uri;
                    binding.etRoutineImage.setText("");
                    ImageUtils.loadInto(requireContext(), uri.toString(), binding.ivPhotoPreview, R.drawable.img_construccion);
                }
            });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
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

    /**
     * Inicializa el RecyclerView con los ejercicios añadidos a la rutina.
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
     * Configura los listeners de la interfaz.
     */
    private void setupListeners() {
        binding.toolbar.setNavigationOnClickListener(
                v -> Navigation.findNavController(v).navigateUp());

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
     * Muestra el diálogo para buscar un ejercicio del catálogo
     * e introducir series y repeticiones.
     */
    private void showAddExerciseDialog() {
        List<Exercise> allExercises = getExerciseCatalog();
        if (allExercises == null) return;

        View dialogView = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_add_routine_exercise, null);

        AutoCompleteTextView actvExercise = dialogView.findViewById(R.id.actvExercise);
        TextInputEditText etSets          = dialogView.findViewById(R.id.etSets);
        TextInputEditText etReps          = dialogView.findViewById(R.id.etReps);
        MaterialButton btnConfirmAdd      = dialogView.findViewById(R.id.btnConfirmAdd);

        actvExercise.setAdapter(new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                getExerciseNames(allExercises)));

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .create();
        dialog.show();

        btnConfirmAdd.setOnClickListener(v ->
                onConfirmAddExercise(actvExercise, etSets, etReps, allExercises, dialog));
    }

    /**
     * Devuelve la lista de ejercicios del catálogo, o null si aún no ha cargado.
     */
    private List<Exercise> getExerciseCatalog() {
        Resource<List<Exercise>> resource = viewModel.getExercises().getValue();
        if (resource == null || resource.data == null) {
            toast("Cargando catálogo de ejercicios, espera...");
            return null;
        }
        return resource.data;
    }

    /**
     * Extrae los nombres de una lista de ejercicios.
     */
    private List<String> getExerciseNames(List<Exercise> exercises) {
        List<String> names = new ArrayList<>();
        for (Exercise e : exercises) names.add(e.getName());
        return names;
    }

    /**
     * Maneja la confirmación del diálogo: valida los campos,
     * localiza el ejercicio y lo añade a la rutina.
     */
    private void onConfirmAddExercise(AutoCompleteTextView actvExercise,
                                      TextInputEditText etSets,
                                      TextInputEditText etReps,
                                      List<Exercise> allExercises,
                                      AlertDialog dialog) {
        String selectedName = actvExercise.getText().toString().trim();
        String setsStr = etSets.getText() != null ? etSets.getText().toString() : "";
        String repsStr = etReps.getText() != null ? etReps.getText().toString() : "";

        if (selectedName.isEmpty() || setsStr.isEmpty() || repsStr.isEmpty()) {
            toast("Rellena todos los campos");
            return;
        }

        Exercise selectedExercise = findExerciseByName(allExercises, selectedName);
        if (selectedExercise == null) {
            toast("El ejercicio no existe en el catálogo");
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

    /**
     * Busca un ejercicio por nombre ignorando mayúsculas/minúsculas.
     * Devuelve null si no existe en el catálogo.
     */
    private Exercise findExerciseByName(List<Exercise> exercises, String name) {
        for (Exercise e : exercises) {
            if (e.getName().equalsIgnoreCase(name)) return e;
        }
        return null;
    }

    /**
     * Parsea series y repeticiones a enteros.
     * Muestra un Toast y devuelve null si el formato no es válido.
     */
    private int[] parseSetsAndReps(String setsStr, String repsStr) {
        try {
            return new int[]{ Integer.parseInt(setsStr), Integer.parseInt(repsStr) };
        } catch (NumberFormatException e) {
            toast("Series y repeticiones deben ser números válidos");
            return null;
        }
    }

    /**
     * Construye un {@link RoutineExercise} y lo inserta en la lista y el adapter.
     */
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

    /**
     * Muestra u oculta el mensaje de lista vacía según el estado actual.
     */
    private void updateEmptyState() {
        boolean isEmpty = addedExercises.isEmpty();
        binding.tvEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        binding.rvExercises.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    /**
     * Observa el estado de la acción de creación de rutina para
     * actualizar la UI y navegar atrás al completarse.
     */
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
                toast("Rutina guardada correctamente");
                viewModel.getRoutineActionStatus().setValue(null);
                Navigation.findNavController(requireView()).navigateUp();
            } else if (resource.status == Resource.Status.ERROR) {
                toast(resource.message);
                viewModel.getRoutineActionStatus().setValue(null);
            }
        });
    }

    /**
     * Recoge los datos del formulario, los valida y solicita
     * la creación de la rutina al ViewModel.
     */
    private void saveRoutine() {
        String name = binding.etRoutineName.getText() != null
                ? binding.etRoutineName.getText().toString().trim() : "";
        String desc = binding.etRoutineDesc.getText() != null
                ? binding.etRoutineDesc.getText().toString().trim() : "";
        String imageUrl = binding.etRoutineImage.getText() != null
                ? binding.etRoutineImage.getText().toString().trim() : "";

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

    /** Muestra un Toast corto. */
    private void toast(String msg) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}