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
 * Fragment que muestra el perfil del usuario.
 * Permite visualizar datos personales, acceder a edición de perfil,
 * cerrar sesión y, en caso de administrador, acceder al panel de gestión de usuarios.
 */
public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private SecondViewModel viewModel;
    private PreferencesRepository prefs;
    private User currentUser;

    /**
     * Infla la vista del fragmento de perfil.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * Inicializa ViewModel, repositorios, botones y observadores del perfil.
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
     * Rellena la UI con los datos del usuario.
     */
    @SuppressLint("SetTextI18n")
    private void fillUserData(User user) {
        binding.tvProfileName.setText(user.getName());
        binding.tvProfileEmail.setText(user.getEmail());

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
                user.getDisplayAvatar(),
                binding.ivProfilePhoto,
                R.drawable.ic_profile
        );

        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            binding.btnEditProfile.setVisibility(View.GONE);
        } else {
            binding.btnEditProfile.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Configura botones de edición y cierre de sesión.
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
     * Configura el botón flotante de administración si el usuario es admin.
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

    /**
     * Formatea un valor numérico eliminando decimales innecesarios.
     */
    private String formatMetric(Double value) {
        if (value == null) return "--";
        if (value == Math.floor(value)) {
            return String.valueOf(value.intValue());
        }
        return String.format(Locale.getDefault(), "%.1f", value);
    }

    /**
     * Limpia el binding cuando se destruye la vista.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}