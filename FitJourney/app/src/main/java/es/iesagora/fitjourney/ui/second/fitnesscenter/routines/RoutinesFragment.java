package es.iesagora.fitjourney.ui.second.fitnesscenter.routines;

import android.app.AlertDialog;
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
import es.iesagora.fitjourney.data.remote.Resource;
import es.iesagora.fitjourney.data.repositories.PreferencesRepository;
import es.iesagora.fitjourney.databinding.FragmentRoutinesBinding;
import es.iesagora.fitjourney.ui.second.adapters.RoutineAdapter;
import es.iesagora.fitjourney.viewmodel.SecondViewModel;

/**
 * Pantalla que muestra las rutinas disponibles.
 * Permite al usuario ver todas las rutinas de la app o solo las que ha creado él.
 * Desde aquí se puede crear una nueva rutina o eliminar las propias.
 */
public class RoutinesFragment extends Fragment {

    private FragmentRoutinesBinding binding;
    private SecondViewModel viewModel;
    private RoutineAdapter adapter;
    private List<Routine> allRoutines = new ArrayList<>();
    private int currentUserId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentRoutinesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(SecondViewModel.class);

        // Limpiamos el estado al entrar por si venimos de borrar una rutina previamente
        viewModel.getRoutineActionStatus().setValue(null);

        PreferencesRepository prefs = new PreferencesRepository(requireContext());
        String userIdStr = prefs.getUserId();
        try {
            currentUserId = userIdStr != null ? Integer.parseInt(userIdStr) : -1;
        } catch (NumberFormatException e) {
            currentUserId = -1;
        }
        boolean isAdmin = "admin".equalsIgnoreCase(prefs.getRole());

        setupRecyclerView(isAdmin);
        setupChips();

        binding.btnCreateRoutine.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.formRoutineFragment)
        );

        viewModel.fetchRoutines();

        setupObservers();
    }

    /**
     * Se queda "escuchando" a la API para actualizar la pantalla
     * cuando llegan las rutinas o cuando se borra alguna.
     */
    private void setupObservers() {
        viewModel.getRoutines().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;

            switch (resource.status) {
                case LOADING:
                    binding.progressBar.setVisibility(View.VISIBLE);
                    break;
                case SUCCESS:
                    binding.progressBar.setVisibility(View.GONE);
                    allRoutines = resource.data != null ? resource.data : new ArrayList<>();
                    applyFilter();
                    break;
                case ERROR:
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Error: " + resource.message, Toast.LENGTH_SHORT).show();
                    break;
            }
        });

        viewModel.getRoutineActionStatus().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;

            if (resource.status == Resource.Status.SUCCESS) {
                if (resource.message != null && resource.message.toLowerCase().contains("eliminada")) {
                    Toast.makeText(getContext(), "Rutina borrada con éxito", Toast.LENGTH_SHORT).show();
                    viewModel.getRoutineActionStatus().setValue(null); // Limpiamos el aviso para que no se repita
                }
            } else if (resource.status == Resource.Status.ERROR) {
                Toast.makeText(getContext(), "Error al borrar: " + resource.message, Toast.LENGTH_LONG).show();
                viewModel.getRoutineActionStatus().setValue(null);
            }
        });
    }

    /**
     * Prepara la lista visual y define qué pasa al tocar una rutina o al darle a borrar.
     */
    private void setupRecyclerView(boolean isAdmin) {
        binding.rvRoutines.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new RoutineAdapter(currentUserId, isAdmin, new RoutineAdapter.OnRoutineListener() {
            @Override
            public void onRoutineClick(Routine routine) {
                Bundle bundle = new Bundle();
                bundle.putInt("routine_id", routine.getIdRoutine());

                try {
                    Navigation.findNavController(requireView())
                            .navigate(R.id.routineDetailFragment, bundle);
                } catch (IllegalArgumentException e) {
                    Toast.makeText(getContext(), "Falta crear el fragmento de detalles en nav_second.xml", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onDeleteClick(Routine routine) {
                showDeleteDialog(routine);
            }
        });

        binding.rvRoutines.setAdapter(adapter);
    }

    /**
     * Configura los botones de filtro (Todas / Personales) para que cambien de color al pulsarlos.
     */
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

    /**
     * Filtra la lista de rutinas en base a lo que el usuario haya seleccionado.
     */
    private void applyFilter() {
        if (allRoutines == null) return;
        List<Routine> filtered = new ArrayList<>();

        if (binding.chipAll.isChecked()) {
            for (Routine r : allRoutines) {
                boolean isPublic = isPublic(r.getIsPublic());
                int routineUserId = r.getIdUser();
                if (isPublic || routineUserId == currentUserId) {
                    filtered.add(r);
                }
            }
        } else if (binding.chipPersonal.isChecked()) {
            for (Routine r : allRoutines) {
                int routineUserId = r.getIdUser();
                if (routineUserId == currentUserId) {
                    filtered.add(r);
                }
            }
        }

        adapter.setRoutines(filtered);

        if (filtered.isEmpty()) {
            binding.rvRoutines.setVisibility(View.GONE);
            binding.tvEmptyRoutines.setVisibility(View.VISIBLE);
        } else {
            binding.rvRoutines.setVisibility(View.VISIBLE);
            binding.tvEmptyRoutines.setVisibility(View.GONE);
        }
    }

    /**
     * Muestra una advertencia antes de borrar la rutina definitivamente.
     */
    private void showDeleteDialog(Routine routine) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Borrar Rutina")
                .setMessage("¿Estás seguro de que quieres eliminar la rutina '" +
                        routine.getName() + "'?\n\nLa rutina y su configuración desaparecerán para siempre.")
                .setPositiveButton("Borrar para siempre", (dialog, which) -> {
                    int idToBorrar = routine.getIdRoutine();
                    viewModel.deleteRoutine(idToBorrar);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private boolean isPublic(Integer isPublic) {
        if (isPublic != null) {
            return isPublic == 1;
        } else {
            return false;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}