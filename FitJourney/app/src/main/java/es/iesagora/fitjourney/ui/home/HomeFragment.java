package es.iesagora.fitjourney.ui.home;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import es.iesagora.fitjourney.databinding.FragmentHomeBinding;
import es.iesagora.fitjourney.viewmodel.HomeViewModel;
import es.iesagora.fitjourney.data.remote.Resource;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private HomeViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        String token = requireContext().getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE)
                .getString("TOKEN", null);

        if (token != null) {
            viewModel.getUserLiveData().observe(getViewLifecycleOwner(), resource -> {
                switch (resource.status) {
                    case LOADING:
                        if (binding.progressBarHome != null) {
                            binding.progressBarHome.setVisibility(View.VISIBLE);
                        }
                        break;
                    case SUCCESS:
                        if (binding.progressBarHome != null) {
                            binding.progressBarHome.setVisibility(View.GONE);
                        }
                        if (resource.data != null) {
                            if (binding.tvPrueba2 != null) {
                                binding.tvPrueba2.setText("¡Bienvenido, " + resource.data.getName() + "!");
                            }
                        }
                        break;
                    case ERROR:
                        if (binding.progressBarHome != null) {
                            binding.progressBarHome.setVisibility(View.GONE);
                        }
                        Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show();
                        break;
                }
            });

            viewModel.fetchUser(token);

        } else {
            Toast.makeText(requireContext(), "No se encontró token, inicia sesión primero", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
