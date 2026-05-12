package es.iesagora.fitjourney.ui.second.fitnesscenter.exercises;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;

import java.util.Objects;

import es.iesagora.fitjourney.R;
import es.iesagora.fitjourney.data.model.Exercise;
import es.iesagora.fitjourney.databinding.FragmentFormExerciseBinding;
import es.iesagora.fitjourney.utils.ImageUtils;
import es.iesagora.fitjourney.viewmodel.SecondViewModel;

/**
 * Fragment encargado de crear y editar ejercicios dentro del Fitness Center.
 *
 * <p>Este fragment permite al usuario administrador crear nuevos ejercicios o editar existentes,
 * gestionando información como nombre, grupo muscular, dificultad, descripción e imagen.</p>
 *
 * Responsabilidades principales:
 * <ul>
 *     <li>Mostrar formulario de creación/edición de ejercicios.</li>
 *     <li>Permitir selección de imagen desde la galería.</li>
 *     <li>Convertir imágenes a Base64 para almacenamiento.</li>
 *     <li>Gestionar modo creación o edición según argumentos recibidos.</li>
 *     <li>Enviar datos al ViewModel para persistencia.</li>
 * </ul>
 */
public class FormExerciseFragment extends Fragment {

    /** ViewBinding del layout del formulario de ejercicios */
    private FragmentFormExerciseBinding binding;

    /** ViewModel compartido para gestión de ejercicios */
    private SecondViewModel viewModel;

    /** Ejercicio a editar (null si es creación) */
    private Exercise exerciseToEdit = null;

    /** Imagen actual en formato Base64 */
    private String currentImageBase64 = null;

    /**
     * Launcher para seleccionar imagen desde la galería del dispositivo.
     * Convierte la imagen seleccionada a Base64 y la muestra en preview.
     */
    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    binding.ivPreview.setImageURI(imageUri);
                    currentImageBase64 = ImageUtils.uriToBase64(
                            requireContext().getContentResolver(),
                            imageUri
                    );
                }
            }
    );

    /**
     * Infla la vista del fragmento.
     *
     * @param inflater inflador del layout
     * @param container contenedor padre
     * @param savedInstanceState estado previo
     * @return vista raíz del fragmento
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentFormExerciseBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * Inicializa la interfaz y determina si el fragmento está en modo creación o edición.
     * Configura:
     * - ViewModel
     * - Carga de datos si es edición
     * - Eventos de botones (imagen y guardar)
     *
     * @param view vista raíz
     * @param savedInstanceState estado previo
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(SecondViewModel.class);

        if (getArguments() != null && getArguments().containsKey("exercise_data")) {
            exerciseToEdit = (Exercise) getArguments().getSerializable("exercise_data");
        }

        if (exerciseToEdit != null) {
            setupEditMode();
        } else {
            binding.tvFormTitle.setText(R.string.new_exercise);
            binding.btnSave.setText(R.string.create);
        }

        binding.btnSelectImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryLauncher.launch(intent);
        });

        binding.btnSave.setOnClickListener(v -> saveExercise());
    }

    /**
     * Configura el formulario en modo edición cargando los datos existentes del ejercicio.
     */
    private void setupEditMode() {
        binding.tvFormTitle.setText(R.string.edit_exercise);
        binding.btnSave.setText(R.string.update);

        binding.etName.setText(exerciseToEdit.getName());
        binding.etMuscle.setText(exerciseToEdit.getMuscleGroup());
        binding.etDifficulty.setText(exerciseToEdit.getDifficulty());
        binding.etDescription.setText(exerciseToEdit.getDescription());

        if (exerciseToEdit.getImageUrl() != null && !exerciseToEdit.getImageUrl().isEmpty()) {
            currentImageBase64 = exerciseToEdit.getImageUrl();

            if (currentImageBase64.length() > 200) {
                Bitmap bmp = ImageUtils.base64ToBitmap(currentImageBase64);
                binding.ivPreview.setImageBitmap(bmp);
            } else {
                Glide.with(this).load(currentImageBase64).into(binding.ivPreview);
            }
        }
    }

    /**
     * Valida y guarda un ejercicio (creación o actualización).
     * - Valida campos obligatorios
     * - Construye objeto Exercise
     * - Llama al ViewModel correspondiente
     */
    private void saveExercise() {
        String name       = Objects.requireNonNull(binding.etName.getText()).toString().trim();
        String muscle     = Objects.requireNonNull(binding.etMuscle.getText()).toString().trim();
        String difficulty = Objects.requireNonNull(binding.etDifficulty.getText()).toString().trim();
        String description = Objects.requireNonNull(binding.etDescription.getText()).toString().trim();

        if (name.isEmpty() || muscle.isEmpty()) {
            Toast.makeText(getContext(), "Faltan datos obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        Exercise newExercise = new Exercise();
        newExercise.setName(name);
        newExercise.setDifficulty(difficulty);
        newExercise.setDescription(description);
        newExercise.setMuscleGroup(muscle);
        newExercise.setImageUrl(currentImageBase64);

        if (exerciseToEdit == null) {
            viewModel.createExercise(newExercise, () -> {
                Toast.makeText(getContext(), R.string.exercise_created, Toast.LENGTH_SHORT).show();
                requireActivity().getOnBackPressedDispatcher().onBackPressed();
            });
        } else {
            newExercise.setIdExercise(exerciseToEdit.getIdExercise());
            viewModel.updateExercise(exerciseToEdit.getIdExercise(), newExercise, () -> {
                viewModel.selectExercise(newExercise);
                requireActivity().getOnBackPressedDispatcher().onBackPressed();
            });
        }
    }

    /**
     * Limpia referencias del binding para evitar memory leaks.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}