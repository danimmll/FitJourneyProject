package es.iesagora.fitjourney.ui.workout;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import es.iesagora.fitjourney.databinding.ActivityWorkoutBinding;

/**
 * Actividad que gestiona la pantalla principal del módulo de entrenamiento.
 * Actúa como contenedor base para la interfaz de workout.
 */
public class WorkoutActivity extends AppCompatActivity {

    /**
     * Inicializa la actividad y configura el binding de la vista.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ActivityWorkoutBinding binding;
        super.onCreate(savedInstanceState);

        binding = ActivityWorkoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
}