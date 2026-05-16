package es.iesagora.fitjourney.ui.second.fitnesscenter.routines;

import android.annotation.SuppressLint;
import android.content.Intent;
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
import es.iesagora.fitjourney.data.model.Routine;
import es.iesagora.fitjourney.data.remote.Resource;
import es.iesagora.fitjourney.data.repositories.PreferencesRepository;
import es.iesagora.fitjourney.databinding.DialogConfirmDeleteBinding;
import es.iesagora.fitjourney.databinding.FragmentRoutineDetailBinding;
import es.iesagora.fitjourney.ui.second.adapters.RoutineDetailExerciseAdapter;
import es.iesagora.fitjourney.ui.workout.WorkoutActivity;
import es.iesagora.fitjourney.utils.ImageUtils;
import es.iesagora.fitjourney.viewmodel.SecondViewModel;

/**
 * Fragmento encargado de mostrar los detalles de una rutina específica.
 * Presenta la información general (imagen, nombre) y la lista de ejercicios que la componen.
 * Gestiona los permisos de usuario para habilitar la edición y eliminación (solo creador o administrador)
 * y permite iniciar un entrenamiento basado en esta rutina.
 */
public class RoutineDetailFragment extends Fragment {

    private FragmentRoutineDetailBinding binding;
    private SecondViewModel viewModel;
    private Routine currentRoutine;
    private int routineId = -1;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentRoutineDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(SecondViewModel.class);

        if (getArguments() != null) {
            routineId = getArguments().getInt("routine_id", -1);
        }

        if (routineId != -1) {
            loadRoutineData();
        } else {
            Toast.makeText(getContext(), "Error al cargar la rutina", Toast.LENGTH_SHORT).show();
            Navigation.findNavController(requireView()).navigateUp();
        }
    }

    /**
     * Solicita al ViewModel los datos de la rutina mediante su ID y observa la respuesta.
     * En caso de éxito, actualiza la interfaz y configura los botones correspondientes.
     */
    private void loadRoutineData() {
        viewModel.fetchRoutineById(routineId);

        viewModel.getSingleRoutine().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;

            if (resource.status == Resource.Status.SUCCESS && resource.data != null) {
                currentRoutine = resource.data;
                fillRoutineData(currentRoutine);
                setupButtons(currentRoutine);
            } else if (resource.status == Resource.Status.ERROR) {
                Toast.makeText(getContext(), "Error: " + resource.message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Rellena la interfaz de usuario con la información de la rutina.
     * Carga la imagen de portada y configura el RecyclerView con los ejercicios asignados.
     *
     * @param routine La rutina cuyos datos se van a mostrar.
     */
    private void fillRoutineData(Routine routine) {
        binding.tvRoutineDetailName.setText(routine.getName());
        ImageUtils.loadInto(requireContext(), routine.getDisplayImage(),
                binding.ivRoutineDetail, R.drawable.img_construccion);

        if (routine.getExercises() != null && !routine.getExercises().isEmpty()) {
            RoutineDetailExerciseAdapter adapter = new RoutineDetailExerciseAdapter(routine.getExercises(), exercise -> {
                Bundle bundle = new Bundle();
                bundle.putSerializable("exercise_data", exercise);

                try {
                    Navigation.findNavController(requireView())
                            .navigate(R.id.action_routineDetail_to_exerciseDetail, bundle);
                } catch (IllegalArgumentException e) {
                    Toast.makeText(getContext(),
                            "Error de navegación: comprueba nav_second.xml",
                            Toast.LENGTH_SHORT).show();
                }
            });

            binding.rvRoutineExercises.setLayoutManager(new LinearLayoutManager(getContext()));
            binding.rvRoutineExercises.setAdapter(adapter);
            binding.rvRoutineExercises.setVisibility(View.VISIBLE);
        } else {
            binding.rvRoutineExercises.setVisibility(View.GONE);
        }
    }

    /**
     * Configura la visibilidad y las acciones de los botones según los permisos del usuario.
     * Muestra el panel de propietario (editar/eliminar) si el usuario es el creador de la rutina o un administrador.
     * Habilita el botón principal para iniciar la actividad de entrenamiento.
     *
     * @param routine La rutina actual, utilizada para comprobar la autoría y pasar su ID.
     */
    private void setupButtons(Routine routine) {
        PreferencesRepository prefs = new PreferencesRepository(requireContext());
        boolean isAdmin = "admin".equalsIgnoreCase(prefs.getRole());

        int currentUserId = -1;
        try {
            currentUserId = Integer.parseInt(prefs.getUserId() != null ? prefs.getUserId() : "-1");
        } catch (NumberFormatException ignored) {}

        boolean isOwner = (routine.getIdUser() == currentUserId);

        if (isOwner || isAdmin) {
            binding.ownerPanel.setVisibility(View.VISIBLE);

            binding.btnEditRoutine.setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                bundle.putSerializable("routine_data", routine);
                Navigation.findNavController(v).navigate(R.id.formRoutineFragment, bundle);
            });

            binding.btnDeleteRoutine.setOnClickListener(v -> showDeleteDialog(routine));
        } else {
            binding.ownerPanel.setVisibility(View.GONE);
        }

        binding.btnStartWorkout.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(requireContext(), WorkoutActivity.class);
                intent.putExtra("ROUTINE_ID", routine.getIdRoutine());
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(getContext(), "Crea primero la WorkoutActivity", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Muestra un cuadro de diálogo de confirmación para eliminar la rutina actual.
     * Si se confirma, solicita la eliminación al ViewModel, actualiza la lista global y cierra el fragmento.
     *
     * @param routine La rutina que se desea eliminar.
     */
    @SuppressLint("SetTextI18n")
    private void showDeleteDialog(Routine routine) {
        DialogConfirmDeleteBinding dialogBinding = DialogConfirmDeleteBinding.inflate(getLayoutInflater());

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogBinding.getRoot())
                .create();

        dialogBinding.tvDeleteTitle.setText("Eliminar Rutina");
        dialogBinding.tvDeleteMessage.setText("¿Estás seguro de que quieres borrar la rutina '" + routine.getName() + "'?");

        dialogBinding.btnCancelDelete.setOnClickListener(v -> dialog.dismiss());

        dialogBinding.btnConfirmDelete.setOnClickListener(v -> {
            dialog.dismiss();
            viewModel.deleteRoutine(routine.getIdRoutine());
            Toast.makeText(getContext(), "Borrando rutina...", Toast.LENGTH_SHORT).show();

            viewModel.fetchRoutines();
            Navigation.findNavController(requireView()).navigateUp();
        });

        dialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}