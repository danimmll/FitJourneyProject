package es.iesagora.fitjourney.ui.second.workouthistory;

import android.annotation.SuppressLint;
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
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import es.iesagora.fitjourney.R;
import es.iesagora.fitjourney.data.model.Workout;
import es.iesagora.fitjourney.data.remote.Resource;
import es.iesagora.fitjourney.databinding.DialogConfirmDeleteBinding;
import es.iesagora.fitjourney.databinding.FragmentWorkoutHistoryBinding;
import es.iesagora.fitjourney.ui.second.adapters.WorkoutHistoryAdapter;
import es.iesagora.fitjourney.viewmodel.SecondViewModel;

/**
 * Fragment que muestra el historial de entrenamientos del usuario.
 * Permite ver el detalle de cada entrenamiento y eliminar registros.
 */
public class WorkoutHistoryFragment extends Fragment {

    private FragmentWorkoutHistoryBinding binding;
    private SecondViewModel viewModel;
    private WorkoutHistoryAdapter adapter;

    /**
     * Infla la vista del fragmento.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentWorkoutHistoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * Inicializa ViewModel, RecyclerView, adaptador y observadores.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(SecondViewModel.class);

        adapter = new WorkoutHistoryAdapter(
                workout -> {
                    Bundle bundle = new Bundle();
                    bundle.putInt("workout_id", workout.getIdWorkout());

                    try {
                        Navigation.findNavController(requireView())
                                .navigate(R.id.workoutHistoryDetailFragment, bundle);
                    } catch (Exception ignored) {}
                },
                this::showDeleteDialog
        );

        binding.rvWorkoutHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvWorkoutHistory.setAdapter(adapter);

        setupObservers();
        viewModel.fetchWorkoutHistory();
    }

    /**
     * Configura observadores del historial y acciones de entrenamiento.
     */
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
                        binding.rvWorkoutHistory.setVisibility(View.VISIBLE);
                    } else {
                        binding.tvEmptyHistory.setVisibility(View.VISIBLE);
                        binding.rvWorkoutHistory.setVisibility(View.GONE);
                    }
                    break;

                case ERROR:
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), resource.message, Toast.LENGTH_SHORT).show();
                    break;
            }
        });

        viewModel.getWorkoutActionStatus().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;

            if (resource.status == Resource.Status.SUCCESS) {
                Toast.makeText(getContext(), "Entrenamiento borrado", Toast.LENGTH_SHORT).show();
                viewModel.getWorkoutActionStatus().setValue(null);

            } else if (resource.status == Resource.Status.ERROR) {
                Toast.makeText(getContext(), resource.message, Toast.LENGTH_SHORT).show();
                viewModel.getWorkoutActionStatus().setValue(null);
            }
        });
    }

    /**
     * Muestra diálogo de confirmación para eliminar un entrenamiento.
     */
    @SuppressLint("SetTextI18n")
    private void showDeleteDialog(Workout workout) {

        DialogConfirmDeleteBinding dialogBinding =
                DialogConfirmDeleteBinding.inflate(getLayoutInflater());

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogBinding.getRoot())
                .create();

        String routineName = workout.getRoutine() != null
                ? workout.getRoutine().getName()
                : "este entrenamiento";

        dialogBinding.tvDeleteTitle.setText("Eliminar Registro");
        dialogBinding.tvDeleteMessage.setText(
                "¿Estás seguro de que quieres borrar el registro de " +
                        routineName +
                        "? Esta acción no se puede deshacer."
        );

        dialogBinding.btnCancelDelete.setOnClickListener(v -> dialog.dismiss());

        dialogBinding.btnConfirmDelete.setOnClickListener(v -> {
            dialog.dismiss();
            viewModel.deleteWorkout(workout.getIdWorkout());
        });

        dialog.show();
    }

    /**
     * Limpia el binding al destruir la vista.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}