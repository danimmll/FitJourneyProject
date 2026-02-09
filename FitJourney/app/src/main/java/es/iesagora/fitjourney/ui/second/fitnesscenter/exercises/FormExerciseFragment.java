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

import es.iesagora.fitjourney.R;
import es.iesagora.fitjourney.data.model.Exercise;
import es.iesagora.fitjourney.databinding.FragmentFormExerciseBinding;
import es.iesagora.fitjourney.utils.ImageUtils;
import es.iesagora.fitjourney.viewmodel.SecondViewModel;

public class FormExerciseFragment extends Fragment {

    private FragmentFormExerciseBinding binding;
    private SecondViewModel viewModel;
    private Exercise exerciseToEdit = null;
    private String currentImageBase64 = null;

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    binding.ivPreview.setImageURI(imageUri);
                    currentImageBase64 = ImageUtils.uriToBase64(requireContext().getContentResolver(), imageUri);
                }
            }
    );

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
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

        binding.btnSelectImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryLauncher.launch(intent);
        });

        binding.btnSave.setOnClickListener(v -> saveExercise());
    }

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

    private void saveExercise() {
        String name = binding.etName.getText().toString().trim();
        String muscle = binding.etMuscle.getText().toString().trim();
        String difficulty = binding.etDifficulty.getText().toString().trim();
        String description = binding.etDescription.getText().toString().trim();

        if (name.isEmpty() || muscle.isEmpty()) {
            Toast.makeText(getContext(), "Faltan datos obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        Exercise newExercise = new Exercise();
        newExercise.setName(name);
        newExercise.setMuscleGroup(muscle);
        newExercise.setDifficulty(difficulty);
        newExercise.setDescription(description);
        newExercise.setImageUrl(currentImageBase64);

        if (exerciseToEdit == null) {
            viewModel.createExercise(newExercise, () -> {
                Toast.makeText(getContext(), R.string.exercise_created, Toast.LENGTH_SHORT).show();
                requireActivity().onBackPressed();
            });
        } else {
            newExercise.setId(exerciseToEdit.getId());

            viewModel.updateExercise(exerciseToEdit.getId(), newExercise, () -> {

                viewModel.selectExercise(newExercise);

                Toast.makeText(getContext(), R.string.exercise_updated, Toast.LENGTH_SHORT).show();
                requireActivity().onBackPressed();
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}