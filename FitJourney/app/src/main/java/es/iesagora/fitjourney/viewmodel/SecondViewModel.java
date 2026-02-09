package es.iesagora.fitjourney.viewmodel;

import android.app.Application;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

import es.iesagora.fitjourney.data.model.Exercise;
import es.iesagora.fitjourney.data.remote.Resource;
import es.iesagora.fitjourney.data.repositories.ExerciseRepository;

public class SecondViewModel extends AndroidViewModel {

    private final ExerciseRepository repository;

    public MutableLiveData<Resource<List<Exercise>>> exercises = new MutableLiveData<>();
    public final MutableLiveData<Exercise> exerciseSelected = new MutableLiveData<>();


    private List<Exercise> fullExerciseList = new ArrayList<>();

    public SecondViewModel(@NonNull Application application) {
        super(application);
        repository = new ExerciseRepository(application.getApplicationContext());
        loadExercises();
    }

    public void loadExercises() {
        exercises.setValue(Resource.loading());

        repository.getExercises(result -> {
            if (result.status == Resource.Status.SUCCESS && result.data != null) {
                fullExerciseList = new ArrayList<>(result.data);
                exercises.setValue(result);
            } else {
                exercises.setValue(result);
            }
        });
    }

    public void searchExercises(String query) {
        if (query == null || query.trim().isEmpty()) {
            exercises.setValue(Resource.success(new ArrayList<>(fullExerciseList)));
            return;
        }

        List<Exercise> filteredList = new ArrayList<>();
        String filterPattern = query.toLowerCase().trim();

        for (Exercise item : fullExerciseList) {
            if (item.getName().toLowerCase().contains(filterPattern)) {
                filteredList.add(item);
            }
        }

        if (filteredList.isEmpty()) {
            exercises.setValue(Resource.success(new ArrayList<>()));
        } else {
            exercises.setValue(Resource.success(filteredList));
        }
    }

    public void selectExercise(Exercise exercise) {
        exerciseSelected.setValue(exercise);
    }

    public void deleteExercise(int id, Runnable onSuccess) {
        repository.deleteExercise(id, result -> {
            if (result.status == Resource.Status.SUCCESS) {
                loadExercises();
                onSuccess.run();
            } else if (result.status == Resource.Status.ERROR) {
                String msg = result.message != null ? result.message : "Error al eliminar";
                Toast.makeText(getApplication(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void createExercise(Exercise exercise, Runnable onSuccess) {
        repository.createExercise(exercise, result -> {
            if (result.status == Resource.Status.SUCCESS) {
                loadExercises();
                onSuccess.run();
            }
        });
    }

    public void updateExercise(int id, Exercise exercise, Runnable onSuccess) {
        repository.updateExercise(id, exercise, result -> {
            if (result.status == Resource.Status.SUCCESS) {
                loadExercises();
                onSuccess.run();
            }
        });
    }
}