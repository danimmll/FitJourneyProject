package es.iesagora.fitjourney.viewmodel;

import android.app.Application;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

import es.iesagora.fitjourney.data.model.Exercise;
import es.iesagora.fitjourney.data.remote.AccessResponse;
import es.iesagora.fitjourney.data.remote.Resource;
import es.iesagora.fitjourney.data.repositories.ExerciseRepository;
import es.iesagora.fitjourney.data.repositories.UserRepository;

public class SecondViewModel extends AndroidViewModel {

    private final ExerciseRepository repository;
    private final UserRepository userRepository;

    private final MutableLiveData<Resource<List<Exercise>>> exercisesLiveData = new MutableLiveData<>();
    private final MutableLiveData<Exercise> exerciseSelected = new MutableLiveData<>();
    private final MutableLiveData<Resource<Exercise>> singleExerciseLiveData = new MutableLiveData<>();
    private final MutableLiveData<Resource<AccessResponse>> profileLiveData = new MutableLiveData<>();

    private List<Exercise> fullExerciseList = new ArrayList<>();

    public SecondViewModel(@NonNull Application application) {
        super(application);
        repository = new ExerciseRepository(application.getApplicationContext());
        userRepository = new UserRepository(application.getApplicationContext());
        fetchExercises();
    }

    public LiveData<Resource<List<Exercise>>> getExercises() {
        return exercisesLiveData;
    }

    public void fetchExercises() {
        exercisesLiveData.setValue(Resource.loading());

        repository.getExercises(result -> {
            if (result.status == Resource.Status.SUCCESS && result.data != null) {
                fullExerciseList = new ArrayList<>(result.data);
                exercisesLiveData.setValue(result);
            } else {
                exercisesLiveData.setValue(result);
            }
        });
    }

    public void searchExercises(String query) {
        if (query == null || query.trim().isEmpty()) {
            exercisesLiveData.setValue(Resource.success(new ArrayList<>(fullExerciseList)));
            return;
        }

        List<Exercise> filteredList = new ArrayList<>();
        String filterPattern = query.toLowerCase().trim();

        for (Exercise item : fullExerciseList) {
            if (item.getName().toLowerCase().contains(filterPattern)) {
                filteredList.add(item);
            }
        }

        exercisesLiveData.setValue(Resource.success(filteredList));
    }

    public void selectExercise(Exercise exercise) {
        exerciseSelected.setValue(exercise);
    }

    public LiveData<Exercise> getSelectedExercise() {
        return exerciseSelected;
    }

    public void fetchExerciseById(int id) {
        singleExerciseLiveData.setValue(Resource.loading());
        repository.getExerciseById(id, result -> {
            singleExerciseLiveData.setValue(result);
        });
    }

    public LiveData<Resource<Exercise>> getSingleExercise() {
        return singleExerciseLiveData;
    }

    public void deleteExercise(int id, Runnable onSuccess) {
        repository.deleteExercise(id, result -> {
            if (result.status == Resource.Status.SUCCESS) {
                fetchExercises();
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
                fetchExercises();
                onSuccess.run();
            }
        });
    }

    public void updateExercise(int id, Exercise exercise, Runnable onSuccess) {
        repository.updateExercise(id, exercise, result -> {
            if (result.status == Resource.Status.SUCCESS) {
                fetchExercises();
                onSuccess.run();
            }
        });
    }

    public LiveData<Resource<AccessResponse>> getProfile() {
        return profileLiveData;
    }

    public void fetchProfile(String token) {
        profileLiveData.setValue(Resource.loading());
        userRepository.getUser(token, result -> {
            profileLiveData.setValue(result);
        });
    }
}