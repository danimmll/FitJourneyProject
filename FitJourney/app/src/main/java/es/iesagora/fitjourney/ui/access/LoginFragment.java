package es.iesagora.fitjourney.ui.access;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import es.iesagora.fitjourney.R;
import es.iesagora.fitjourney.databinding.FragmentLoginBinding;
import es.iesagora.fitjourney.viewmodel.UserViewModel;

public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;
    private UserViewModel viewModel;
    private NavController navController;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(UserViewModel.class);
        navController = Navigation.findNavController(view);

        viewModel.getLoginState().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;

            switch (resource.status) {
                case LOADING:
                    binding.btnLoginSubmit.setEnabled(false);
                    break;
                case SUCCESS:
                    binding.btnLoginSubmit.setEnabled(true);
                    binding.btnLoginSubmit.setText(R.string.login);

                    if (resource.data != null && resource.data.getToken() != null) {
                        requireContext().getSharedPreferences("APP_PREFS",
                                        Context.MODE_PRIVATE)
                                .edit()
                                .putString("TOKEN", resource.data.getToken())
                                .apply();

                        Toast.makeText(requireContext(), "¡WELCOME!", Toast.LENGTH_SHORT).show();
                        navController.navigate(R.id.action_loginFragment_to_homeFragment);
                    }
                    break;
                case ERROR:
                    binding.btnLoginSubmit.setEnabled(true);
                    binding.btnLoginSubmit.setText(R.string.login);
                    Toast.makeText(requireContext(), "Error: " + resource.message, Toast.LENGTH_SHORT).show();
                    break;
            }
        });

        binding.btnLoginSubmit.setOnClickListener(v -> {
            String email = binding.etEmail.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();

            if (validarDatos(email, password)) {
                viewModel.login(email, password);
            }
        });
    }

    private boolean validarDatos(String email, String password) {
        if (email.isEmpty()) {
            binding.tlEmail.setError("El email es requerido");
            return false;
        } else {
            binding.tlEmail.setError(null);
        }

        if (password.isEmpty()) {
            binding.tlPassword.setError("La contraseña es requerida");
            return false;
        } else {
            binding.tlPassword.setError(null);
        }
        return true;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}