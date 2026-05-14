package es.iesagora.fitjourney.ui.workout;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import es.iesagora.fitjourney.databinding.ActivityWorkoutBinding;

public class WorkoutActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ActivityWorkoutBinding binding;
        super.onCreate(savedInstanceState);

        binding = ActivityWorkoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
}