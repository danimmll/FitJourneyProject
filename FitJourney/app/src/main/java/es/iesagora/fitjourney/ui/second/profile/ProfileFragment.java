package es.iesagora.fitjourney.ui.second.profile;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    Glide.with(this).load(imageUri).circleCrop().into(binding.ivProfilePhoto);
                    Toast.makeText(getContext(), "Foto actualizada visualmente", Toast.LENGTH_SHORT).show();
                }
            }
    );

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

        String token = prefs.getToken();
        if (token != null) {
            viewModel.fetchProfile(token);
        }

        viewModel.getProfile().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;

            switch (resource.status) {
                case LOADING:
                    binding.progressBar.setVisibility(View.VISIBLE);
                    binding.scrollViewProfile.setVisibility(View.GONE);
                    break;

                case SUCCESS:
                    binding.progressBar.setVisibility(View.GONE);
                    binding.scrollViewProfile.setVisibility(View.VISIBLE);

                    if (resource.data != null && resource.data.getUser() != null) {
                        currentUser = resource.data.getUser();

                        binding.tvProfileName.setText(currentUser.getName());
                        binding.tvProfileEmail.setText(currentUser.getEmail());

                        String peso = (currentUser.getWeight() != null) ? currentUser.getWeight() + " kg" : "-- kg";
                        String altura = (currentUser.getHeight() != null) ? currentUser.getHeight() + " cm" : "-- cm";
                        String meta = (currentUser.getGoal() != null) ? currentUser.getGoal() : "No definida";

                        binding.tvProfileWeight.setText(peso);
                        binding.tvProfileHeight.setText(altura);
                        binding.tvProfileGoal.setText(meta);

                        loadProfileImage(currentUser);

                        if ("admin".equalsIgnoreCase(currentUser.getRole())) {
                            binding.fabAdminUsers.setVisibility(View.VISIBLE);
                        } else {
                            binding.fabAdminUsers.setVisibility(View.GONE);
                        }
                    }
                    break;

                case ERROR:
                    binding.progressBar.setVisibility(View.GONE);
                    binding.scrollViewProfile.setVisibility(View.VISIBLE);
                    Toast.makeText(getContext(), "Error al cargar datos: " + resource.message, Toast.LENGTH_SHORT).show();
                    break;
            }
        });


        binding.ivProfilePhoto.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });

        // Click en Editar Perfil -> Navegar a la pantalla de edición (la crearemos luego)
        binding.btnEditProfile.setOnClickListener(v -> {
            // Por ahora solo mostramos un aviso, luego configuraremos la navegación
            Toast.makeText(getContext(), "Yendo a editar perfil...", Toast.LENGTH_SHORT).show();
            // Navigation.findNavController(v).navigate(R.id.editProfileFragment);
        });

        binding.fabAdminUsers.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Accediendo al Panel de Control de Usuarios...", Toast.LENGTH_SHORT).show();
            // TODO: Navegar al fragmento de gestión de usuarios
        });

        binding.btnLogout.setOnClickListener(v -> {
            prefs.clearSession();
            Intent intent = new Intent(requireActivity(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
        });
    }

    private void loadProfileImage(User user) {
        String photoUrl = user.getPhoto_url();

        if (photoUrl == null || photoUrl.isEmpty()) {
            photoUrl = "https://api.dicebear.com/7.x/initials/png?seed=" + user.getName();
        }

        Glide.with(this)
                .load(photoUrl)
                .placeholder(R.drawable.img_construccion)
                .error(R.drawable.ic_profile)
                .into(binding.ivProfilePhoto);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}