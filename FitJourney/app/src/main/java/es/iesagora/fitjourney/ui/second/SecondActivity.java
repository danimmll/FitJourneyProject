package es.iesagora.fitjourney.ui.second;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.util.Objects;

import es.iesagora.fitjourney.R;
import es.iesagora.fitjourney.databinding.ActivitySecondBinding;

/**
 * Actividad principal de la segunda pantalla de la aplicación (zona autenticada).
 * Esta actividad actúa como contenedor principal tras el login/registro y gestiona:
 * - La navegación entre fragmentos principales (home, ejercicios y perfil)
 * - La integración con Navigation Component
 * - La configuración de la barra superior (Toolbar)
 * - La sincronización con el BottomNavigationView
 * - El manejo del menú de opciones (settings)
 * Es el punto central de la experiencia del usuario una vez autenticado.
 */
public class SecondActivity extends AppCompatActivity {

    /**
     * Controlador de navegación de Android Navigation Component.
     * Gestiona la navegación entre fragmentos dentro del NavHostFragment.
     */
    private NavController navController;

    /**
     * Configuración de la AppBar.
     * Define qué destinos se consideran "top level" dentro de la navegación.
     */
    private AppBarConfiguration appBarConfiguration;

    /**
     * Método de creación de la actividad.
     * Inicializa:
     * - ViewModel
     * - ViewBinding
     * - Toolbar
     * - NavController
     * - BottomNavigationView
     * - Configuración de navegación superior
     *
     * @param savedInstanceState estado previo de la actividad si fue recreada
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        ActivitySecondBinding binding = ActivitySecondBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        navController = ((NavHostFragment) Objects.requireNonNull(getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment)))
                .getNavController();

        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.homeFragment,
                R.id.exercisesFragment,
                R.id.profileFragment
        ).build();

        NavigationUI.setupActionBarWithNavController(
                this,
                navController,
                appBarConfiguration
        );

        NavigationUI.setupWithNavController(
                binding.bottomNavigationView,
                navController
        );
    }

    /**
     * Infla el menú de opciones en la Toolbar.
     *
     * @param menu menú de opciones de la actividad
     * @return true si el menú fue creado correctamente
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings_menu, menu);
        return true;
    }

    /**
     * Gestiona la selección de opciones del menú superior.
     * Delega la navegación a NavigationUI para manejar destinos del menú.
     *
     * @param item elemento seleccionado del menú
     * @return true si la acción fue manejada correctamente
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return NavigationUI.onNavDestinationSelected(item, navController)
                || super.onOptionsItemSelected(item);
    }

    /**
     * Controla la navegación "Up" (botón atrás en la barra superior).
     *
     * @return true si la navegación hacia arriba se realizó correctamente
     */
    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}