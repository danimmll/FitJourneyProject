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
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;

import es.iesagora.fitjourney.R;
import es.iesagora.fitjourney.data.repositories.PreferencesRepository;
import es.iesagora.fitjourney.databinding.FragmentExercisesBinding;
import es.iesagora.fitjourney.ui.second.adapters.ExerciseAdapter;
import es.iesagora.fitjourney.viewmodel.SecondViewModel;

public class ExercisesFragment extends Fragment {

    private FragmentExercisesBinding binding;
    private SecondViewModel viewModel;
    private ExerciseAdapter adapter;
    private PreferencesRepository prefs;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentExercisesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(SecondViewModel.class);
        prefs = new PreferencesRepository(requireContext());

        setupRecyclerView();

        viewModel.getExercises().observe(getViewLifecycleOwner(), resource -> {
            if (resource != null) {
                switch (resource.status) {
                    case LOADING:
                        binding.progressBar.setVisibility(View.VISIBLE);
                        binding.rvExercises.setVisibility(View.GONE);
                        binding.tvError.setVisibility(View.GONE);
                        break;

                    case SUCCESS:
                        binding.progressBar.setVisibility(View.GONE);

                        if (resource.data != null && !resource.data.isEmpty()) {
                            binding.rvExercises.setVisibility(View.VISIBLE);
                            binding.tvError.setVisibility(View.GONE);
                            adapter.setExercises(resource.data);
                        } else {
                            binding.tvError.setText(R.string.no_exercises_aviable);
                            binding.tvError.setVisibility(View.VISIBLE);
                            binding.rvExercises.setVisibility(View.GONE);
                            adapter.setExercises(new ArrayList<>());
                        }
                        break;

                    case ERROR:
                        binding.progressBar.setVisibility(View.GONE);
                        binding.rvExercises.setVisibility(View.GONE);
                        binding.tvError.setText("Error al conectar con el servidor.");
                        binding.tvError.setVisibility(View.VISIBLE);
                        Toast.makeText(getContext(), resource.message, Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });

        binding.svExercises.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String exerciseName) {
                viewModel.searchExercises(exerciseName);
                return true;
            }
        });

        String role = prefs.getRole();
        if ("admin".equalsIgnoreCase(role)) {
            binding.fabAddExercise.setVisibility(View.VISIBLE);
            binding.fabAddExercise.setOnClickListener(v -> {
                Navigation.findNavController(v).navigate(R.id.formExerciseFragment);
            });
        } else {
            binding.fabAddExercise.setVisibility(View.GONE);
        }

        if (viewModel.getExercises().getValue() == null) {
            viewModel.fetchExercises();
        }
    }

    private void setupRecyclerView() {
        binding.rvExercises.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ExerciseAdapter(requireContext(), viewModel);
        binding.rvExercises.setAdapter(adapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}