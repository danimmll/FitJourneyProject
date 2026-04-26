package es.iesagora.fitjourney.ui.main.origin.login;

import android.content.Intent;
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
import es.iesagora.fitjourney.ui.second.SecondActivity;
import es.iesagora.fitjourney.viewmodel.MainViewModel;

public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;
    private MainViewModel viewModel;
    private NavController navController;
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "password";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);

        if (savedInstanceState != null) {
            binding.etEmail.setText(savedInstanceState.getString(KEY_EMAIL, ""));
            binding.etPassword.setText(savedInstanceState.getString(KEY_PASSWORD, ""));
        }

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        navController = Navigation.findNavController(view);

        viewModel.getLoginState().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;

            switch (resource.status) {
                case LOADING:
                    binding.btnLoginSubmit.setEnabled(false);
                    binding.progressBar.setVisibility(View.VISIBLE);
                    break;
                case SUCCESS:
                    binding.btnLoginSubmit.setEnabled(true);
                    binding.progressBar.setVisibility(View.GONE);

                    Toast.makeText(requireContext(), "¡BIENVENIDO!", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(requireContext(), SecondActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    requireActivity().finish();
                    break;
                case ERROR:
                    binding.btnLoginSubmit.setEnabled(true);
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(requireContext(), "Error: " + resource.message, Toast.LENGTH_SHORT).show();
                    break;
            }
        });

        binding.btnLoginSubmit.setOnClickListener(v -> {
            String email = binding.etEmail.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();

            if (checkData(email, password)) {
                viewModel.login(email, password);
            }
        });

        binding.tvGoToRegister.setOnClickListener(v -> {
            navController.navigate(R.id.registerFragment);
        });
    }

    private boolean checkData(String email, String password) {
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

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_EMAIL, binding.etEmail.getText().toString());
        outState.putString(KEY_PASSWORD, binding.etPassword.getText().toString());
    }
}