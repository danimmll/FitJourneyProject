package es.iesagora.fitjourney.ui.main.origin.access;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import es.iesagora.fitjourney.R;
import es.iesagora.fitjourney.databinding.FragmentAccessBinding;

/**
 * Fragment de acceso inicial de la aplicación.
 * Pantalla de entrada donde el usuario puede elegir entre
 * iniciar sesión o registrarse.
 * Es el punto inicial del flujo de autenticación.
 */
public class AccessFragment extends Fragment {

    /**
     * ViewBinding del fragmento de acceso.
     */
    private FragmentAccessBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Infla la vista del fragmento de acceso.
     *
     * @return vista raíz del fragmento.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentAccessBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * Configura los eventos de los botones de la pantalla.
     * Define la navegación hacia login o registro.
     */
    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {

        // Navegar a registro
        binding.btnRegister.setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_accessFragment_to_registerFragment)
        );

        // Navegar a login
        binding.btnLogin.setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_accessFragment_to_loginFragment)
        );
    }

    /**
     * Libera el binding para evitar fugas de memoria.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}