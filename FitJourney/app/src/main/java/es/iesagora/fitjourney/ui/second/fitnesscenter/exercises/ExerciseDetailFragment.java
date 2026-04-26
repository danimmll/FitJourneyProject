package es.iesagora.fitjourney.ui.second.fitnesscenter.exercises;

import android.os.Bundle;
import android.util.Base64;
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
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import es.iesagora.fitjourney.R;
import es.iesagora.fitjourney.data.model.Exercise;
import es.iesagora.fitjourney.data.repositories.PreferencesRepository;
import es.iesagora.fitjourney.databinding.FragmentExerciseDetailBinding;
import es.iesagora.fitjourney.ui.second.adapters.InstructionAdapter; // <-- Importamos tu nuevo Adapter
import es.iesagora.fitjourney.viewmodel.SecondViewModel;

public class ExerciseDetailFragment extends Fragment {

    private FragmentExerciseDetailBinding binding;
    private SecondViewModel viewModel;
    private Exercise currentExercise;
    private InstructionAdapter instructionAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentExerciseDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(SecondViewModel.class);

        instructionAdapter = new InstructionAdapter();
        binding.rvInstructions.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvInstructions.setAdapter(instructionAdapter);

        viewModel.getSelectedExercise().observe(getViewLifecycleOwner(), exercise -> {
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
                    showDeleteDialog();
                }
            });
        } else {
            binding.adminPanel.setVisibility(View.GONE);
        }
    }

    private void showDeleteDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("¿Eliminar Ejercicio?")
                .setMessage("Esta acción no se puede deshacer.")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    viewModel.deleteExercise(currentExercise.getId_exercise(), () -> {
                        Toast.makeText(getContext(), "Ejercicio eliminado", Toast.LENGTH_SHORT).show();
                        requireActivity().onBackPressed();
                    });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void fillData(Exercise exercise) {
        binding.tvDetailName.setText(exercise.getName());
        binding.tvDetailMuscle.setText(exercise.getMuscle_group());

        String diff = exercise.getDifficulty();
        if (diff != null) {
            if (diff.equals("1")) diff = "Principiante";
            if (diff.equals("2")) diff = "Intermedio";
            if (diff.equals("3")) diff = "Difícil";
            binding.tvDetailDifficulty.setText(diff);
        } else {
            binding.tvDetailDifficulty.setText("N/A");
        }

        instructionAdapter.setInstructions(getInstructionsList(exercise));

        String imageUrl = exercise.getImage_url();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            if (imageUrl.length() > 200 || !imageUrl.startsWith("http")) {
                try {
                    byte[] imageBytes = Base64.decode(imageUrl, Base64.DEFAULT);
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
        } else {
            binding.ivDetailImage.setImageResource(R.drawable.ic_launcher_background);
        }
    }

    private List<String> getInstructionsList(Exercise exercise) {
        List<String> list = new ArrayList<>();
        if (exercise.getDescription() == null || exercise.getDescription().isEmpty()) {
            list.add("No hay instrucciones detalladas para este ejercicio.");
            return list;
        }

        String[] steps = exercise.getDescription().split("\\.");
        for (String step : steps) {
            if (!step.trim().isEmpty()) {
                list.add(step.trim() + ".");
            }
        }
        return list;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}