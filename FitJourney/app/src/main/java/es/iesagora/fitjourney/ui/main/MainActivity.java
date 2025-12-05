package es.iesagora.fitjourney.ui.main;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import es.iesagora.fitjourney.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView((binding = ActivityMainBinding.inflate(getLayoutInflater())).getRoot());
    }
}