package es.iesagora.fitjourney.ui.second.settings;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

import es.iesagora.fitjourney.data.repositories.PreferencesRepository;
import es.iesagora.fitjourney.databinding.FragmentSettingsBinding;
import es.iesagora.fitjourney.ui.main.MainActivity;
import es.iesagora.fitjourney.utils.NotificationWorker;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private PreferencesRepository preferences;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    activarNotificaciones();
                } else {
                    binding.switchNotifications.setChecked(false);
                    Toast.makeText(getContext(), "Permiso necesario para avisos",
                            Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        preferences = new PreferencesRepository(requireContext());

        binding.switchDarkMode.setChecked(preferences.getTheme() == AppCompatDelegate.MODE_NIGHT_YES);
        binding.switchDarkMode.setOnCheckedChangeListener((v, isChecked) -> {
            int mode = isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO;
            preferences.setTheme(mode);
            AppCompatDelegate.setDefaultNightMode(mode);
        });

        binding.switchNotifications.setChecked(preferences.getNotifications());
        binding.switchNotifications.setOnCheckedChangeListener((v, isChecked) -> {
            if (isChecked) {
                checkPermisosYActivar();
            } else {
                desactivarNotificaciones();
            }
        });

        binding.btnLogout.setOnClickListener(v -> {
            preferences.clearSession();
            startActivity(new Intent(getActivity(), MainActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        });
    }

    private void checkPermisosYActivar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                activarNotificaciones();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        } else {
            activarNotificaciones();
        }
    }

    private void activarNotificaciones() {
        preferences.setNotifications(true);

        WorkManager.getInstance(requireContext()).enqueue(new OneTimeWorkRequest.Builder(NotificationWorker.class).build());
        Toast.makeText(getContext(), "¡Notificaciones Activadas!", Toast.LENGTH_SHORT).show();

        PeriodicWorkRequest diario = new PeriodicWorkRequest.Builder(NotificationWorker.class, 24, TimeUnit.HOURS).build();
        WorkManager.getInstance(requireContext()).enqueueUniquePeriodicWork("AvisoDiario", ExistingPeriodicWorkPolicy.UPDATE, diario);
    }

    private void desactivarNotificaciones() {
        preferences.setNotifications(false);
        WorkManager.getInstance(requireContext()).cancelUniqueWork("AvisoDiario");
        Toast.makeText(getContext(), "Notificaciones desactivados", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}