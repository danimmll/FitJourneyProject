package es.iesagora.fitjourney.ui.second.fitnesscenter.exercises;

import android.content.Intent;
import android.net.Uri;
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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import es.iesagora.fitjourney.R;
import es.iesagora.fitjourney.data.model.Exercise;
import es.iesagora.fitjourney.data.repositories.PreferencesRepository;
import es.iesagora.fitjourney.databinding.FragmentExerciseDetailBinding;
import es.iesagora.fitjourney.ui.second.adapters.InstructionAdapter;
import es.iesagora.fitjourney.utils.ImageUtils;
import es.iesagora.fitjourney.viewmodel.SecondViewModel;

/**
 * Fragment encargado de mostrar el detalle completo de un ejercicio.
 *
 * <p>Este fragment permite visualizar toda la información de un ejercicio seleccionado,
 * incluyendo nombre, grupo muscular, dificultad, imagen e instrucciones paso a paso.</p>
 *
 * <p>También permite a usuarios administradores editar o eliminar ejercicios.</p>
 *
 * Responsabilidades principales:
 * <ul>
 *     <li>Obtener el ejercicio seleccionado desde el ViewModel.</li>
 *     <li>Renderizar la información detallada del ejercicio.</li>
 *     <li>Parsear la descripción en pasos/instrucciones.</li>
 *     <li>Gestionar carga de imágenes en Base64 o URL.</li>
 *     <li>Permitir edición y eliminación para administradores.</li>
 * </ul>
 */
public class ExerciseDetailFragment extends Fragment {

    /** ViewBinding del layout del detalle del ejercicio */
    private FragmentExerciseDetailBinding binding;

    /** ViewModel compartido que gestiona los ejercicios */
    private SecondViewModel viewModel;

    /** Ejercicio actualmente mostrado en pantalla */
    private Exercise currentExercise;

    /** Adaptador de instrucciones paso a paso */
    private InstructionAdapter instructionAdapter;

    /**
     * Infla la vista del fragmento.
     *
     * @param inflater inflador del layout
     * @param container contenedor padre
     * @param savedInstanceState estado previo del fragmento
     * @return vista raíz del fragmento
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentExerciseDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * Inicializa la interfaz, ViewModel y lógica del fragmento.
     * Configura:
     * - RecyclerView de instrucciones
     * - Observador del ejercicio seleccionado
     * - Panel de administración (editar/eliminar)
     *
     * @param view vista raíz del fragmento
     * @param savedInstanceState estado previo
     */
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

    /**
     * Muestra un diálogo de confirmación antes de eliminar un ejercicio.
     */
    private void showDeleteDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("¿Eliminar Ejercicio?")
                .setMessage("Esta acción no se puede deshacer.")
                .setPositiveButton("Eliminar", (dialog, which) ->
                        viewModel.deleteExercise(currentExercise.getIdExercise(), () -> {
                            Toast.makeText(getContext(), "Ejercicio eliminado", Toast.LENGTH_SHORT).show();
                            requireActivity().getOnBackPressedDispatcher().onBackPressed();
                        }))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    /**
     * Rellena la interfaz con los datos del ejercicio seleccionado.
     *
     * @param exercise ejercicio a mostrar
     */
    private void fillData(Exercise exercise) {

        String youtubeUrl = exercise.getVideoUrl();

        if (youtubeUrl != null && !youtubeUrl.trim().isEmpty()) {
            binding.btnYoutube.setVisibility(View.VISIBLE);
            binding.btnYoutube.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(youtubeUrl));
                startActivity(intent);
            });
        } else {
            binding.btnYoutube.setVisibility(View.GONE);
        }

        binding.tvDetailName.setText(exercise.getName());
        binding.tvDetailMuscle.setText(exercise.getMuscleGroup());

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

        ImageUtils.loadInto(
                requireContext(),
                exercise.getImageUrl(),
                binding.ivDetailImage,
                R.drawable.ic_ejercicios
        );
    }

    /**
     * Convierte la descripción del ejercicio en una lista de instrucciones.
     *
     * @param exercise ejercicio del que se extraen instrucciones
     * @return lista de pasos formateados
     */
    private List<String> getInstructionsList(Exercise exercise) {
        if (exercise.getDescription() == null || exercise.getDescription().isEmpty()) {
            List<String> list = new ArrayList<>();
            list.add("No hay instrucciones detalladas para este ejercicio.");
            return list;
        }

        return Stream.of(exercise.getDescription().split("\\."))
                .map(String::trim)
                .filter(step -> !step.isEmpty())
                .map(step -> step + ".")
                .collect(Collectors.toList());
    }

    /**
     * Limpia referencias del binding para evitar memory leaks.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}