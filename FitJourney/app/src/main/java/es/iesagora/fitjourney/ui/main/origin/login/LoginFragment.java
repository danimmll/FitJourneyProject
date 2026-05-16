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

import java.util.Objects;

import es.iesagora.fitjourney.R;
import es.iesagora.fitjourney.databinding.FragmentLoginBinding;
import es.iesagora.fitjourney.ui.second.SecondActivity;
import es.iesagora.fitjourney.viewmodel.MainViewModel;

/**
 * Fragment encargado del inicio de sesión.
 * Permite al usuario autenticarse en la aplicación
 * y acceder a la pantalla principal.
 */
public class LoginFragment extends Fragment {

    /**
     * ViewBinding asociado al fragmento.
     */
    private FragmentLoginBinding binding;

    /**
     * Clave utilizada para guardar el email.
     */
    private static final String KEY_EMAIL = "email";

    /**
     * Clave utilizada para guardar la contraseña.
     */
    private static final String KEY_PASSWORD = "password";

    /**
     * Infla la vista del fragmento y restaura los datos guardados.
     *
     * @param inflater Objeto para inflar layouts.
     * @param container Contenedor padre de la vista.
     * @param savedInstanceState Estado previo del fragmento.
     * @return Vista raíz del fragmento.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentLoginBinding.inflate(
                inflater,
                container,
                false
        );

        if (savedInstanceState != null) {

            binding.etEmail.setText(
                    savedInstanceState.getString(KEY_EMAIL, "")
            );

            binding.etPassword.setText(
                    savedInstanceState.getString(KEY_PASSWORD, "")
            );
        }

        return binding.getRoot();
    }

    /**
     * Configura el ViewModel, la navegación y los eventos
     * relacionados con el inicio de sesión.
     *
     * @param view Vista del fragmento.
     * @param savedInstanceState Estado previo del fragmento.
     */
    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        MainViewModel viewModel =
                new ViewModelProvider(this).get(MainViewModel.class);

        NavController navController =
                Navigation.findNavController(view);

        viewModel.getLoginState().observe(
                getViewLifecycleOwner(),
                resource -> {

                    if (resource == null) return;

                    switch (resource.status) {

                        case LOADING:

                            binding.btnLoginSubmit.setEnabled(false);

                            binding.progressBar.setVisibility(
                                    View.VISIBLE
                            );

                            break;

                        case SUCCESS:

                            binding.btnLoginSubmit.setEnabled(true);

                            binding.progressBar.setVisibility(
                                    View.GONE
                            );

                            Toast.makeText(
                                    requireContext(),
                                    "¡BIENVENIDO!",
                                    Toast.LENGTH_SHORT
                            ).show();

                            Intent intent = new Intent(
                                    requireContext(),
                                    SecondActivity.class
                            );

                            intent.setFlags(
                                    Intent.FLAG_ACTIVITY_NEW_TASK
                                            | Intent.FLAG_ACTIVITY_CLEAR_TASK
                            );

                            startActivity(intent);

                            requireActivity().finish();

                            break;

                        case ERROR:

                            binding.btnLoginSubmit.setEnabled(true);

                            binding.progressBar.setVisibility(
                                    View.GONE
                            );

                            Toast.makeText(
                                    requireContext(),
                                    "Error: " + resource.message,
                                    Toast.LENGTH_SHORT
                            ).show();

                            break;
                    }
                }
        );

        binding.btnLoginSubmit.setOnClickListener(v -> {

            String email = Objects.requireNonNull(
                    binding.etEmail.getText()
            ).toString().trim();

            String password = Objects.requireNonNull(
                    binding.etPassword.getText()
            ).toString().trim();

            if (checkData(email, password)) {
                viewModel.login(email, password);
            }
        });

        binding.tvGoToRegister.setOnClickListener(v ->

                navController.navigate(R.id.registerFragment)
        );
    }

    /**
     * Valida los datos introducidos por el usuario.
     *
     * @param email Email introducido.
     * @param password Contraseña introducida.
     * @return true si los datos son válidos.
     */
    private boolean checkData(String email, String password) {

        if (email.isEmpty()) {

            binding.tlEmail.setError("El email es requerido");

            return false;

        } else {

            binding.tlEmail.setError(null);
        }

        if (password.isEmpty()) {

            binding.tlPassword.setError(
                    "La contraseña es requerida"
            );

            return false;

        } else {

            binding.tlPassword.setError(null);
        }

        return true;
    }

    /**
     * Libera el binding para evitar fugas de memoria.
     */
    @Override
    public void onDestroyView() {

        super.onDestroyView();

        binding = null;
    }

    /**
     * Guarda temporalmente los datos introducidos
     * para restaurarlos en cambios de configuración.
     *
     * @param outState Bundle donde se guardan los datos.
     */
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {

        super.onSaveInstanceState(outState);

        outState.putString(
                KEY_EMAIL,
                Objects.requireNonNull(
                        binding.etEmail.getText()
                ).toString()
        );

        outState.putString(
                KEY_PASSWORD,
                Objects.requireNonNull(
                        binding.etPassword.getText()
                ).toString()
        );
    }
}