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
import es.iesagora.fitjourney.data.model.Routine;
import es.iesagora.fitjourney.data.model.User;
import es.iesagora.fitjourney.data.model.Workout;
import es.iesagora.fitjourney.data.remote.AccessResponse;
import es.iesagora.fitjourney.data.remote.Resource;
import es.iesagora.fitjourney.data.repositories.ExerciseRepository;
import es.iesagora.fitjourney.data.repositories.PreferencesRepository;
import es.iesagora.fitjourney.data.repositories.RoutineRepository;
import es.iesagora.fitjourney.data.repositories.UserRepository;
import es.iesagora.fitjourney.data.repositories.WorkoutRepository;

/**
 * ViewModel secundario encargado de la lógica principal de la aplicación.
 * Gestiona ejercicios, rutinas, entrenamientos y perfil de usuario.
 * Actúa como puente entre la capa de datos (repositories) y la UI,
 * exponiendo estados mediante LiveData.
 */
public class SecondViewModel extends AndroidViewModel {

    /**
     * Repositorio de ejercicios.
     */
    private final ExerciseRepository repository;

    /**
     * Repositorio de usuarios.
     */
    private final UserRepository userRepository;

    /**
     * Repositorio de preferencias locales.
     */
    private final PreferencesRepository preferencesRepository;

    /**
     * Repositorio de entrenamientos.
     */
    private final WorkoutRepository workoutRepository;

    /**
     * Repositorio de rutinas.
     */
    private final RoutineRepository routineRepository;

    /**
     * Lista de ejercicios con estado (loading/success/error).
     */
    private final MutableLiveData<Resource<List<Exercise>>> exercisesLiveData =
            new MutableLiveData<>();

    /**
     * Ejercicio actualmente seleccionado.
     */
    private final MutableLiveData<Exercise> exerciseSelected =
            new MutableLiveData<>();

    /**
     * Ejercicio obtenido por ID.
     */
    private final MutableLiveData<Resource<Exercise>> singleExerciseLiveData =
            new MutableLiveData<>();

    /**
     * Perfil del usuario.
     */
    private final MutableLiveData<Resource<AccessResponse>> profileLiveData =
            new MutableLiveData<>();

    /**
     * Historial de entrenamientos.
     */
    private final MutableLiveData<Resource<List<Workout>>> workoutHistoryLiveData =
            new MutableLiveData<>();

    /**
     * Estado de actualización del perfil.
     */
    private final MutableLiveData<Resource<es.iesagora.fitjourney.data.model.User>>
            updateProfileLiveData = new MutableLiveData<>();

    /**
     * Estado de eliminación de cuenta.
     */
    private final MutableLiveData<Resource<String>> deleteAccountLiveData =
            new MutableLiveData<>();

    /**
     * Lista de rutinas.
     */
    private final MutableLiveData<Resource<List<Routine>>> routinesLiveData =
            new MutableLiveData<>();

    /**
     * Estado de acciones sobre rutinas (crear/eliminar).
     */
    private final MutableLiveData<Resource<String>> routineActionLiveData =
            new MutableLiveData<>();

    /**
     * Rutina obtenida por ID.
     */
    private final MutableLiveData<Resource<Routine>> singleRoutineLiveData =
            new MutableLiveData<>();

    /**
     * Lista completa de ejercicios (sin filtrar).
     */
    private List<Exercise> fullExerciseList = new ArrayList<>();

    /**
     * Constructor del ViewModel.
     * Inicializa repositorios y carga ejercicios iniciales.
     *
     * @param application aplicación Android.
     */
    public SecondViewModel(@NonNull Application application) {
        super(application);

        repository = new ExerciseRepository(application.getApplicationContext());
        userRepository = new UserRepository(application.getApplicationContext());
        workoutRepository = new WorkoutRepository(application.getApplicationContext());
        preferencesRepository = new PreferencesRepository(application.getApplicationContext());
        routineRepository = new RoutineRepository(application.getApplicationContext());

        fetchExercises();
    }

    /**
     * Devuelve LiveData de ejercicios.
     */
    public LiveData<Resource<List<Exercise>>> getExercises() {
        return exercisesLiveData;
    }

    /**
     * Devuelve el ejercicio seleccionado.
     */
    public LiveData<Exercise> getSelectedExercise() {
        return exerciseSelected;
    }

    /**
     * Devuelve un ejercicio cargado por ID.
     */
    public LiveData<Resource<Exercise>> getSingleExercise() {
        return singleExerciseLiveData;
    }

    /**
     * Selecciona un ejercicio.
     */
    public void selectExercise(Exercise exercise) {
        exerciseSelected.setValue(exercise);
    }

    /**
     * Obtiene todos los ejercicios desde el repositorio.
     */
    public void fetchExercises() {
        exercisesLiveData.setValue(Resource.loading());

        repository.getExercises(result -> {
            if (result.status == Resource.Status.SUCCESS && result.data != null) {
                fullExerciseList = new ArrayList<>(result.data);
            }
            exercisesLiveData.setValue(result);
        });
    }

    /**
     * Filtra ejercicios por nombre.
     */
    public void searchExercises(String query) {

        if (query == null || query.trim().isEmpty()) {
            exercisesLiveData.setValue(Resource.success(new ArrayList<>(fullExerciseList)));
            return;
        }

        List<Exercise> filteredList = new ArrayList<>();
        String filter = query.toLowerCase().trim();

        for (Exercise item : fullExerciseList) {
            if (item.getName().toLowerCase().contains(filter)) {
                filteredList.add(item);
            }
        }

        exercisesLiveData.setValue(Resource.success(filteredList));
    }

    /**
     * Obtiene un ejercicio por ID.
     */
    public void fetchExerciseById(int id) {
        singleExerciseLiveData.setValue(Resource.loading());
        repository.getExerciseById(id, singleExerciseLiveData::setValue);
    }

    /**
     * Elimina un ejercicio.
     */
    public void deleteExercise(int id, Runnable onSuccess) {
        repository.deleteExercise(id, result -> {
            if (result.status == Resource.Status.SUCCESS) {
                fetchExercises();
                onSuccess.run();
            } else {
                Toast.makeText(getApplication(),
                        result.message != null ? result.message : "Error al eliminar",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Crea un ejercicio.
     */
    public void createExercise(Exercise exercise, Runnable onSuccess) {
        repository.createExercise(exercise, result -> {
            if (result.status == Resource.Status.SUCCESS) {
                fetchExercises();
                onSuccess.run();
            }
        });
    }

    /**
     * Actualiza un ejercicio.
     */
    public void updateExercise(int id, Exercise exercise, Runnable onSuccess) {
        repository.updateExercise(id, exercise, result -> {
            if (result.status == Resource.Status.SUCCESS) {
                fetchExercises();
                onSuccess.run();
            }
        });
    }

    /**
     * Devuelve el perfil del usuario.
     */
    public LiveData<Resource<AccessResponse>> getProfile() {
        return profileLiveData;
    }

    /**
     * Obtiene el perfil del usuario desde la API.
     */
    public void fetchProfile(String token) {
        profileLiveData.setValue(Resource.loading());
        userRepository.getUser(token, profileLiveData::setValue);
    }

    /**
     * Actualiza el perfil del usuario.
     */
    public void updateProfile(es.iesagora.fitjourney.data.model.User user) {
        updateProfileLiveData.setValue(Resource.loading());

        userRepository.updateUserProfile(user, result -> {
            updateProfileLiveData.setValue(result);

            if (result.status == Resource.Status.SUCCESS) {
                fetchProfile(preferencesRepository.getToken());
            }
        });
    }

    /**
     * Estado de actualización del perfil.
     */
    public LiveData<Resource<es.iesagora.fitjourney.data.model.User>> getUpdateProfileStatus() {
        return updateProfileLiveData;
    }

    /**
     * Historial de entrenamientos.
     */
    public LiveData<Resource<List<Workout>>> getWorkoutHistory() {
        return workoutHistoryLiveData;
    }

    /**
     * Obtiene historial de entrenamientos.
     */
    public void fetchWorkoutHistory() {
        workoutHistoryLiveData.setValue(Resource.loading());
        workoutRepository.getWorkoutHistory(workoutHistoryLiveData::setValue);
    }

    /**
     * Estado de eliminación de cuenta.
     */
    public LiveData<Resource<String>> getDeleteAccountStatus() {
        return deleteAccountLiveData;
    }

    /**
     * Elimina la cuenta del usuario.
     */
    public void deleteMyAccount(String email, String password) {
        deleteAccountLiveData.setValue(Resource.loading());
        userRepository.deleteAccountPhysical(email, password, deleteAccountLiveData::setValue);
    }

    /**
     * Lista de rutinas.
     */
    public LiveData<Resource<List<Routine>>> getRoutines() {
        return routinesLiveData;
    }

    /**
     * Estado de acciones sobre rutinas.
     */
    public MutableLiveData<Resource<String>> getRoutineActionStatus() {
        return routineActionLiveData;
    }

    /**
     * Rutina seleccionada por ID.
     */
    public LiveData<Resource<Routine>> getSingleRoutine() {
        return singleRoutineLiveData;
    }

    /**
     * Obtiene todas las rutinas.
     */
    public void fetchRoutines() {
        routinesLiveData.setValue(Resource.loading());
        routineRepository.getRoutines(routinesLiveData::setValue);
    }

    /**
     * Crea una rutina.
     */
    public void createRoutine(Routine routine) {
        routineActionLiveData.setValue(Resource.loading());

        routineRepository.createRoutine(routine, result -> {
            routineActionLiveData.setValue(result);
            if (result.status == Resource.Status.SUCCESS) fetchRoutines();
        });
    }

    /**
     * Elimina una rutina.
     */
    public void deleteRoutine(int id) {
        routineActionLiveData.setValue(Resource.loading());

        routineRepository.deleteRoutine(id, result -> {
            routineActionLiveData.setValue(result);
            if (result.status == Resource.Status.SUCCESS) fetchRoutines();
        });
    }

    /**
     * Obtiene una rutina por ID.
     */
    public void fetchRoutineById(int id) {
        singleRoutineLiveData.setValue(Resource.loading());
        routineRepository.getRoutineById(id, singleRoutineLiveData::setValue);
    }

    /**
     * Obtiene la lista completa de todos los usuarios registrados en la aplicación.
     * (Acción reservada generalmente para usuarios con rol de administrador).
     * * El método se comunica con el repositorio y envuelve la respuesta
     * asíncrona (Callback) en un objeto LiveData. De esta forma, la vista
     * (Fragment/Activity) puede observar los datos y reaccionar a los
     * diferentes estados (Cargando, Éxito o Error).
     *
     * @return LiveData que contiene un Resource con la lista de usuarios (List<User>).
     */
    public LiveData<Resource<List<User>>> getAllUsers() {
        MutableLiveData<Resource<List<User>>> result = new MutableLiveData<>();

        userRepository.getAllUsers(result::setValue);

        return result;
    }

    /**
     * Llama al repositorio para eliminar un usuario desde la administración.
     *
     * @param email Correo del usuario a eliminar.
     * @return LiveData con el estado de la operación.
     */
    public LiveData<Resource<String>> deleteUserByAdmin(String email) {
        MutableLiveData<Resource<String>> result = new MutableLiveData<>();

        userRepository.deleteUserByAdmin(email, response -> {
            result.setValue(response);
        });

        return result;
    }

    /**
     * Llama al repositorio para banear a un usuario desde la administración.
     *
     * @param email Correo del usuario a banear.
     * @return LiveData con el estado de la operación.
     */
    public LiveData<Resource<String>> banUser(String email) {
        MutableLiveData<Resource<String>> result = new MutableLiveData<>();

        userRepository.banUser(email, response -> {
            result.setValue(response);
        });

        return result;
    }
}