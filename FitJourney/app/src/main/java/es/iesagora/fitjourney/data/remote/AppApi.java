package es.iesagora.fitjourney.data.remote;

import java.util.List;

import es.iesagora.fitjourney.data.model.Exercise;
import es.iesagora.fitjourney.data.model.Routine;
import es.iesagora.fitjourney.data.model.User;
import es.iesagora.fitjourney.data.model.Workout;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

/**
 * Interfaz principal de Retrofit para la comunicación
 * entre la aplicación y la API de FitJourney.
 * Define todos los endpoints relacionados con autenticación,
 * usuarios, ejercicios, rutinas y entrenamientos.
 */
public interface AppApi {

    // AUTH ----------------------------------------------------------

    /**
     * Registra un nuevo usuario.
     *
     * @param user Datos del usuario a registrar.
     * @return Respuesta con el token y la información del usuario.
     */
    @POST("auth/register")
    Call<AccessResponse> registerUser(@Body User user);

    /**
     * Inicia sesión en la aplicación.
     *
     * @param loginRequest Credenciales del usuario.
     * @return Respuesta con el token y los datos del usuario.
     */
    @POST("auth/login")
    Call<AccessResponse> loginUser(@Body LoginRequest loginRequest);

    /**
     * Reactiva una cuenta desactivada.
     *
     * @param loginRequest Credenciales del usuario.
     * @return Respuesta con el token de acceso.
     */
    @POST("auth/reactivate")
    Call<AccessResponse> reactivateAccount(@Body LoginRequest loginRequest);

    /**
     * Elimina permanentemente la cuenta del usuario.
     *
     * @param loginRequest Credenciales para confirmar la operación.
     * @return Confirmación de eliminación.
     */
    @POST("auth/delete")
    Call<ResponseBody> deleteAccountPhysical(@Body LoginRequest loginRequest);

    // USUARIOS -----------------------------------------------------------

    /**
     * Obtiene el perfil del usuario autenticado.
     *
     * @return Datos completos del usuario.
     */
    @GET("users/profile")
    Call<User> getUserProfile();

    /**
     * Actualiza el perfil del usuario.
     *
     * @param user Datos actualizados.
     * @return Usuario actualizado.
     */
    @PUT("users/profile")
    Call<User> updateUserProfile(@Body User user);

    /**
     * Obtiene todos los usuarios registrados.
     *
     * @return Lista de usuarios.
     */
    @GET("users")
    Call<List<User>> getAllUsers();

    /**
     * Actualiza un usuario desde administración.
     *
     * @param id Identificador del usuario.
     * @param user Nuevos datos del usuario.
     * @return Usuario actualizado.
     */
    @PUT("users/{id}")
    Call<User> updateUserByAdmin(@Path("id") int id, @Body User user);

    /**
     * Desactiva un usuario desde administración.
     *
     * @param userId Identificador del usuario.
     * @return Confirmación de desactivación.
     */
    @DELETE("users/{id}")
    Call<ResponseBody> deleteUserByAdmin(@Path("id") int userId);

    /**
     * Desactiva la cuenta del usuario actual.
     *
     * @param request Credenciales de confirmación.
     * @return Confirmación de desactivación.
     */
    @POST("users/deactivate")
    Call<ResponseBody> deactivateUser(@Body LoginRequest request);

    // EJERCICIOS ----------------------------------------------------

    /**
     * Obtiene todos los ejercicios disponibles.
     *
     * @return Lista de ejercicios.
     */
    @GET("exercises")
    Call<List<Exercise>> getExercises();

    /**
     * Obtiene un ejercicio por su ID.
     *
     * @param id Identificador del ejercicio.
     * @return Ejercicio encontrado.
     */
    @GET("exercises/{id}")
    Call<Exercise> getExerciseById(@Path("id") int id);

    /**
     * Crea un nuevo ejercicio.
     *
     * @param exercise Datos del ejercicio.
     * @return Ejercicio creado.
     */
    @POST("exercises")
    Call<Exercise> createExercise(@Body Exercise exercise);

    /**
     * Actualiza un ejercicio existente.
     *
     * @param id Identificador del ejercicio.
     * @param exercise Datos actualizados.
     * @return Confirmación de actualización.
     */
    @PUT("exercises/{id}")
    Call<Void> updateExercise(@Path("id") int id, @Body Exercise exercise);

    /**
     * Elimina un ejercicio.
     *
     * @param id Identificador del ejercicio.
     * @return Confirmación de eliminación.
     */
    @DELETE("exercises/{id}")
    Call<Void> deleteExercise(@Path("id") int id);

    // RUTINAS -----------------------------------------------------------

    /**
     * Obtiene las rutinas disponibles para el usuario.
     *
     * @return Lista de rutinas.
     */
    @GET("routines")
    Call<List<Routine>> getUserRoutines();

    /**
     * Crea una nueva rutina.
     *
     * @param routine Datos de la rutina.
     * @return Confirmación de creación.
     */
    @POST("routines")
    Call<ResponseBody> createRoutine(@Body Routine routine);

    /**
     * Elimina una rutina.
     *
     * @param id Identificador de la rutina.
     * @return Confirmación de eliminación.
     */
    @DELETE("routines/{id}")
    Call<ResponseBody> deleteRoutine(@Path("id") int id);

    /**
     * Obtiene una rutina por su ID.
     *
     * @param id Identificador de la rutina.
     * @return Rutina completa.
     */
    @GET("routines/{id}")
    Call<Routine> getRoutineById(@Path("id") int id);

    /**
     * Actualiza una rutina existente.
     *
     * @param id Identificador de la rutina.
     * @param routine Datos actualizados.
     * @return Confirmación de actualización.
     */
    @PUT("routines/{id}")
    Call<ResponseBody> updateRoutine(@Path("id") int id, @Body Routine routine);

    // WORKOUTS ------------------------------------------------------------

    /**
     * Obtiene el historial de entrenamientos del usuario.
     *
     * @return Lista de entrenamientos.
     */
    @GET("workouts/history")
    Call<List<Workout>> getWorkoutHistory();

    /**
     * Obtiene un entrenamiento por su ID.
     *
     * @param id Identificador del entrenamiento.
     * @return Entrenamiento completo.
     */
    @GET("workouts/{id}")
    Call<Workout> getWorkoutById(@Path("id") int id);

    /**
     * Guarda un nuevo entrenamiento.
     *
     * @param workout Datos del entrenamiento.
     * @return Confirmación de guardado.
     */
    @POST("workouts")
    Call<ResponseBody> createWorkout(@Body Workout workout);

    /**
     * Elimina un entrenamiento del historial.
     *
     * @param id Identificador del entrenamiento.
     * @return Confirmación de eliminación.
     */
    @DELETE("workouts/{id}")
    Call<ResponseBody> deleteWorkout(@Path("id") int id);
}