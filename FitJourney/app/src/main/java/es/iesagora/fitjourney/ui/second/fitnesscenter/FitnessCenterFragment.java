package es.iesagora.fitjourney.ui.second.fitnesscenter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.google.android.material.tabs.TabLayoutMediator;

import es.iesagora.fitjourney.R;
import es.iesagora.fitjourney.databinding.FragmentFitnessCenterBinding;
import es.iesagora.fitjourney.ui.second.fitnesscenter.exercises.ExercisesFragment;
import es.iesagora.fitjourney.ui.second.fitnesscenter.routines.RoutinesFragment;

public class FitnessCenterFragment extends Fragment {

    private FragmentFitnessCenterBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentFitnessCenterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        establecerAdaptadorViewPager();

        vincularTabLayoutConViewPager();
    }

    private void establecerAdaptadorViewPager() {
        binding.viewPager.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                if (position == 0) {
                    return new ExercisesFragment();
                } else {
                    return new RoutinesFragment();
                }
            }

            @Override
            public int getItemCount() {
                return 2;
            }
        });
    }

    private void vincularTabLayoutConViewPager() {
        new TabLayoutMediator(binding.tabLayout, binding.viewPager,
                (tab, position) -> {
                    if (position == 0) {
                        tab.setText(getString(R.string.exercises));
                    } else {
                        tab.setText(getString(R.string.routines));
                    }
                }
        ).attach();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}