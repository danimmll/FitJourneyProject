package es.iesagora.fitjourney.ui.second.fitnesscenter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.google.android.material.tabs.TabLayoutMediator;

import es.iesagora.fitjourney.R;
import es.iesagora.fitjourney.databinding.FragmentFitnessCenterBinding;
import es.iesagora.fitjourney.ui.second.fitnesscenter.exercises.ExercisesFragment;
import es.iesagora.fitjourney.ui.second.fitnesscenter.routines.RoutinesFragment;

/**
 * Fragment principal del centro de fitness.
 * Este fragmento actúa como contenedor de navegación interna dentro de la sección
 * de entrenamiento de la aplicación. Utiliza un {@link androidx.viewpager2.widget.ViewPager2}
 * junto con un {@link com.google.android.material.tabs.TabLayout} para alternar entre:
 * - Lista de ejercicios
 * - Rutinas de entrenamiento
 * Responsabilidades principales:
 * - Inicializar el ViewPager2 con sus fragments hijos.
 * - Conectar el TabLayout con el ViewPager mediante TabLayoutMediator.
 * - Gestionar la destrucción segura de la vista.
 * No contiene lógica de negocio compleja, únicamente navegación UI.
 */
public class FitnessCenterFragment extends Fragment {

    /**
     * ViewBinding del layout del fragmento FitnessCenter.
     * Permite acceso seguro a las vistas sin usar findViewById.
     */
    private FragmentFitnessCenterBinding binding;

    /**
     * Infla la vista del fragmento FitnessCenter.
     *
     * @param inflater objeto usado para inflar layouts XML
     * @param container contenedor padre
     * @param savedInstanceState estado previo del fragmento
     * @return vista raíz del fragmento
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentFitnessCenterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * Inicializa el ViewPager2 y lo vincula con el TabLayout.
     * En este método se configuran:
     * - Adaptador del ViewPager con los fragments de ejercicios y rutinas
     * - Mediador entre TabLayout y ViewPager para sincronizar pestañas
     *
     * @param view vista raíz del fragmento
     * @param savedInstanceState estado previo del fragmento
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        establecerAdaptadorViewPager();
        vincularTabLayoutConViewPager();
    }

    /**
     * Configura el adaptador del ViewPager2.
     * Define dos páginas:
     * - Posición 0: {@link ExercisesFragment}
     * - Posición 1: {@link RoutinesFragment}
     */
    private void establecerAdaptadorViewPager() {
        binding.viewPager.setAdapter(new FragmentStateAdapter(this) {

            @NonNull
            @Override
            public Fragment createFragment(int position) {
                if (position == 0) {
                    return new ExercisesFragment();
                } else {
                    return new RoutinesFragment();
                }
            }

            @Override
            public int getItemCount() {
                return 2;
            }
        });
    }

    /**
     * Vincula el TabLayout con el ViewPager2.
     * Define los títulos de las pestañas:
     * - "Ejercicios"
     * - "Rutinas"
     * TabLayoutMediator sincroniza el cambio de pestañas con el ViewPager.
     */
    private void vincularTabLayoutConViewPager() {
        new TabLayoutMediator(binding.tabLayout, binding.viewPager,
                (tab, position) -> {
                    if (position == 0) {
                        tab.setText(getString(R.string.exercises));
                    } else {
                        tab.setText(getString(R.string.routines));
                    }
                }
        ).attach();
    }

    /**
     * Libera la referencia del ViewBinding para evitar fugas de memoria.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}