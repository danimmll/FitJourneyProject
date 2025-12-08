package es.iesagora.fitjourney.ui.home;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import es.iesagora.fitjourney.R;
import es.iesagora.fitjourney.databinding.FragmentHomeBinding;
import es.iesagora.fitjourney.viewmodel.UserViewModel;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private UserViewModel viewModel;

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

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        Toast.makeText(requireContext(), "No puedes volver atrás", Toast.LENGTH_SHORT).show();
                    }
                });

        if (getActivity() instanceof AppCompatActivity) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }

        viewModel = new ViewModelProvider(this).get(UserViewModel.class);

        String token = requireContext().getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE)
                .getString("TOKEN", null);

        if (token != null) {
            viewModel.fetchUser(token);

            viewModel.getUserProfileState().observe(getViewLifecycleOwner(), resource -> {
                if (resource == null) return;

                switch (resource.status) {
                    case LOADING:
                        binding.progressBarHome.setVisibility(View.VISIBLE);
                        break;

                    case SUCCESS:
                        binding.progressBarHome.setVisibility(View.GONE);
                        if (resource.data != null) {
                            String[] nameParts = resource.data.getName().split(" ");
                            if (nameParts.length <= 3) {
                                binding.tvName.setText(nameParts[0]);
                            } else {
                                binding.tvName.setText(nameParts[0] + " " + nameParts[1]);
                            }
                        }
                        break;

                    case ERROR:
                        binding.progressBarHome.setVisibility(View.GONE);
                        Toast.makeText(requireContext(), resource.message, Toast.LENGTH_LONG).show();
                        break;
                }
            });

        } else {
            Toast.makeText(requireContext(), "Token no encontrado", Toast.LENGTH_SHORT).show();
        }

        binding.btnLogout.setOnClickListener(v -> {
            requireContext().getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE)
                    .edit()
                    .remove("TOKEN")
                    .apply();

            NavOptions navOptions = new NavOptions.Builder()
                    .setPopUpTo(R.id.homeFragment, true)
                    .build();

            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_homeFragment_to_accessFragment, null, navOptions);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}