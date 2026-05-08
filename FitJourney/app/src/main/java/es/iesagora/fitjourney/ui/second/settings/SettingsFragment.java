package es.iesagora.fitjourney.ui.second.settings;

import android.Manifest;
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
import es.iesagora.fitjourney.utils.NotificationWorker;

/**
 * Fragment de configuración de la aplicación.
 * Este fragmento permite al usuario personalizar el comportamiento y la apariencia de la app.
 * Gestiona principalmente dos funcionalidades:
 * - Modo oscuro (Dark Mode) usando {@link AppCompatDelegate}
 * - Activación y desactivación de notificaciones periódicas mediante WorkManager
 * Además, en Android 13 o superior, gestiona el permiso en tiempo de ejecución
 * para poder mostrar notificaciones.
 * Las preferencias se almacenan localmente mediante {@link PreferencesRepository}.
 */
public class SettingsFragment extends Fragment {

    /**
     * ViewBinding del layout de configuración.
     * Permite acceder de forma segura a los elementos de la interfaz.
     */
    private FragmentSettingsBinding binding;

    /**
     * Repositorio de preferencias locales del usuario.
     * Se utiliza para guardar estado del tema y notificaciones.
     */
    private PreferencesRepository preferences;

    /**
     * Launcher encargado de solicitar el permiso de notificaciones en tiempo de ejecución.
     * Se usa en Android 13+ para pedir {@link Manifest.permission#POST_NOTIFICATIONS}.
     * Si el usuario concede el permiso, se activan las notificaciones automáticamente.
     */
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    activarNotificaciones();
                } else {
                    binding.switchNotifications.setChecked(false);
                    Toast.makeText(getContext(),
                            "Permiso necesario para avisos",
                            Toast.LENGTH_SHORT).show();
                }
            });

    /**
     * Infla la vista del fragmento de ajustes.
     *
     * @param inflater objeto para inflar layouts XML
     * @param container contenedor padre del fragmento
     * @param savedInstanceState estado previo del fragmento
     * @return vista raíz del fragmento
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * Inicializa la interfaz y las preferencias del usuario.
     * Configura:
     * - Switch de modo oscuro
     * - Switch de notificaciones
     * - Estado inicial desde preferencias almacenadas
     *
     * @param view vista raíz del fragmento
     * @param savedInstanceState estado previo del fragmento
     */
    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        preferences = new PreferencesRepository(requireContext());

        setupThemeSwitch();
        setupNotificationSwitch();
    }

    /**
     * Configura el switch de modo oscuro.
     * Sincroniza el estado del switch con las preferencias almacenadas
     * y aplica el tema global de la aplicación.
     */
    private void setupThemeSwitch() {
        binding.switchDarkMode.setChecked(
                preferences.getTheme() == AppCompatDelegate.MODE_NIGHT_YES
        );

        binding.switchDarkMode.setOnCheckedChangeListener((v, isChecked) -> {
            int mode = isChecked
                    ? AppCompatDelegate.MODE_NIGHT_YES
                    : AppCompatDelegate.MODE_NIGHT_NO;

            preferences.setTheme(mode);
            AppCompatDelegate.setDefaultNightMode(mode);
        });
    }

    /**
     * Configura el switch de notificaciones.
     * Carga el estado guardado en preferencias y define el comportamiento
     * al activarlo o desactivarlo.
     */
    private void setupNotificationSwitch() {
        binding.switchNotifications.setChecked(preferences.getNotifications());

        binding.switchNotifications.setOnCheckedChangeListener((v, isChecked) -> {
            if (isChecked) {
                checkPermisosYActivar();
            } else {
                desactivarNotificaciones();
            }
        });
    }

    /**
     * Comprueba si la aplicación tiene permiso para enviar notificaciones.
     * En Android 13 o superior solicita el permiso en tiempo de ejecución.
     * En versiones anteriores, lo activa directamente.
     */
    private void checkPermisosYActivar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED) {
                activarNotificaciones();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }

        } else {
            activarNotificaciones();
        }
    }

    /**
     * Activa el sistema de notificaciones periódicas.
     * - Guarda la preferencia del usuario
     * - Ejecuta una notificación inmediata de prueba
     * - Programa notificaciones cada 24 horas mediante WorkManager
     */
    private void activarNotificaciones() {
        preferences.setNotifications(true);

        WorkManager.getInstance(requireContext())
                .enqueue(new OneTimeWorkRequest.Builder(NotificationWorker.class).build());

        Toast.makeText(getContext(),
                "¡Notificaciones Activadas!",
                Toast.LENGTH_SHORT).show();

        PeriodicWorkRequest diario =
                new PeriodicWorkRequest.Builder(NotificationWorker.class, 24, TimeUnit.HOURS)
                        .build();

        WorkManager.getInstance(requireContext())
                .enqueueUniquePeriodicWork(
                        "AvisoDiario",
                        ExistingPeriodicWorkPolicy.UPDATE,
                        diario
                );
    }

    /**
     * Desactiva el sistema de notificaciones periódicas.
     * - Actualiza preferencias
     * - Cancela trabajos programados en WorkManager
     */
    private void desactivarNotificaciones() {
        preferences.setNotifications(false);

        WorkManager.getInstance(requireContext())
                .cancelUniqueWork("AvisoDiario");

        Toast.makeText(getContext(),
                "Notificaciones desactivadas",
                Toast.LENGTH_SHORT).show();
    }

    /**
     * Limpia el binding para evitar fugas de memoria.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}