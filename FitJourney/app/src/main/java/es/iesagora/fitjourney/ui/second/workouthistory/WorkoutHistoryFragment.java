package es.iesagora.fitjourney.ui.second.workouthistory;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import es.iesagora.fitjourney.data.remote.Resource;
import es.iesagora.fitjourney.databinding.FragmentWorkoutHistoryBinding;
import es.iesagora.fitjourney.ui.second.adapters.WorkoutHistoryAdapter;
import es.iesagora.fitjourney.viewmodel.SecondViewModel;

public class WorkoutHistoryFragment extends Fragment {

    private FragmentWorkoutHistoryBinding binding;
    private SecondViewModel viewModel;
    private WorkoutHistoryAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentWorkoutHistoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(SecondViewModel.class);

        adapter = new WorkoutHistoryAdapter();
        binding.rvWorkoutHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvWorkoutHistory.setAdapter(adapter);

        setupObservers();
        viewModel.fetchWorkoutHistory(); // Cargamos los datos
    }

    private void setupObservers() {
        viewModel.getWorkoutHistory().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;
            switch (resource.status) {
                case LOADING:
                    binding.progressBar.setVisibility(View.VISIBLE);
                    break;
                case SUCCESS:
                    binding.progressBar.setVisibility(View.GONE);
                    if (resource.data != null && !resource.data.isEmpty()) {
                        adapter.setWorkouts(resource.data);
                        binding.tvEmptyHistory.setVisibility(View.GONE);
                    } else {
                        binding.tvEmptyHistory.setVisibility(View.VISIBLE);
                    }
                    break;
                case ERROR:
                    binding.progressBar.setVisibility(View.GONE);
                    break;
            }
        });
    }
}