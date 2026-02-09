package es.iesagora.fitjourney.ui.second.fitnesscenter.exercises;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import es.iesagora.fitjourney.R;
import es.iesagora.fitjourney.data.model.Exercise;
import es.iesagora.fitjourney.data.repositories.PreferencesRepository;
import es.iesagora.fitjourney.databinding.FragmentExerciseDetailBinding;
import es.iesagora.fitjourney.viewmodel.SecondViewModel;

public class ExerciseDetailFragment extends Fragment {

    private FragmentExerciseDetailBinding binding;
    private SecondViewModel viewModel;
    private Exercise currentExercise;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentExerciseDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(SecondViewModel.class);

        viewModel.exerciseSelected.observe(getViewLifecycleOwner(), exercise -> {
            if (exercise != null) {
                currentExercise = exercise;
                fillData(exercise);
            }
        });

        PreferencesRepository prefs = new PreferencesRepository(requireContext());

        if ("admin".equals(prefs.getRole())) {
            binding.adminPanel.setVisibility(View.VISIBLE);

            binding.btnEdit.setOnClickListener(v -> {
                if (currentExercise != null) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("exercise_data", currentExercise);
                    Navigation.findNavController(v).navigate(R.id.action_detail_to_form, bundle);
                }
            });

            binding.btnDelete.setOnClickListener(v -> {
                if (currentExercise != null) {
                    mostrarDialogoBorrar();
                }
            });

        } else {
            binding.adminPanel.setVisibility(View.GONE);
        }
    }

    private void mostrarDialogoBorrar() {
        new AlertDialog.Builder(requireContext())
                .setTitle("¿Eliminar Ejercicio?")
                .setMessage("Esta acción no se puede deshacer.")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    viewModel.deleteExercise(currentExercise.getId(), () -> {
                        Toast.makeText(getContext(), "Ejercicio eliminado", Toast.LENGTH_SHORT).show();
                        requireActivity().onBackPressed();
                    });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void fillData(Exercise exercise) {
        binding.tvDetailName.setText(exercise.getName());
        binding.tvDetailMuscle.setText(exercise.getMuscleGroup());
        binding.tvDetailDifficulty.setText(exercise.getDifficulty());
        binding.tvDetailInstructions.setText(getInstructions(exercise));

        String imageUrl = exercise.getImageUrl();

        if (imageUrl != null && (imageUrl.length() > 200 || !imageUrl.startsWith("http"))) {
            try {
                byte[] imageBytes = android.util.Base64.decode(imageUrl, android.util.Base64.DEFAULT);
                Glide.with(this)
                        .asBitmap()
                        .load(imageBytes)
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(android.R.drawable.ic_menu_report_image)
                        .centerCrop()
                        .into(binding.ivDetailImage);
            } catch (Exception e) {
                binding.ivDetailImage.setImageResource(android.R.drawable.ic_menu_report_image);
            }
        } else {
            Glide.with(this)
                    .load(imageUrl)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(android.R.drawable.ic_menu_report_image)
                    .centerCrop()
                    .into(binding.ivDetailImage);
        }
    }

    private String getInstructions(Exercise exercise) {
        if (exercise.getDescription() == null) return "";

        String description = exercise.getDescription();
        String[] steps = description.split("\\.");

        StringBuilder instructions = new StringBuilder();
        for (int i = 0; i < steps.length; i++) {
            if (!steps[i].trim().isEmpty()) {
                instructions.append(i + 1).append(". ").append(steps[i].trim()).append("\n\n");
            }
        }
        return instructions.toString();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}