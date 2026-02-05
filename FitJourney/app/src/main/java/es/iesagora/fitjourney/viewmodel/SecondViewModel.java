package es.iesagora.fitjourney.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import es.iesagora.fitjourney.data.model.Exercise;
import es.iesagora.fitjourney.data.remote.Resource;
import es.iesagora.fitjourney.data.repositories.ExerciseRepository;

public class SecondViewModel extends AndroidViewModel {

    private final ExerciseRepository exerciseRepository;

    public MutableLiveData<Resource<List<Exercise>>> exercises = new MutableLiveData<>();
    public final MutableLiveData<Exercise> exerciseSelected = new MutableLiveData<>();

    private boolean loading = false;

    public SecondViewModel(@NonNull Application application) {
        super(application);
        exerciseRepository = new ExerciseRepository(application);
    }

    public void loadExercises() {
        if (loading) {
            return;
        }
        loading = true;

        exerciseRepository.getExercises(result -> {
            exercises.setValue(result);
            if (result.status != Resource.Status.LOADING) {
                loading = false;
            }
        });
    }

    public void selectExercise(Exercise exercise) {
        exerciseSelected.setValue(exercise);

    }
}