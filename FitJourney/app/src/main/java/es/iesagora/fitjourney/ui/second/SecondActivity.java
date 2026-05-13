package es.iesagora.fitjourney.ui.second;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import android.util.Base64;
import org.json.JSONObject;
import java.util.Objects;

import es.iesagora.fitjourney.R;
import es.iesagora.fitjourney.data.repositories.PreferencesRepository;
import es.iesagora.fitjourney.databinding.ActivitySecondBinding;
import es.iesagora.fitjourney.ui.main.MainActivity;
import es.iesagora.fitjourney.utils.SessionEvent;

/**
 * Actividad principal de la zona autenticada.
 */
public class SecondActivity extends AppCompatActivity {

    private NavController navController;
    private AppBarConfiguration appBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferencesRepository prefs = new PreferencesRepository(this);
        if (!isTokenValid(prefs.getToken())) {
            prefs.clearSession();
            redirectToLogin();
            return;
        }

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

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.bottomNavigationView, navController);

        SessionEvent.getSessionExpired().observe(this, expired -> {
            if (expired != null && expired) {
                SessionEvent.clear();
                redirectToLogin();
            }
        });
    }

    private boolean isTokenValid(String token) {
        if (token == null || token.trim().isEmpty()) return false;
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) return false;
            String payload = new String(Base64.decode(parts[1], Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING));
            JSONObject json = new JSONObject(payload);
            long exp = json.optLong("exp", 0L);
            if (exp <= 0) return false;
            long now = System.currentTimeMillis() / 1000L;
            return exp > now;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Redirige al usuario a MainActivity (login) limpiando el back stack
     * para que no pueda volver atrás a la zona autenticada.
     */
    private void redirectToLogin() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return NavigationUI.onNavDestinationSelected(item, navController)
                || super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}