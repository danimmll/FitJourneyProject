package es.iesagora.fitjourney.ui.main;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.util.Objects;

import es.iesagora.fitjourney.R;
import es.iesagora.fitjourney.databinding.ActivityMainBinding;
import es.iesagora.fitjourney.viewmodel.UserViewModel;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private NavController navController;
    private AppBarConfiguration appBarConfiguration;
    private UserViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        navController = ((NavHostFragment) Objects.requireNonNull(getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment))).getNavController();

        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        viewModel = new ViewModelProvider(this).get(UserViewModel.class);

        String token = getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE)
                .getString("TOKEN", null);

        if (token != null) {
            viewModel.fetchUser(token);

            viewModel.getLoginState().observe(this, resource -> {
                if (resource == null) return;

                switch (resource.status) {
                    case LOADING:
                        if (binding.progressBar != null) {
                            binding.progressBar.setVisibility(View.VISIBLE);
                        }
                        break;
                    case SUCCESS:
                        if (binding.progressBar != null) {
                            binding.progressBar.setVisibility(View.GONE);
                        }
                        if (resource.data != null) {
                            Toast.makeText(this, R.string.welcome_back, Toast.LENGTH_SHORT).show();
                            navController.navigate(R.id.homeFragment);
                        }
                        break;
                    case ERROR:
                        getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE)
                                .edit()
                                .remove("TOKEN")
                                .apply();
                        Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show();
                        navController.navigate(R.id.loginFragment);
                        break;
                }
            });
        } else {
            navController.navigate(R.id.accessFragment);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

}
