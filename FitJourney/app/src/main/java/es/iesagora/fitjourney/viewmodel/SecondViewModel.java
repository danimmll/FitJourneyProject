package es.iesagora.fitjourney.viewmodel;

import static android.content.ContentValues.TAG;

import android.app.Application;
import android.util.Log;

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
import es.iesagora.fitjourney.data.remote.RetrofitClient;
import es.iesagora.fitjourney.data.repositories.ExerciseRepository;
import es.iesagora.fitjourney.data.repositories.PreferencesRepository;
import es.iesagora.fitjourney.data.repositories.RoutineRepository;
import es.iesagora.fitjourney.data.repositories.UserRepository;
import es.iesagora.fitjourney.data.repositories.WorkoutRepository;

/**
 * ViewModel principal de la aplicación FitJourney.
 *
 * <p>Centraliza toda la lógica de negocio relacionada con:
 * ejercicios, rutinas, entrenamientos y perfil de usuario.</p>
 *
 * <p>Actúa como capa intermedia entre UI (Fragments/Activities)
 * y los repositorios de datos, exponiendo estados mediante LiveData.</p>
 */
public class SecondViewModel extends AndroidViewModel {

    /** Repositorio de ejercicios */
    private final ExerciseRepository repository;

    /** Repositorio de usuarios */
    private final UserRepository userRepository;

    /** Repositorio de preferencias locales */
    private final PreferencesRepository preferencesRepository;

    /** Repositorio de entrenamientos */
    private final WorkoutRepository workoutRepository;

    /** Repositorio de rutinas */
    private final RoutineRepository routineRepository;

    /** Lista observable de ejercicios */
    private final MutableLiveData<Resource<List<Exercise>>> exercisesLiveData = new MutableLiveData<>();

    /** Ejercicio seleccionado actualmente en memoria */
    private final MutableLiveData<Exercise> exerciseSelected = new MutableLiveData<>();

    /** Estado del perfil del usuario autenticado */
    private final MutableLiveData<Resource<AccessResponse>> profileLiveData = new MutableLiveData<>();

    /** Historial de entrenamientos */
    private final MutableLiveData<Resource<List<Workout>>> workoutHistoryLiveData = new MutableLiveData<>();

    /** Estado de actualización de perfil */
    private final MutableLiveData<Resource<User>> updateProfileLiveData = new MutableLiveData<>();

    /** Estado de eliminación/desactivación de cuenta */
    private final MutableLiveData<Resource<String>> deleteAccountLiveData = new MutableLiveData<>();

    /** Lista de rutinas */
    private final MutableLiveData<Resource<List<Routine>>> routinesLiveData = new MutableLiveData<>();

    /** Estado de acciones sobre rutinas */
    private final MutableLiveData<Resource<String>> routineActionLiveData = new MutableLiveData<>();

    /** Rutina individual cargada por ID */
    private final MutableLiveData<Resource<Routine>> singleRoutineLiveData = new MutableLiveData<>();

    /** Lista de usuarios (modo admin) */
    private final MutableLiveData<Resource<List<User>>> allUsersLiveData = new MutableLiveData<>();

    /** Estado de eliminación de usuario (admin) */
    private final MutableLiveData<Resource<String>> deleteUserLiveData = new MutableLiveData<>();

    /** Estado de baneo de usuario (admin) */
    private final MutableLiveData<Resource<String>> banUserLiveData = new MutableLiveData<>();

    /** Estado de creación/eliminación de entrenamientos */
    private final MutableLiveData<Resource<String>> workoutActionLiveData = new MutableLiveData<>();

    /** Entrenamiento individual */
    private final MutableLiveData<Resource<Workout>> singleWorkoutLiveData = new MutableLiveData<>();

    /** Cache local de ejercicios */
    private List<Exercise> fullExerciseList = new ArrayList<>();

    /**
     * Constructor del ViewModel.
     *
     * Inicializa repositorios y carga automáticamente los ejercicios iniciales.
     *
     * @param application contexto de la aplicación Android
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
     * Obtiene LiveData de ejercicios.
     *
     * @return lista reactiva de ejercicios
     */
    public LiveData<Resource<List<Exercise>>> getExercises() {
        return exercisesLiveData;
    }

    /**
     * Devuelve el ejercicio actualmente seleccionado.
     *
     * @return LiveData del ejercicio seleccionado
     */
    public LiveData<Exercise> getSelectedExercise() {
        return exerciseSelected;
    }

    /**
     * Guarda un ejercicio como seleccionado en memoria.
     *
     * @param exercise ejercicio a seleccionar
     */
    public void selectExercise(Exercise exercise) {
        exerciseSelected.setValue(exercise);
    }

    /**
     * Obtiene todos los ejercicios desde el servidor.
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
     *
     * @param query texto de búsqueda
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
     * Elimina un ejercicio.
     *
     * @param id identificador del ejercicio
     * @param onSuccess callback tras éxito
     */
    public void deleteExercise(int id, Runnable onSuccess) {
        repository.deleteExercise(id, result -> {
            if (result.status == Resource.Status.SUCCESS) {
                fetchExercises();
                onSuccess.run();
            } else {
                Log.d(TAG, "deleteExercise: error" );
            }
        });
    }

    /**
     * Crea un nuevo ejercicio.
     *
     * @param exercise datos del ejercicio
     * @param onSuccess callback tras éxito
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
     * Actualiza un ejercicio existente.
     *
     * @param id identificador
     * @param exercise datos nuevos
     * @param onSuccess callback
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
     * Obtiene el perfil del usuario.
     *
     * @return LiveData del perfil
     */
    public LiveData<Resource<AccessResponse>> getProfile() {
        return profileLiveData;
    }

    /**
     * Solicita el perfil al servidor.
     *
     * @param token token de autenticación
     */
    public void fetchProfile(String token) {
        profileLiveData.setValue(Resource.loading());
        userRepository.getUser(token, profileLiveData::setValue);
    }

    /**
     * Actualiza el perfil del usuario autenticado.
     *
     * @param user datos nuevos
     */
    public void updateProfile(User user) {
        updateProfileLiveData.setValue(Resource.loading());

        userRepository.updateUserProfile(user, result -> {
            updateProfileLiveData.setValue(result);

            if (result.status == Resource.Status.SUCCESS) {
                fetchProfile(preferencesRepository.getToken());
            }
        });
    }

    /**
     * Actualiza un usuario como administrador.
     *
     * @param userId id usuario
     * @param user datos nuevos
     */
    public void updateUserByAdmin(int userId, User user) {
        updateProfileLiveData.setValue(Resource.loading());
        userRepository.updateUserByAdmin(userId, user, updateProfileLiveData::setValue);
    }

    /**
     * Estado de actualización de perfil.
     *
     * @return LiveData estado
     */
    public LiveData<Resource<User>> getUpdateProfileStatus() {
        return updateProfileLiveData;
    }

    /**
     * Cierra sesión del usuario.
     */
    public void logout() {
        preferencesRepository.clearSession();
        RetrofitClient.reset();
    }

    /**
     * Historial de entrenamientos.
     */
    public LiveData<Resource<List<Workout>>> getWorkoutHistory() {
        return workoutHistoryLiveData;
    }

    /**
     * Carga historial de entrenamientos.
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
     * Elimina cuenta del usuario autenticado.
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
     * Rutina individual.
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
     * Crea rutina.
     */
    public void createRoutine(Routine routine) {
        routineActionLiveData.setValue(Resource.loading());

        routineRepository.createRoutine(routine, result -> {
            routineActionLiveData.setValue(result);
            if (result.status == Resource.Status.SUCCESS) fetchRoutines();
        });
    }

    /**
     * Elimina rutina.
     */
    public void deleteRoutine(int id) {
        routineActionLiveData.setValue(Resource.loading());

        routineRepository.deleteRoutine(id, result -> {
            routineActionLiveData.setValue(result);
            if (result.status == Resource.Status.SUCCESS) fetchRoutines();
        });
    }

    /**
     * Carga rutina por ID.
     */
    public void fetchRoutineById(int id) {
        singleRoutineLiveData.setValue(Resource.loading());
        routineRepository.getRoutineById(id, singleRoutineLiveData::setValue);
    }

    /**
     * Actualiza rutina.
     */
    public void updateRoutine(int id, Routine routine) {
        routineActionLiveData.setValue(Resource.loading());

        routineRepository.updateRoutine(id, routine, result -> {
            routineActionLiveData.setValue(result);
            if (result.status == Resource.Status.SUCCESS) fetchRoutines();
        });
    }

    /**
     * Obtiene usuarios (admin).
     */
    public LiveData<Resource<List<User>>> getAllUsers() {
        allUsersLiveData.setValue(Resource.loading());
        userRepository.getAllUsers(allUsersLiveData::setValue);
        return allUsersLiveData;
    }

    /**
     * Baneo usuario.
     */
    public LiveData<Resource<String>> getBanUserLiveData() {
        return banUserLiveData;
    }

    /**
     * Eliminación usuario.
     */
    public LiveData<Resource<String>> getDeleteUserLiveData() {
        return deleteUserLiveData;
    }

    /**
     * Elimina usuario (admin).
     */
    public LiveData<Resource<String>> deleteUserByAdmin(int userId) {
        deleteUserLiveData.setValue(Resource.loading());
        userRepository.deleteUserByAdmin(userId, deleteUserLiveData::setValue);
        return deleteUserLiveData;
    }

    /**
     * Desactiva cuenta propia.
     */
    public void deactivateMyAccount(String email, String password) {
        deleteAccountLiveData.setValue(Resource.loading());
        userRepository.deactivateUser(email, password, deleteAccountLiveData::setValue);
    }

    /**
     * Estado de acciones sobre entrenamientos.
     */
    public MutableLiveData<Resource<String>> getWorkoutActionStatus() {
        return workoutActionLiveData;
    }

    /**
     * Crea entrenamiento.
     */
    public void createWorkout(Workout workout) {
        workoutActionLiveData.setValue(Resource.loading());

        workoutRepository.createWorkout(workout, result -> {
            workoutActionLiveData.setValue(result);

            if (result.status == Resource.Status.SUCCESS) {
                fetchWorkoutHistory();
            }
        });
    }

    /**
     * Elimina entrenamiento.
     */
    public void deleteWorkout(int idWorkout) {
        workoutActionLiveData.setValue(Resource.loading());
        workoutRepository.deleteWorkout(idWorkout, result -> {
            workoutActionLiveData.setValue(result);
            if (result.status == Resource.Status.SUCCESS) {
                fetchWorkoutHistory();
            }
        });
    }

    /**
     * Entrenamiento individual.
     */
    public LiveData<Resource<Workout>> getSingleWorkout() {
        return singleWorkoutLiveData;
    }

    /**
     * Carga entrenamiento por ID.
     */
    public void fetchWorkoutById(int id) {
        singleWorkoutLiveData.setValue(Resource.loading());
        workoutRepository.getWorkoutById(id, singleWorkoutLiveData::setValue);
    }
}