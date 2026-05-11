package es.iesagora.fitjourney.ui.second.profile;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import java.util.Locale;

import es.iesagora.fitjourney.R;
import es.iesagora.fitjourney.data.model.User;
import es.iesagora.fitjourney.data.repositories.PreferencesRepository;
import es.iesagora.fitjourney.databinding.FragmentProfileBinding;
import es.iesagora.fitjourney.ui.main.MainActivity;
import es.iesagora.fitjourney.utils.ImageUtils;
import es.iesagora.fitjourney.viewmodel.SecondViewModel;

/**
 * Fragment de perfil del usuario.
 * Este fragmento muestra la información personal del usuario autenticado y permite:
 * - Visualizar datos del perfil (nombre, email, peso, altura y objetivo)
 * - Cargar y mostrar la imagen de perfil (desde URL o Base64)
 * - Acceder a la edición del perfil
 * - Cerrar sesión del usuario
 * - Mostrar opciones administrativas si el usuario tiene rol de admin
 * Los datos del usuario se obtienen a través de {@link SecondViewModel}.
 */
public class ProfileFragment extends Fragment {

    /**
     * ViewBinding del layout del perfil.
     * Permite acceso seguro a las vistas del XML.
     */
    private FragmentProfileBinding binding;

    /**
     * ViewModel compartido de la actividad secundaria.
     * Encargado de obtener y exponer los datos del perfil del usuario.
     */
    private SecondViewModel viewModel;

    /**
     * Repositorio de preferencias locales.
     * Se utiliza para obtener token, rol del usuario y gestionar sesión.
     */
    private PreferencesRepository prefs;

    /**
     * Usuario actualmente cargado en pantalla.
     * Se utiliza para edición y navegación a otros fragmentos.
     */
    private User currentUser;

    /**
     * Infla la vista del fragmento de perfil.
     *
     * @param inflater inflador de layouts XML
     * @param container contenedor padre del fragmento
     * @param savedInstanceState estado previo del fragmento
     * @return vista raíz del fragmento
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * Inicializa el fragmento de perfil.
     * Configura:
     * - ViewModel
     * - Preferencias del usuario
     * - Botones de acción
     * - FAB de administrador
     * - Observador del perfil del usuario
     *
     * @param view vista raíz del fragmento
     * @param savedInstanceState estado previo del fragmento
     */
    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(SecondViewModel.class);
        prefs = new PreferencesRepository(requireContext());

        setupAdminFab();
        setupButtons();

        String token = prefs.getToken();
        if (token != null) {
            viewModel.fetchProfile(token);
        }

        viewModel.getProfile().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;

            switch (resource.status) {

                case LOADING:
                    binding.progressBar.setVisibility(View.VISIBLE);
                    binding.scrollViewProfile.setAlpha(0.5f);
                    break;

                case SUCCESS:
                    binding.progressBar.setVisibility(View.GONE);
                    binding.scrollViewProfile.setAlpha(1f);

                    if (resource.data != null && resource.data.getUser() != null) {
                        currentUser = resource.data.getUser();
                        fillUserData(currentUser);
                    }
                    break;

                case ERROR:
                    binding.progressBar.setVisibility(View.GONE);
                    binding.scrollViewProfile.setAlpha(1f);

                    Toast.makeText(requireContext(),
                            "Error al cargar perfil: " + resource.message,
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }

    /**
     * Rellena la interfaz con los datos del usuario.
     * Muestra información como:
     * - Nombre y email
     * - Peso y altura
     * - Objetivo de entrenamiento
     * - Imagen de perfil (Base64 o URL)
     *
     * @param user usuario cuyos datos se mostrarán
     */
    @SuppressLint("SetTextI18n")
    private void fillUserData(User user) {
        binding.tvProfileName.setText(user.getName());
        binding.tvProfileEmail.setText(user.getEmail());

        // CORREGIDO: evitar "75.0 kg" usando intValue() o formato con 1 decimal
        String weight = user.getWeight() != null
                ? formatMetric(user.getWeight()) + " kg"
                : "-- kg";
        String height = user.getHeight() != null
                ? formatMetric(user.getHeight()) + " cm"
                : "-- cm";

        binding.tvProfileWeight.setText(weight);
        binding.tvProfileHeight.setText(height);


        if (user.getGoal() != null && !user.getGoal().isEmpty()) {
            String goal = user.getGoal().substring(0, 1).toUpperCase()
                    + user.getGoal().substring(1).toLowerCase();
            binding.tvProfileGoal.setText(goal);
        } else {
            binding.tvProfileGoal.setText("No definido");
        }

        ImageUtils.loadCircle(
                requireContext(),
                user.getPhoto_url(),
                binding.ivProfilePhoto,
                R.drawable.ic_profile
        );
    }

    /**
     * Configura los botones del perfil:
     * - Edición de perfil
     * - Cierre de sesión
     */
    private void setupButtons() {

        binding.btnEditProfile.setOnClickListener(v -> {
            if (currentUser != null) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("user_data", currentUser);

                Navigation.findNavController(v).navigate(
                        R.id.action_profileFragment_to_editProfileFragment,
                        bundle
                );
            }
        });

        binding.btnLogout.setOnClickListener(v -> {
            viewModel.logout();

            Intent intent = new Intent(requireActivity(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    /**
     * Configura el botón flotante de administrador.
     * Solo se muestra si el usuario tiene rol "admin".
     * En caso contrario, se oculta.
     */
    private void setupAdminFab() {
        if ("admin".equalsIgnoreCase(prefs.getRole())) {
            binding.fabAdminUsers.setVisibility(View.VISIBLE);

            binding.fabAdminUsers.setOnClickListener(v ->
                    Navigation.findNavController(v).navigate(R.id.adminUsersFragment)
            );
        } else {
            binding.fabAdminUsers.setVisibility(View.GONE);
        }
    }

    private String formatMetric(Double value) {
        if (value == null) return "--";
        if (value == Math.floor(value)) {
            return String.valueOf(value.intValue());
        }
        return String.format(Locale.getDefault(), "%.1f", value);
    }


    /**
     * Limpia el binding al destruir la vista para evitar fugas de memoria.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}