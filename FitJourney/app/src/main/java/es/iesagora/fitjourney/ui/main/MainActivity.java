package es.iesagora.fitjourney.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.util.Objects;

import es.iesagora.fitjourney.R;
import es.iesagora.fitjourney.databinding.ActivityMainBinding;
import es.iesagora.fitjourney.ui.second.SecondActivity;
import es.iesagora.fitjourney.viewmodel.UserViewModel;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private NavController navController;
    private UserViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = new ViewModelProvider(this).get(UserViewModel.class);

        AppCompatDelegate.setDefaultNightMode(viewModel.getTheme());

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.progressBar.setVisibility(View.GONE);
        setSupportActionBar(binding.toolbar);

        navController = ((NavHostFragment) Objects.requireNonNull(getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment))).getNavController();

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        if (viewModel.isUserLoggedIn()) {
            viewModel.fetchUser(viewModel.getToken());

            viewModel.getUserProfileState().observe(this, resource -> {
                if (resource == null) return;

                switch (resource.status) {
                    case LOADING:
                        binding.progressBar.setVisibility(View.VISIBLE);
                        binding.navHostFragment.setVisibility(View.GONE);
                        break;

                    case SUCCESS:
                        binding.progressBar.setVisibility(View.GONE);
                        binding.navHostFragment.setVisibility(View.VISIBLE);
                        Toast.makeText(this, getString(R.string.welcome_back), Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(this, SecondActivity.class);
                        startActivity(intent);
                        finish();
                        break;

                    case ERROR:
                        binding.progressBar.setVisibility(View.GONE);
                        binding.navHostFragment.setVisibility(View.VISIBLE);

                        viewModel.logout();

                        navController.navigate(R.id.accessFragment);
                        break;
                }
            });

        } else {
            navController.navigate(R.id.accessFragment);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController,
                new AppBarConfiguration.Builder(navController.getGraph()).build())
                || super.onSupportNavigateUp();
    }
}