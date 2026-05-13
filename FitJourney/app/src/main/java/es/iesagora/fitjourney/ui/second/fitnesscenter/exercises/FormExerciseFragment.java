package es.iesagora.fitjourney.ui.second.fitnesscenter.exercises;

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
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

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
 * gestionando información como nombre, grupo muscular, dificultad, descripción, imagen
 * (por URL o galería local) y enlace al videotutorial de YouTube.</p>
 */
public class FormExerciseFragment extends Fragment {

    private FragmentFormExerciseBinding binding;
    private SecondViewModel viewModel;
    private Exercise exerciseToEdit = null;

    private Uri selectedLocalImageUri = null;
    private String currentImageBase64 = null;

    /**
     * Launcher para seleccionar imagen desde la galería usando el contrato GetContent.
     */
    private final ActivityResultLauncher<String> getContent =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedLocalImageUri = uri;
                    currentImageBase64 = null;
                    if (binding != null) {
                        binding.etImageUrl.setText("");
                    }
                    ImageUtils.loadInto(requireContext(), uri.toString(), binding.ivPreview, R.drawable.ic_ejercicios);
                }
            });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFormExerciseBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

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

        setupListeners();
    }

    /**
     * Configura los eventos de los botones y los focos de los campos de texto.
     */
    private void setupListeners() {
        binding.btnSelectImage.setOnClickListener(v -> getContent.launch("image/*"));
        binding.ivPreview.setOnClickListener(v -> getContent.launch("image/*"));

        binding.etImageUrl.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String url = binding.etImageUrl.getText() != null ? binding.etImageUrl.getText().toString().trim() : "";
                if (!url.isEmpty()) {
                    selectedLocalImageUri = null;
                    currentImageBase64 = null;
                    ImageUtils.loadInto(requireContext(), url, binding.ivPreview, R.drawable.ic_ejercicios);
                }
            }
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
        binding.etDifficulty.setText(exerciseToEdit.getDifficulty());
        binding.etDescription.setText(exerciseToEdit.getDescription());

        String muscle = exerciseToEdit.getMuscleGroup();
        binding.etMuscle.setText(muscle);

        if (exerciseToEdit.getVideoUrl() != null) {
            binding.etVideoUrl.setText(exerciseToEdit.getVideoUrl());
        }

        String existingImage = exerciseToEdit.getImageUrl();

        if (existingImage != null && !existingImage.isEmpty()) {
            if (existingImage.startsWith("http")) {
                binding.etImageUrl.setText(existingImage);
            } else {
                currentImageBase64 = existingImage;
            }

            ImageUtils.loadInto(requireContext(), existingImage, binding.ivPreview, R.drawable.ic_ejercicios);
        }
    }

    /**
     * Valida y guarda un ejercicio (creación o actualización).
     */
    private void saveExercise() {
        String name = Objects.requireNonNull(binding.etName.getText()).toString().trim();
        String muscle = Objects.requireNonNull(binding.etMuscle.getText()).toString().trim();
        String difficulty = Objects.requireNonNull(binding.etDifficulty.getText()).toString().trim();
        String description = Objects.requireNonNull(binding.etDescription.getText()).toString().trim();
        String imageUrlStr = Objects.requireNonNull(binding.etImageUrl.getText()).toString().trim();

        String videoUrl = "";
        if (binding.etVideoUrl.getText() != null) {
            videoUrl = binding.etVideoUrl.getText().toString().trim();
        }

        if (name.isEmpty() || muscle.isEmpty() || difficulty.isEmpty() || description.isEmpty()) {
            Toast.makeText(getContext(), "Faltan datos obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        String finalPhotoData;

        if (selectedLocalImageUri != null) {
            finalPhotoData = ImageUtils.uriToBase64(requireContext().getContentResolver(), selectedLocalImageUri);
            if (finalPhotoData == null) {
                Toast.makeText(getContext(), "Error al procesar la imagen de la galería", Toast.LENGTH_SHORT).show();
                return;
            }
        } else if (!imageUrlStr.isEmpty()) {
            finalPhotoData = imageUrlStr;
        } else {
            finalPhotoData = currentImageBase64;
        }

        Exercise newExercise = new Exercise();
        newExercise.setName(name);
        newExercise.setDifficulty(difficulty);
        newExercise.setDescription(description);
        newExercise.setMuscleGroup(muscle);
        newExercise.setImageUrl(finalPhotoData);
        newExercise.setVideoUrl(videoUrl);

        if (exerciseToEdit == null) {
            viewModel.createExercise(newExercise, () -> {
                Toast.makeText(getContext(), R.string.exercise_created, Toast.LENGTH_SHORT).show();
                requireActivity().getOnBackPressedDispatcher().onBackPressed();
            });
        } else {
            int exId = exerciseToEdit.getIdExercise();
            newExercise.setIdExercise(exId);

            viewModel.updateExercise(exId, newExercise, () -> {
                viewModel.selectExercise(newExercise);
                requireActivity().getOnBackPressedDispatcher().onBackPressed();
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}