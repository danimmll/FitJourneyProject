package es.iesagora.fitjourney.ui.second.fitnesscenter.routines;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

import es.iesagora.fitjourney.R;
import es.iesagora.fitjourney.data.model.Routine;
import es.iesagora.fitjourney.databinding.FragmentRoutinesBinding;
import es.iesagora.fitjourney.viewmodel.SecondViewModel;

public class RoutinesFragment extends Fragment {

    private FragmentRoutinesBinding binding;
    private SecondViewModel viewModel;
    private List<Routine> allRoutines = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentRoutinesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(SecondViewModel.class);

        setupRecyclerView();
        setupChips();

        binding.btnCreateRoutine.setOnClickListener(v ->
                Toast.makeText(getContext(), "Crear rutina en desarrollo 🛠️", Toast.LENGTH_SHORT).show()
        );
    }

    private void setupRecyclerView() {
        binding.rvRoutines.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void setupChips() {
        int colorRosa = ContextCompat.getColor(requireContext(), R.color.mainrosa);
        int colorSurface = ContextCompat.getColor(requireContext(), R.color.surface);

        binding.chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            for (int i = 0; i < group.getChildCount(); i++) {
                Chip chip = (Chip) group.getChildAt(i);
                if (chip.isChecked()) {
                    chip.setChipBackgroundColor(ColorStateList.valueOf(colorRosa));
                } else {
                    chip.setChipBackgroundColor(ColorStateList.valueOf(colorSurface));
                }
            }
            applyFilter();
        });
    }

    private void applyFilter() {
        List<Routine> filtered = new ArrayList<>();

        if (binding.chipAll.isChecked()) {
            filtered.addAll(allRoutines);
        } else if (binding.chipStrength.isChecked()) {
            for (Routine r : allRoutines) if ("Fuerza".equalsIgnoreCase(r.getType())) filtered.add(r);
        } else if (binding.chipHypert.isChecked()) {
            for (Routine r : allRoutines) if ("Hipertrofia".equalsIgnoreCase(r.getType())) filtered.add(r);
        } else if (binding.chipCardio.isChecked()) {
            for (Routine r : allRoutines) if ("Cardio".equalsIgnoreCase(r.getType())) filtered.add(r);
        }

        if (filtered.isEmpty() && !allRoutines.isEmpty()) {
            binding.rvRoutines.setVisibility(View.GONE);
            binding.tvEmptyRoutines.setVisibility(View.VISIBLE);
            binding.tvEmptyRoutines.setText("No hay rutinas de este tipo.");
        } else if (!allRoutines.isEmpty()) {
            binding.rvRoutines.setVisibility(View.VISIBLE);
            binding.tvEmptyRoutines.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}