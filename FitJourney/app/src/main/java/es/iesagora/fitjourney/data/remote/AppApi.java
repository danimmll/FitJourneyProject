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
 * Interfaz principal de Retrofit utilizada para comunicarse
 * con la API de la aplicación.
 * Aquí se definen todos los endpoints disponibles relacionados
 * con autenticación, usuarios, ejercicios, favoritos,
 * rutinas y entrenamientos.
 */
public interface AppApi {

    /**
     * Registra un nuevo usuario en la aplicación.
     *
     * @param user Datos del usuario a registrar.
     * @return Respuesta con el usuario creado y token de acceso.
     */
    @POST("auth/register")
    Call<AccessResponse> registerUser(@Body User user);

    /**
     * Inicia sesión con las credenciales del usuario.
     *
     * @param loginRequest Datos de acceso del usuario.
     * @return Respuesta con el usuario autenticado y token.
     */
    @POST("auth/login")
    Call<AccessResponse> loginUser(@Body LoginRequest loginRequest);

    /**
     * Reactiva una cuenta desactivada.
     *
     * @param loginRequest Credenciales del usuario.
     * @return Respuesta con el usuario reactivado y token.
     */
    @POST("auth/reactivate")
    Call<AccessResponse> reactivateAccount(@Body LoginRequest loginRequest);

    /**
     * Elimina físicamente una cuenta de usuario.
     *
     * @param loginRequest Credenciales del usuario.
     * @return Respuesta de confirmación.
     */
    @POST("auth/physical-delete")
    Call<ResponseBody> deleteAccountPhysical(@Body LoginRequest loginRequest);

    /**
     * Obtiene el perfil del usuario autenticado.
     *
     * @return Información del usuario.
     */
    @GET("users/profile")
    Call<User> getUserProfile();

    /**
     * Actualiza los datos del perfil del usuario.
     *
     * @param user Datos actualizados del usuario.
     * @return Usuario actualizado.
     */
    @PUT("users/profile")
    Call<User> updateUserProfile(@Body User user);

    /**
     * Obtiene la lista completa de ejercicios.
     *
     * @return Lista de ejercicios.
     */
    @GET("exercises")
    Call<List<Exercise>> getExercises();

    /**
     * Obtiene un ejercicio concreto mediante su ID.
     *
     * @param id Identificador del ejercicio.
     * @return Ejercicio solicitado.
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
     * @param id       Identificador del ejercicio.
     * @param exercise Datos actualizados.
     * @return Respuesta vacía de confirmación.
     */
    @PUT("exercises/{id}")
    Call<Void> updateExercise(@Path("id") int id, @Body Exercise exercise);

    /**
     * Elimina un ejercicio.
     *
     * @param id Identificador del ejercicio.
     * @return Respuesta vacía de confirmación.
     */
    @DELETE("exercises/{id}")
    Call<Void> deleteExercise(@Path("id") int id);

    /**
     * Obtiene la lista de ejercicios favoritos del usuario.
     *
     * @return Lista de favoritos.
     */
    @GET("favorites")
    Call<List<Exercise>> getUserFavorites();

    /**
     * Añade o elimina un ejercicio de favoritos.
     *
     * @param request Datos del favorito.
     * @return Respuesta de confirmación.
     */
    @POST("favorites/toggle")
    Call<ResponseBody> toggleFavorite(@Body FavoriteRequest request);

    /**
     * Obtiene las rutinas disponibles del usuario.
     *
     * @return Lista de rutinas.
     */
    @GET("routines")
    Call<List<Routine>> getUserRoutines();

    /**
     * Crea una nueva rutina.
     *
     * @param routine Datos de la rutina.
     * @return Respuesta de confirmación.
     */
    @POST("routines")
    Call<ResponseBody> createRoutine(@Body Routine routine);

    /**
     * Elimina una rutina existente.
     *
     * @param id Identificador de la rutina.
     * @return Respuesta de confirmación.
     */
    @DELETE("routines/{id}")
    Call<ResponseBody> deleteRoutine(@Path("id") int id);

    /**
     * Obtiene una rutina concreta mediante su ID.
     *
     * @param id Identificador de la rutina.
     * @return Rutina solicitada.
     */
    @GET("routines/{id}")
    Call<Routine> getRoutineById(@Path("id") int id);

    /**
     * Obtiene el historial de entrenamientos del usuario.
     *
     * @return Lista de entrenamientos realizados.
     */
    @GET("workouts/history")
    Call<List<Workout>> getWorkoutHistory();

    /**
     * Obtiene un entrenamiento concreto mediante su ID.
     *
     * @param id Identificador del entrenamiento.
     * @return Entrenamiento solicitado.
     */
    @GET("workouts/{id}")
    Call<Workout> getWorkoutById(@Path("id") int id);

    /**
     * Guarda un nuevo entrenamiento.
     *
     * @param workout Datos del entrenamiento.
     * @return Respuesta de confirmación.
     */
    @POST("workouts")
    Call<ResponseBody> createWorkout(@Body Workout workout);

    /**
     * Elimina un entrenamiento existente.
     *
     * @param id Identificador del entrenamiento.
     * @return Respuesta de confirmación.
     */
    @DELETE("workouts/{id}")
    Call<ResponseBody> deleteWorkout(@Path("id") int id);
}