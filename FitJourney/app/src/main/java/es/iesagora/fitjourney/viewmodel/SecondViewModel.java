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
 * ViewModel secundario encargado de la lógica principal de la aplicación.
 * Gestiona ejercicios, rutinas, entrenamientos y perfil de usuario.
 * Actúa como puente entre la capa de datos (repositories) y la UI,
 * exponiendo estados mediante LiveData.
 */
public class SecondViewModel extends AndroidViewModel {

    private final ExerciseRepository repository;
    private final UserRepository userRepository;
    private final PreferencesRepository preferencesRepository;
    private final WorkoutRepository workoutRepository;
    private final RoutineRepository routineRepository;

    private final MutableLiveData<Resource<List<Exercise>>> exercisesLiveData = new MutableLiveData<>();
    private final MutableLiveData<Exercise> exerciseSelected = new MutableLiveData<>();
    private final MutableLiveData<Resource<AccessResponse>> profileLiveData = new MutableLiveData<>();
    private final MutableLiveData<Resource<List<Workout>>> workoutHistoryLiveData = new MutableLiveData<>();
    private final MutableLiveData<Resource<User>> updateProfileLiveData = new MutableLiveData<>();
    private final MutableLiveData<Resource<String>> deleteAccountLiveData = new MutableLiveData<>();
    private final MutableLiveData<Resource<List<Routine>>> routinesLiveData = new MutableLiveData<>();
    private final MutableLiveData<Resource<String>> routineActionLiveData = new MutableLiveData<>();
    private final MutableLiveData<Resource<Routine>> singleRoutineLiveData = new MutableLiveData<>();
    private final MutableLiveData<Resource<List<User>>> allUsersLiveData = new MutableLiveData<>();
    private final MutableLiveData<Resource<String>> deleteUserLiveData = new MutableLiveData<>();
    private final MutableLiveData<Resource<String>> banUserLiveData = new MutableLiveData<>();
    private final MutableLiveData<Resource<String>> workoutActionLiveData = new MutableLiveData<>();
    private final MutableLiveData<Resource<Workout>> singleWorkoutLiveData = new MutableLiveData<>();

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
     * Devuelve LiveData de la lista de ejercicios.
     *
     * @return LiveData con estado y lista de ejercicios.
     */
    public LiveData<Resource<List<Exercise>>> getExercises() {
        return exercisesLiveData;
    }

    /**
     * Devuelve el ejercicio seleccionado en memoria.
     *
     * @return LiveData del ejercicio.
     */
    public LiveData<Exercise> getSelectedExercise() {
        return exerciseSelected;
    }

    /**
     * Almacena en memoria el ejercicio seleccionado.
     *
     * @param exercise Ejercicio a seleccionar.
     */
    public void selectExercise(Exercise exercise) {
        exerciseSelected.setValue(exercise);
    }

    /**
     * Obtiene el catálogo completo de ejercicios desde el servidor.
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
     * Filtra la lista local de ejercicios por nombre.
     *
     * @param query Texto a buscar en los nombres de los ejercicios.
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
     * Elimina un ejercicio del servidor.
     *
     * @param id        Identificador del ejercicio a borrar.
     * @param onSuccess Callback a ejecutar tras la eliminación exitosa.
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
     * Crea un nuevo ejercicio en el servidor.
     *
     * @param exercise  Objeto con los datos del nuevo ejercicio.
     * @param onSuccess Callback a ejecutar tras la creación exitosa.
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
     * Actualiza la información de un ejercicio existente.
     *
     * @param id        Identificador del ejercicio.
     * @param exercise  Objeto con los nuevos datos.
     * @param onSuccess Callback a ejecutar tras la actualización exitosa.
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
     * Devuelve el estado de la carga del perfil del usuario.
     *
     * @return LiveData con estado y perfil.
     */
    public LiveData<Resource<AccessResponse>> getProfile() {
        return profileLiveData;
    }

    /**
     * Solicita al servidor los datos actualizados del usuario autenticado.
     *
     * @param token Token de acceso actual.
     */
    public void fetchProfile(String token) {
        profileLiveData.setValue(Resource.loading());
        userRepository.getUser(token, profileLiveData::setValue);
    }

    /**
     * Actualiza los datos del perfil del propio usuario autenticado.
     *
     * @param user Objeto User con las modificaciones.
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
     * Actualiza los datos del perfil de un usuario específico (Solo administradores).
     *
     * @param userId Identificador del usuario a editar.
     * @param user   Objeto User con las modificaciones.
     */
    public void updateUserByAdmin(int userId, User user) {
        updateProfileLiveData.setValue(Resource.loading());
        userRepository.updateUserByAdmin(userId, user, updateProfileLiveData::setValue);
    }

    /**
     * Devuelve el estado de la última solicitud de actualización de perfil.
     *
     * @return LiveData con estado de la actualización.
     */
    public LiveData<Resource<User>> getUpdateProfileStatus() {
        return updateProfileLiveData;
    }

    /**
     * Cierra la sesión local del usuario y limpia tokens y preferencias.
     */
    public void logout() {
        preferencesRepository.clearSession();
        RetrofitClient.reset();
    }

    /**
     * Devuelve el historial de entrenamientos completados.
     *
     * @return LiveData con estado e historial.
     */
    public LiveData<Resource<List<Workout>>> getWorkoutHistory() {
        return workoutHistoryLiveData;
    }

    /**
     * Solicita al servidor el historial completo de entrenamientos del usuario.
     */
    public void fetchWorkoutHistory() {
        workoutHistoryLiveData.setValue(Resource.loading());
        workoutRepository.getWorkoutHistory(workoutHistoryLiveData::setValue);
    }

    /**
     * Devuelve el estado de la solicitud de eliminación de cuenta.
     *
     * @return LiveData con estado de la eliminación.
     */
    public LiveData<Resource<String>> getDeleteAccountStatus() {
        return deleteAccountLiveData;
    }

    /**
     * Elimina permanentemente la cuenta del usuario autenticado.
     *
     * @param email    Email de confirmación.
     * @param password Contraseña de confirmación.
     */
    public void deleteMyAccount(String email, String password) {
        deleteAccountLiveData.setValue(Resource.loading());
        userRepository.deleteAccountPhysical(email, password, deleteAccountLiveData::setValue);
    }

    /**
     * Devuelve la lista de rutinas disponibles.
     *
     * @return LiveData con estado y lista de rutinas.
     */
    public LiveData<Resource<List<Routine>>> getRoutines() {
        return routinesLiveData;
    }

    /**
     * Devuelve el estado de la última acción realizada sobre una rutina.
     *
     * @return MutableLiveData del estado de la acción.
     */
    public MutableLiveData<Resource<String>> getRoutineActionStatus() {
        return routineActionLiveData;
    }

    /**
     * Devuelve los detalles de una rutina cargada por ID.
     *
     * @return LiveData con estado y rutina individual.
     */
    public LiveData<Resource<Routine>> getSingleRoutine() {
        return singleRoutineLiveData;
    }

    /**
     * Solicita al servidor todas las rutinas públicas y las privadas del usuario.
     */
    public void fetchRoutines() {
        routinesLiveData.setValue(Resource.loading());
        routineRepository.getRoutines(routinesLiveData::setValue);
    }

    /**
     * Registra una nueva rutina en el servidor.
     *
     * @param routine Objeto con los datos de la rutina.
     */
    public void createRoutine(Routine routine) {
        routineActionLiveData.setValue(Resource.loading());

        routineRepository.createRoutine(routine, result -> {
            routineActionLiveData.setValue(result);
            if (result.status == Resource.Status.SUCCESS) fetchRoutines();
        });
    }

    /**
     * Elimina físicamente una rutina del servidor.
     *
     * @param id Identificador de la rutina.
     */
    public void deleteRoutine(int id) {
        routineActionLiveData.setValue(Resource.loading());

        routineRepository.deleteRoutine(id, result -> {
            routineActionLiveData.setValue(result);
            if (result.status == Resource.Status.SUCCESS) fetchRoutines();
        });
    }

    /**
     * Obtiene los detalles y ejercicios de una rutina mediante su ID.
     *
     * @param id Identificador de la rutina.
     */
    public void fetchRoutineById(int id) {
        singleRoutineLiveData.setValue(Resource.loading());
        routineRepository.getRoutineById(id, singleRoutineLiveData::setValue);
    }

    /**
     * Actualiza una rutina existente en el servidor.
     *
     * @param id Identificador de la rutina a actualizar.
     * @param routine Objeto con los nuevos datos de la rutina.
     */
    public void updateRoutine(int id, Routine routine) {
        routineActionLiveData.setValue(Resource.loading());

        routineRepository.updateRoutine(id, routine, result -> {
            routineActionLiveData.setValue(result);
            if (result.status == Resource.Status.SUCCESS) fetchRoutines();
        });
    }

    /**
     * Solicita al servidor el directorio completo de usuarios (Solo administradores).
     *
     * @return LiveData con el estado y la lista de usuarios.
     */
    public LiveData<Resource<List<User>>> getAllUsers() {
        allUsersLiveData.setValue(Resource.loading());
        userRepository.getAllUsers(allUsersLiveData::setValue);
        return allUsersLiveData;
    }

    /**
     * Devuelve el estado de una solicitud de baneo de usuario.
     *
     * @return LiveData con el estado de la acción.
     */
    public LiveData<Resource<String>> getBanUserLiveData() {
        return banUserLiveData;
    }

    /**
     * Devuelve el estado de una solicitud de eliminación de usuario.
     *
     * @return LiveData con el estado de la acción.
     */
    public LiveData<Resource<String>> getDeleteUserLiveData() {
        return deleteUserLiveData;
    }

    /**
     * Ejecuta un borrado lógico de un usuario y sus registros relacionados (Solo administradores).
     *
     * @param userId Identificador del usuario a eliminar.
     * @return LiveData para monitorizar el estado de la petición.
     */
    public LiveData<Resource<String>> deleteUserByAdmin(int userId) {
        deleteUserLiveData.setValue(Resource.loading());
        userRepository.deleteUserByAdmin(userId, deleteUserLiveData::setValue);
        return deleteUserLiveData;
    }

    /**
     * Desactiva lógicamente la cuenta del propio usuario.
     *
     * @param email    Email de confirmación.
     * @param password Contraseña de confirmación.
     */
    public void deactivateMyAccount(String email, String password) {
        deleteAccountLiveData.setValue(Resource.loading());
        userRepository.deactivateUser(email, password, deleteAccountLiveData::setValue);
    }

    /**
     * Devuelve el estado de la última acción realizada sobre un entrenamiento (como guardarlo).
     *
     * @return MutableLiveData del estado de la acción.
     */
    public MutableLiveData<Resource<String>> getWorkoutActionStatus() {
        return workoutActionLiveData;
    }

    /**
     * Envía un entrenamiento finalizado al servidor para guardarlo en el historial.
     *
     * @param workout Objeto Workout que contiene el ID de la rutina, duración y detalles de ejercicios.
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

    public void deleteWorkout(int idWorkout) {
        workoutActionLiveData.setValue(Resource.loading());
        workoutRepository.deleteWorkout(idWorkout, result -> {
            workoutActionLiveData.setValue(result);
            if (result.status == Resource.Status.SUCCESS) {
                fetchWorkoutHistory();
            }
        });
    }

    public LiveData<Resource<Workout>> getSingleWorkout() {
        return singleWorkoutLiveData;
    }

    public void fetchWorkoutById(int id) {
        singleWorkoutLiveData.setValue(Resource.loading());
        workoutRepository.getWorkoutById(id, singleWorkoutLiveData::setValue);
    }
}