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
 * Fragment principal de acceso a la aplicación.
 * Permite al usuario elegir entre iniciar sesión
 * o crear una nueva cuenta.
 */
public class AccessFragment extends Fragment {

    /**
     * ViewBinding asociado al fragmento.
     */
    private FragmentAccessBinding binding;

    /**
     * Método de creación del fragmento.
     *
     * @param savedInstanceState Estado previo del fragmento.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Infla la vista del fragmento.
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

        binding = FragmentAccessBinding.inflate(
                inflater,
                container,
                false
        );

        return binding.getRoot();
    }

    /**
     * Configura los eventos de la interfaz.
     * Gestiona la navegación hacia login y registro.
     *
     * @param view Vista del fragmento.
     * @param savedInstanceState Estado previo del fragmento.
     */
    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {

        // Navegación hacia registro
        binding.btnRegister.setOnClickListener(v ->

                Navigation.findNavController(v)
                        .navigate(
                                R.id.action_accessFragment_to_registerFragment
                        )
        );

        // Navegación hacia login
        binding.btnLogin.setOnClickListener(v ->

                Navigation.findNavController(v)
                        .navigate(
                                R.id.action_accessFragment_to_loginFragment
                        )
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