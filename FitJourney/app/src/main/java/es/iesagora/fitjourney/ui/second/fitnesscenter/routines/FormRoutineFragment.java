package es.iesagora.fitjourney.ui.second.fitnesscenter.routines;

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

import java.util.ArrayList;
import java.util.Objects;

import es.iesagora.fitjourney.R;
import es.iesagora.fitjourney.data.model.Routine;
import es.iesagora.fitjourney.data.remote.Resource;
import es.iesagora.fitjourney.databinding.FragmentFormRoutineBinding;
import es.iesagora.fitjourney.viewmodel.SecondViewModel;

/**
 * Fragment encargado de la creación de nuevas rutinas de entrenamiento.
 *
 * <p>Permite al usuario administrador o autorizado crear una rutina
 * con nombre, descripción e imagen asociada.</p>
 *
 * <p>La imagen puede seleccionarse desde la galería o introducirse mediante URL.</p>
 *
 * Responsabilidades principales:
 * <ul>
 *     <li>Seleccionar imagen desde almacenamiento local.</li>
 *     <li>Previsualizar imagen mediante Glide.</li>
 *     <li>Validar campos obligatorios del formulario.</li>
 *     <li>Crear y enviar la rutina al ViewModel.</li>
 *     <li>Gestionar estados de carga, éxito y error.</li>
 * </ul>
 */
public class FormRoutineFragment extends Fragment {

    /** ViewBinding del layout del formulario de rutina */
    private FragmentFormRoutineBinding binding;

    /** ViewModel compartido para gestión de rutinas */
    private SecondViewModel viewModel;

    /** URI de la imagen seleccionada localmente */
    private Uri selectedLocalImageUri;

    /**
     * Launcher para abrir la galería y seleccionar una imagen.
     * Devuelve la URI seleccionada por el usuario.
     */
    private final ActivityResultLauncher<String> getContent =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedLocalImageUri = uri;
                    assert binding.etRoutineImage != null;
                    binding.etRoutineImage.setText("");
                    assert binding.ivPhotoPreview != null;
                    Glide.with(this).load(uri).centerCrop().into(binding.ivPhotoPreview);
                }
            });

    /**
     * Infla la vista del fragmento.
     *
     * @param inflater inflador del layout
     * @param container contenedor padre
     * @param savedInstanceState estado previo
     * @return vista raíz del fragmento
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentFormRoutineBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * Inicializa ViewModel, listeners y observers.
     * Configura:
     * - Selección de imagen
     * - Previsualización
     * - Validación del formulario
     * - Observación del estado de creación
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(SecondViewModel.class);

        setupListeners();
        setupObservers();
    }

    /**
     * Configura los eventos de la interfaz:
     * - Navegación
     * - Selección de imagen
     * - Previsualización por URL
     * - Guardado de rutina
     */
    private void setupListeners() {

        assert binding.toolbar != null;
        binding.toolbar.setNavigationOnClickListener(
                v -> Navigation.findNavController(v).navigateUp()
        );

        assert binding.ivPhotoPreview != null;
        binding.ivPhotoPreview.setOnClickListener(v -> getContent.launch("image/*"));

        assert binding.etRoutineImage != null;
        binding.etRoutineImage.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String url = Objects.requireNonNull(binding.etRoutineImage.getText()).toString().trim();
                if (!url.isEmpty()) {
                    selectedLocalImageUri = null;
                    Glide.with(this)
                            .load(url)
                            .centerCrop()
                            .placeholder(R.drawable.img_construccion)
                            .into(binding.ivPhotoPreview);
                }
            }
        });

        assert binding.btnAddExercise != null;
        binding.btnAddExercise.setOnClickListener(v ->
                Toast.makeText(getContext(),
                        "Catálogo de selección de ejercicios en desarrollo 🛠️",
                        Toast.LENGTH_SHORT).show()
        );

        assert binding.btnSaveRoutine != null;
        binding.btnSaveRoutine.setOnClickListener(v -> saveRoutine());
    }

    /**
     * Observa el estado de creación de la rutina:
     * <ul>
     *     <li>LOADING: muestra progreso</li>
     *     <li>SUCCESS: confirma creación y navega atrás</li>
     *     <li>ERROR: muestra mensaje de error</li>
     * </ul>
     */
    private void setupObservers() {

        viewModel.getRoutineActionStatus().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;

            assert binding.progressBar != null;
            if (resource.status == Resource.Status.LOADING) {
                binding.progressBar.setVisibility(View.VISIBLE);
                assert binding.btnSaveRoutine != null;
                binding.btnSaveRoutine.setEnabled(false);

            } else {
                binding.progressBar.setVisibility(View.GONE);
                assert binding.btnSaveRoutine != null;
                binding.btnSaveRoutine.setEnabled(true);

                if (resource.status == Resource.Status.SUCCESS) {

                    if (resource.message != null &&
                            resource.message.toLowerCase().contains("creada")) {

                        Toast.makeText(getContext(),
                                "Rutina guardada correctamente",
                                Toast.LENGTH_SHORT).show();

                        viewModel.getRoutineActionStatus().setValue(null);
                        Navigation.findNavController(requireView()).navigateUp();
                    }

                } else if (resource.status == Resource.Status.ERROR) {
                    Toast.makeText(getContext(),
                            resource.message,
                            Toast.LENGTH_SHORT).show();

                    viewModel.getRoutineActionStatus().setValue(null);
                }
            }
        });
    }

    /**
     * Valida y construye una nueva rutina para enviarla al ViewModel.
     *
     * <p>Reglas:</p>
     * <ul>
     *     <li>El nombre es obligatorio.</li>
     *     <li>La descripción es opcional.</li>
     *     <li>La imagen puede venir de URL o galería.</li>
     * </ul>
     */
    private void saveRoutine() {

        assert binding.etRoutineName != null;
        String name = binding.etRoutineName.getText() != null
                ? binding.etRoutineName.getText().toString().trim()
                : "";

        assert binding.etRoutineDesc != null;
        String desc = binding.etRoutineDesc.getText() != null
                ? binding.etRoutineDesc.getText().toString().trim()
                : "";

        assert binding.etRoutineImage != null;
        String imageUrl = binding.etRoutineImage.getText() != null
                ? binding.etRoutineImage.getText().toString().trim()
                : "";

        assert binding.tilRoutineName != null;
        if (name.isEmpty()) {
            binding.tilRoutineName.setError("El nombre es obligatorio");
            return;
        } else {
            binding.tilRoutineName.setError(null);
        }

        Routine routine = new Routine();
        routine.setName(name);
        routine.setDescription(desc.isEmpty() ? null : desc);

        if (selectedLocalImageUri != null) {
            routine.setPhoto_url(selectedLocalImageUri.toString());
        } else {
            routine.setPhoto_url(imageUrl.isEmpty() ? null : imageUrl);
        }

        routine.setExercises(new ArrayList<>());

        viewModel.createRoutine(routine);
    }

    /**
     * Limpia referencias del binding para evitar memory leaks.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}