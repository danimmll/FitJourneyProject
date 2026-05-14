package es.iesagora.fitjourney.ui.second.fitnesscenter.exercises;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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

public class FormExerciseFragment extends Fragment {

    private FragmentFormExerciseBinding binding;
    private SecondViewModel viewModel;
    private Exercise exerciseToEdit = null;

    private Uri selectedLocalImageUri = null;
    private String currentImageBase64 = null;

    private static final String PRINCIPIANTE_STRING = "Principiante";
    private static final String INTERMEDIO_STRING   = "Intermedio";
    private static final String EXPERTO_STRING      = "Experto";
    private static final String TYPE_HIPERTROFIA   = "Hipertrofia";
    private static final String TYPE_CARDIO        = "Cardio";
    private static final String TYPE_ESTIRAMIENTO  = "Estiramiento";

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

        setupDropdowns();

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

    private void setupDropdowns() {
        // Dropdown de dificultad
        String[] difficulties = {PRINCIPIANTE_STRING, INTERMEDIO_STRING, EXPERTO_STRING};
        ArrayAdapter<String> adapterDifficulty = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_dropdown_item_1line, difficulties);
        binding.actvDifficulty.setAdapter(adapterDifficulty);

        String[] types = {TYPE_HIPERTROFIA, TYPE_CARDIO, TYPE_ESTIRAMIENTO};
        ArrayAdapter<String> adapterType = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_dropdown_item_1line, types);
        binding.actvExerciseType.setAdapter(adapterType);
    }

    private void setupListeners() {
        binding.btnSelectImage.setOnClickListener(v -> getContent.launch("image/*"));
        binding.ivPreview.setOnClickListener(v -> getContent.launch("image/*"));

        binding.etImageUrl.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String url = binding.etImageUrl.getText() != null
                        ? binding.etImageUrl.getText().toString().trim() : "";
                if (!url.isEmpty()) {
                    selectedLocalImageUri = null;
                    currentImageBase64 = null;
                    ImageUtils.loadInto(requireContext(), url, binding.ivPreview, R.drawable.ic_ejercicios);
                }
            }
        });

        binding.btnSave.setOnClickListener(v -> saveExercise());
    }

    private void setupEditMode() {
        binding.tvFormTitle.setText(R.string.edit_exercise);
        binding.btnSave.setText(R.string.update);

        binding.etName.setText(exerciseToEdit.getName());
        binding.etDescription.setText(exerciseToEdit.getDescription());
        binding.etMuscle.setText(exerciseToEdit.getMuscleGroup());

        String diff = exerciseToEdit.getDifficulty();
        if (diff != null) {
            if (diff.equals("1"))      diff = PRINCIPIANTE_STRING;
            else if (diff.equals("2")) diff = INTERMEDIO_STRING;
            else if (diff.equals("3")) diff = EXPERTO_STRING;
            binding.actvDifficulty.setText(diff, false);
        }

        if (exerciseToEdit.getExerciseType() != null) {
            binding.actvExerciseType.setText(exerciseToEdit.getExerciseType(), false);
        }

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

    private void saveExercise() {
        String name        = Objects.requireNonNull(binding.etName.getText()).toString().trim();
        String description = Objects.requireNonNull(binding.etDescription.getText()).toString().trim();
        String imageUrlStr = Objects.requireNonNull(binding.etImageUrl.getText()).toString().trim();
        String muscle      = Objects.requireNonNull(binding.etMuscle.getText()).toString().trim();
        String diffStr     = binding.actvDifficulty.getText().toString().trim();

        String exerciseType = binding.actvExerciseType.getText().toString().trim();

        String videoUrl = "";
        if (binding.etVideoUrl.getText() != null) {
            videoUrl = binding.etVideoUrl.getText().toString().trim();
        }

        if (name.isEmpty() || muscle.isEmpty() || diffStr.isEmpty() || description.isEmpty()) {
            Toast.makeText(getContext(), "Faltan datos obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!exerciseType.isEmpty()
                && !exerciseType.equals(TYPE_HIPERTROFIA)
                && !exerciseType.equals(TYPE_CARDIO)
                && !exerciseType.equals(TYPE_ESTIRAMIENTO)) {
            Toast.makeText(getContext(), "Tipo de ejercicio no válido", Toast.LENGTH_SHORT).show();
            return;
        }

        String difficultyId = "1";
        if (diffStr.equals(INTERMEDIO_STRING)) difficultyId = "2";
        else if (diffStr.equals(EXPERTO_STRING)) difficultyId = "3";

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
        newExercise.setDifficulty(difficultyId);
        newExercise.setDescription(description);
        newExercise.setMuscleGroup(muscle);
        newExercise.setImageUrl(finalPhotoData);
        newExercise.setVideoUrl(videoUrl);
        newExercise.setExerciseType(exerciseType.isEmpty() ? null : exerciseType);

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