package es.iesagora.fitjourney.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import es.iesagora.fitjourney.R;
import es.iesagora.fitjourney.data.repositories.PreferencesRepository;
import es.iesagora.fitjourney.databinding.ActivityMainBinding;
import es.iesagora.fitjourney.ui.second.SecondActivity;
import es.iesagora.fitjourney.utils.NotificationWorker;

/**
 * Actividad principal de la aplicación.
 * Gestiona la navegación inicial, aplica el tema guardado
 * y redirige al usuario si ya tiene sesión iniciada.
 */
public class MainActivity extends AppCompatActivity {

    /**
     * Controlador de navegación.
     */
    private NavController navController;

    /**
     * Método principal de creación de la actividad.
     * Configura tema, navegación y tareas en segundo plano.
     *
     * @param savedInstanceState Estado previo de la actividad.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        PreferencesRepository prefs = new PreferencesRepository(this);

        AppCompatDelegate.setDefaultNightMode(prefs.getTheme());

        PeriodicWorkRequest avisoDiario =
                new PeriodicWorkRequest.Builder(
                        NotificationWorker.class,
                        24,
                        TimeUnit.HOURS
                ).build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "AvisoDiario",
                ExistingPeriodicWorkPolicy.KEEP,
                avisoDiario
        );

        if (prefs.getToken() != null && !prefs.getToken().isEmpty()) {

            Intent intent = new Intent(this, SecondActivity.class);

            startActivity(intent);
            finish();

            return;
        }

        ActivityMainBinding binding =
                ActivityMainBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        binding.progressBar.setVisibility(View.GONE);

        setSupportActionBar(binding.toolbar);

        navController = ((NavHostFragment) Objects.requireNonNull(
                getSupportFragmentManager().findFragmentById(
                        R.id.nav_host_fragment
                )
        )).getNavController();

        AppBarConfiguration appBarConfiguration =
                new AppBarConfiguration.Builder(
                        navController.getGraph()
                ).build();

        NavigationUI.setupActionBarWithNavController(
                this,
                navController,
                appBarConfiguration
        );

        navController.addOnDestinationChangedListener(
                (controller, destination, arguments) -> {

                    if (destination.getId() == R.id.accessFragment) {

                        binding.appBarLayout.setVisibility(View.GONE);

                    } else {

                        binding.appBarLayout.setVisibility(View.VISIBLE);
                    }
                }
        );

        navController.navigate(R.id.accessFragment);
    }

    /**
     * Gestiona la navegación hacia atrás desde la barra superior.
     *
     * @return true si la navegación se realizó correctamente.
     */
    @Override
    public boolean onSupportNavigateUp() {

        return NavigationUI.navigateUp(
                navController,
                new AppBarConfiguration.Builder(
                        navController.getGraph()
                ).build()

        ) || super.onSupportNavigateUp();
    }
}