package es.iesagora.fitjourney.ui.second.profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;

import es.iesagora.fitjourney.R;
import es.iesagora.fitjourney.data.model.User;
import es.iesagora.fitjourney.data.repositories.PreferencesRepository;
import es.iesagora.fitjourney.databinding.FragmentProfileBinding;
import es.iesagora.fitjourney.ui.main.MainActivity;
import es.iesagora.fitjourney.viewmodel.SecondViewModel;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private SecondViewModel viewModel;
    private PreferencesRepository prefs;
    private User currentUser;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
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
                    Toast.makeText(requireContext(), "Error al cargar perfil: " + resource.message, Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }

    private void fillUserData(User user) {
        binding.tvProfileName.setText(user.getName());
        binding.tvProfileEmail.setText(user.getEmail());

        String weight = user.getWeight() != null ? user.getWeight() + " kg" : "-- kg";
        String height = user.getHeight() != null ? user.getHeight() + " cm" : "-- cm";
        binding.tvProfileWeight.setText(weight);
        binding.tvProfileHeight.setText(height);

        if (user.getGoal() != null && !user.getGoal().isEmpty()) {
            String goal = user.getGoal().substring(0, 1).toUpperCase() + user.getGoal().substring(1).toLowerCase();
            binding.tvProfileGoal.setText(goal);
        } else {
            binding.tvProfileGoal.setText("No definido");
        }

        String photoUrl = user.getPhoto_url();
        String dicebearAvatar = "https://api.dicebear.com/7.x/avataaars/png?seed=" + user.getName() + "&backgroundColor=b6e3f4,c0aede,d1d4f9";

        if (photoUrl != null && !photoUrl.isEmpty()) {
            if (photoUrl.length() > 200 || !photoUrl.startsWith("http")) {
                // Si la imagen es Base64
                try {
                    byte[] imageBytes = Base64.decode(photoUrl, Base64.DEFAULT);
                    Glide.with(this).asBitmap().load(imageBytes).circleCrop().into(binding.ivProfilePhoto);
                } catch (Exception e) {
                    Glide.with(this).load(dicebearAvatar).circleCrop().into(binding.ivProfilePhoto);
                }
            } else {
                // Si es URL normal
                Glide.with(this).load(photoUrl).circleCrop().into(binding.ivProfilePhoto);
            }
        } else {
            Glide.with(this).load(dicebearAvatar).circleCrop().into(binding.ivProfilePhoto);
        }
    }

    private void setupButtons() {
        // --- BOTÓN EDITAR PERFIL ---
        binding.btnEditProfile.setOnClickListener(v -> {
            if (currentUser != null) {
                // TODO: Navegar a EditProfileFragment pasándole el objeto currentUser en el Bundle
                // Bundle bundle = new Bundle();
                // bundle.putSerializable("user_data", currentUser);
                // Navigation.findNavController(v).navigate(R.id.action_profile_to_editProfile, bundle);
                Toast.makeText(getContext(), "Pantalla de Editar Perfil en construcción 🛠️", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnLogout.setOnClickListener(v -> {
            prefs.clearSession();
            Intent intent = new Intent(requireActivity(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    private void setupAdminFab() {
        if ("admin".equalsIgnoreCase(prefs.getRole())) {
            binding.fabAdminUsers.setVisibility(View.VISIBLE);
            binding.fabAdminUsers.setOnClickListener(v -> {
                Toast.makeText(getContext(), "Panel de Administrador en construcción 🛠️", Toast.LENGTH_SHORT).show();
            });
        } else {
            binding.fabAdminUsers.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}