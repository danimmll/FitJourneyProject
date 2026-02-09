package es.iesagora.fitjourney.ui.second.fitnesscenter.exercises;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

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
                fillData(exercise);
            }
        });

        PreferencesRepository prefs = new PreferencesRepository(requireContext());

        if ("admin".equals(prefs.getRole())) {

            binding.adminPanel.setVisibility(View.VISIBLE);

            binding.btnEdit.setOnClickListener(v -> {
                Toast.makeText(getContext(), "Función Editar: Próximamente", Toast.LENGTH_SHORT).show();
            });

            binding.btnDelete.setOnClickListener(v -> {
                Toast.makeText(getContext(), "Función Borrar: Próximamente", Toast.LENGTH_SHORT).show();
            });

        } else {
            binding.adminPanel.setVisibility(View.GONE);
        }
    }

    private void fillData(Exercise exercise) {
        binding.tvDetailName.setText(exercise.getName());
        binding.tvDetailMuscle.setText(exercise.getMuscleGroup());
        binding.tvDetailDifficulty.setText(exercise.getDifficulty());
        binding.tvDetailInstructions.setText(getInstructions(exercise));

        Glide.with(this)
                .load(exercise.getImageUrl())
                .transition(DrawableTransitionOptions.withCrossFade())
                .placeholder(R.drawable.ic_launcher_background)
                .error(android.R.drawable.ic_menu_report_image)
                .centerCrop()
                .into(binding.ivDetailImage);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private String getInstructions(Exercise exercise) {
        String description = exercise.getDescription();

        String []steps = description.split("\\.");

        String instructions = "";
        for (int i = 0; i < steps.length; i++) {
            String number = String.valueOf(i+1);
            instructions = instructions + number + " " + steps[i] +"\n";
        }

        return instructions;

    }
}