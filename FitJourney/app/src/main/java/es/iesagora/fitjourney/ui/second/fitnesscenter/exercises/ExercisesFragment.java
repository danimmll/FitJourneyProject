package es.iesagora.fitjourney.ui.second.fitnesscenter.exercises;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import es.iesagora.fitjourney.R;
import es.iesagora.fitjourney.data.model.Exercise;
import es.iesagora.fitjourney.data.repositories.PreferencesRepository;
import es.iesagora.fitjourney.databinding.FragmentExercisesBinding;
import es.iesagora.fitjourney.ui.second.adapters.ExerciseAdapter;
import es.iesagora.fitjourney.viewmodel.SecondViewModel;

public class ExercisesFragment extends Fragment {

    private FragmentExercisesBinding binding;
    private SecondViewModel viewModel;
    private ExerciseAdapter adapter;
    private PreferencesRepository prefs;
    private List<Exercise> allExercises = new ArrayList<>();
    private String currentQuery = "";
    private String selectedDifficulty = "Todos";
    private final String[] difficulties = {"Todos", "Principiante", "Intermedio", "Difícil"};

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentExercisesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(SecondViewModel.class);
        prefs = new PreferencesRepository(requireContext());

        setupRecyclerView();
        setupCategories();

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

                            if (currentQuery.isEmpty() && selectedDifficulty.equals("Todos")) {
                                allExercises = new ArrayList<>(resource.data);
                            }
                            applyFilters();
                        } else {
                            showEmptyState();
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
            public boolean onQueryTextSubmit(String query) { return false; }

            @Override
            public boolean onQueryTextChange(String newText) {
                currentQuery = newText;
                applyFilters();
                return true;
            }
        });

        if ("admin".equalsIgnoreCase(prefs.getRole())) {
            binding.fabAddExercise.setVisibility(View.VISIBLE);
            binding.fabAddExercise.setOnClickListener(v ->
                    Navigation.findNavController(v).navigate(R.id.formExerciseFragment)
            );
        } else {
            binding.fabAddExercise.setVisibility(View.GONE);
        }

        if (viewModel.getExercises().getValue() == null) {
            viewModel.fetchExercises();
        }
    }

    private void setupCategories() {
        binding.llCategories.removeAllViews();

        for (String diff : difficulties) {
            MaterialButton btn = new MaterialButton(requireContext(), null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
            btn.setText(diff);
            btn.setCornerRadius(50);
            btn.setAllCaps(false);

            if (diff.equals(selectedDifficulty)) {
                btn.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.mainrosa)));
                btn.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
                btn.setStrokeWidth(0);
            } else {
                btn.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
                btn.setTextColor(ContextCompat.getColor(requireContext(), R.color.text));
                btn.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.mainrosa)));
                btn.setStrokeWidth(3);
            }

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 16, 0);
            btn.setLayoutParams(params);

            btn.setOnClickListener(v -> {
                selectedDifficulty = diff;
                setupCategories();
                applyFilters();
            });

            binding.llCategories.addView(btn);
        }
    }

    private void applyFilters() {
        List<Exercise> filteredList = new ArrayList<>();

        for (Exercise ex : allExercises) {
            boolean matchesSearch = currentQuery.isEmpty() ||
                    (ex.getName() != null && ex.getName().toLowerCase().contains(currentQuery.toLowerCase()));

            boolean matchesDifficulty = selectedDifficulty.equals("Todos");
            if (!matchesDifficulty && ex.getDifficulty() != null) {
                String exDiff = String.valueOf(ex.getDifficulty()).toLowerCase();

                if (selectedDifficulty.equals("Principiante") && (exDiff.contains("principiante") || exDiff.equals("1"))) matchesDifficulty = true;
                if (selectedDifficulty.equals("Intermedio") && (exDiff.contains("intermedio") || exDiff.equals("2"))) matchesDifficulty = true;
                if (selectedDifficulty.equals("Difícil") && (exDiff.contains("difícil") || exDiff.contains("dificil") || exDiff.contains("avanzado") || exDiff.equals("3"))) matchesDifficulty = true;
            }

            if (matchesSearch && matchesDifficulty) {
                filteredList.add(ex);
            }
        }

        if (filteredList.isEmpty() && !allExercises.isEmpty()) {
            showEmptyState();
        } else {
            binding.tvError.setVisibility(View.GONE);
            binding.rvExercises.setVisibility(View.VISIBLE);
            adapter.setExercises(filteredList);
        }
    }

    private void showEmptyState() {
        binding.tvError.setText("No se encontraron ejercicios con esos filtros.");
        binding.tvError.setVisibility(View.VISIBLE);
        binding.rvExercises.setVisibility(View.GONE);
        adapter.setExercises(new ArrayList<>());
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