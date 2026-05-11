package es.iesagora.fitjourney.ui.second.fitnesscenter.exercises;

import android.annotation.SuppressLint;
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
import androidx.appcompat.widget.SearchView;
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

/**
 * Fragment encargado de mostrar la lista de ejercicios disponibles en el Fitness Center.
 *
 * <p>Este fragment permite al usuario:</p>
 * <ul>
 *     <li>Visualizar ejercicios obtenidos desde el ViewModel.</li>
 *     <li>Filtrar por nombre mediante búsqueda.</li>
 *     <li>Filtrar por nivel de dificultad (Principiante, Intermedio, Experto).</li>
 *     <li>Acceder a la creación de ejercicios si el usuario es administrador.</li>
 * </ul>
 *
 * <p>Incluye gestión de estados de carga, error y vacío, así como un sistema de filtros dinámicos.</p>
 */
public class ExercisesFragment extends Fragment {

    /** ViewBinding del layout del fragmento */
    private FragmentExercisesBinding binding;

    /** ViewModel compartido que gestiona la carga de ejercicios */
    private SecondViewModel viewModel;

    /** Adaptador del RecyclerView de ejercicios */
    private ExerciseAdapter adapter;

    /** Lista completa de ejercicios obtenidos desde backend */
    private List<Exercise> allExercises = new ArrayList<>();

    /** Texto actual de búsqueda */
    private String currentQuery = "";

    /** Dificultad seleccionada en los filtros */
    private String selectedDifficulty = TODOS_STRING;

    /** Lista de niveles de dificultad disponibles */
    private final String[] difficulties = {TODOS_STRING, PRINCIPIANTE_STRING, INTERMEDIO_STRING,
            EXPERTO_STRING};

    private static final String TODOS_STRING = "Todos";

    private static final String PRINCIPIANTE_STRING = "Principiante";

    private static final String INTERMEDIO_STRING = "Intermedio";

    private static final String EXPERTO_STRING = "Experto";

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
        binding = FragmentExercisesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * Inicializa la UI, ViewModel, observers y eventos.
     * Se encarga de:
     * - Configurar RecyclerView
     * - Inicializar filtros por categoría
     * - Observar cambios de ejercicios
     * - Manejar búsqueda en tiempo real
     * - Controlar acceso de administrador
     */
    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(SecondViewModel.class);
        PreferencesRepository prefs = new PreferencesRepository(requireContext());

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
                            allExercises = new ArrayList<>(resource.data);

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

        binding.svExercises.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

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

    /**
     * Configura los botones de filtros por dificultad.
     * Cambia estilos dinámicamente según selección.
     */
    private void setupCategories() {
        binding.llCategories.removeAllViews();

        for (String diff : difficulties) {
            MaterialButton btn = new MaterialButton(
                    requireContext(),
                    null,
                    com.google.android.material.R.attr.materialButtonOutlinedStyle
            );

            btn.setText(diff);
            btn.setCornerRadius(50);
            btn.setAllCaps(false);

            if (diff.equals(selectedDifficulty)) {
                btn.setBackgroundTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(requireContext(), R.color.mainrosa)));
                btn.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
                btn.setStrokeWidth(0);
            } else {
                btn.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
                btn.setTextColor(ContextCompat.getColor(requireContext(), R.color.text));
                btn.setStrokeColor(ColorStateList.valueOf(
                        ContextCompat.getColor(requireContext(), R.color.mainrosa)));
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

    /**
     * Aplica filtros de búsqueda y dificultad sobre la lista de ejercicios.
     *
     * <p>Este método filtra la lista completa de ejercicios aplicando dos criterios:</p>
     * <ul>
     *     <li>Filtro por búsqueda de texto en el nombre del ejercicio.</li>
     *     <li>Filtro por nivel de dificultad seleccionado.</li>
     * </ul>
     *
     * <p>El resultado filtrado se muestra en el RecyclerView.
     * Si no hay resultados, se muestra un estado vacío.</p>
     */
    private void applyFilters() {

        List<Exercise> filteredList = new ArrayList<>();
        String normalizedQuery = currentQuery == null ? "" : currentQuery.toLowerCase();

        for (Exercise ex : allExercises) {
            String exerciseName = ex.getName() != null ? ex.getName().toLowerCase() : "";
            boolean matchesQuery = normalizedQuery.isEmpty() || exerciseName.contains(normalizedQuery);
            if (matchesQuery && isMatchesDifficulty(ex)) {
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

    /**
     * Comprueba si un ejercicio coincide con la dificultad seleccionada actualmente.
     *
     * <p>Este método evalúa el nivel de dificultad del ejercicio frente al filtro activo
     * del usuario. Soporta valores tanto en texto (Principiante, Intermedio, Experto)
     * como en formato numérico ("1", "2", "3") provenientes del backend.</p>
     *
     * @param ex ejercicio a evaluar
     * @return {@code true} si el ejercicio coincide con la dificultad seleccionada
     *         o si el filtro está en "Todos"; {@code false} en caso contrario
     */
    private boolean isMatchesDifficulty(Exercise ex) {
        boolean matchesDifficulty = selectedDifficulty.equals(TODOS_STRING);

        if (!matchesDifficulty && ex.getDifficulty() != null) {
            String exDiff = ex.getDifficulty().trim();

            matchesDifficulty = true;

            if (selectedDifficulty.equalsIgnoreCase(PRINCIPIANTE_STRING)
                    && (exDiff.equalsIgnoreCase(PRINCIPIANTE_STRING) || exDiff.equals("1"))) {
                //COMPROBACION VACIA PUES CASA CON LA DIFICULTAD SOLO SE PONE EN FALSE SI NO CASA CON NIGUNA
            } else if (selectedDifficulty.equalsIgnoreCase(INTERMEDIO_STRING)
                    && (exDiff.equalsIgnoreCase(INTERMEDIO_STRING) || exDiff.equals("2"))) {
                //COMPROBACION VACIA PUES CASA CON LA DIFICULTAD SOLO SE PONE EN FALSE SI NO CASA CON NIGUNA
            } else if (selectedDifficulty.equalsIgnoreCase(EXPERTO_STRING)
                    && (exDiff.equalsIgnoreCase(EXPERTO_STRING) || exDiff.equals("3"))) {
                //COMPROBACION VACIA PUES CASA CON LA DIFICULTAD SOLO SE PONE EN FALSE SI NO CASA CON NIGUNA
            } else {
                matchesDifficulty = false;
            }
        }
        return matchesDifficulty;
    }

    /**
     * Muestra estado vacío cuando no hay resultados.
     */
    @SuppressLint("SetTextI18n")
    private void showEmptyState() {
        binding.tvError.setText("No se encontraron ejercicios con esos filtros.");
        binding.tvError.setVisibility(View.VISIBLE);
        binding.rvExercises.setVisibility(View.GONE);
        adapter.setExercises(new ArrayList<>());
    }

    /**
     * Configura el RecyclerView de ejercicios.
     */
    private void setupRecyclerView() {
        binding.rvExercises.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ExerciseAdapter(requireContext(), viewModel);
        binding.rvExercises.setAdapter(adapter);
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