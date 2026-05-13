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
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

/**
 * Interfaz principal de Retrofit.
 * Define los endpoints de la API de FitJourney para la comunicación cliente-servidor.
 */
public interface AppApi {

    // ── AUTH ────────────────────────────────────────────────────────────────

    /**
     * Registra un nuevo usuario en el sistema.
     *
     * @param user Objeto con los datos del usuario a registrar.
     * @return Una respuesta que contiene el token de acceso y los datos básicos del usuario.
     */
    @POST("auth/register")
    Call<AccessResponse> registerUser(@Body User user);

    /**
     * Inicia sesión con las credenciales del usuario.
     *
     * @param loginRequest Objeto que contiene el email y la contraseña.
     * @return Una respuesta que contiene el token de acceso y el perfil del usuario.
     */
    @POST("auth/login")
    Call<AccessResponse> loginUser(@Body LoginRequest loginRequest);

    /**
     * Reactiva una cuenta de usuario previamente desactivada.
     *
     * @param loginRequest Objeto con las credenciales (email y contraseña) para validar la reactivación.
     * @return Una respuesta con el token de acceso para la cuenta restaurada.
     */
    @POST("auth/reactivate")
    Call<AccessResponse> reactivateAccount(@Body LoginRequest loginRequest);

    /**
     * Elimina físicamente y de forma irreversible la cuenta del usuario de la base de datos.
     *
     * @param loginRequest Objeto con las credenciales para autorizar la eliminación de la propia cuenta.
     * @return Un ResponseBody confirmando el éxito de la operación.
     */
    @POST("auth/delete")
    Call<ResponseBody> deleteAccountPhysical(@Body LoginRequest loginRequest);

    // ── USUARIOS ─────────────────────────────────────────────────────────────

    /**
     * Obtiene los datos del perfil del usuario actualmente autenticado.
     *
     * @return El objeto User con todos los datos personales del usuario.
     */
    @GET("users/profile")
    Call<User> getUserProfile();

    /**
     * Actualiza los datos del perfil del usuario actualmente autenticado.
     *
     * @param user Objeto User con los campos modificados.
     * @return El objeto User actualizado desde el servidor.
     */
    @PUT("users/profile")
    Call<User> updateUserProfile(@Body User user);

    /**
     * Obtiene una lista de todos los usuarios registrados en la plataforma.
     * Requiere permisos de Administrador.
     *
     * @return Una lista de objetos User.
     */
    @GET("users")
    Call<List<User>> getAllUsers();

    /**
     * Permite a un administrador actualizar los datos del perfil de cualquier usuario específico.
     *
     * @param id   Identificador único del usuario a modificar.
     * @param user Objeto User con los nuevos datos.
     * @return El objeto User actualizado.
     */
    @PUT("users/{id}")
    Call<User> updateUserByAdmin(@Path("id") int id, @Body User user);

    /**
     * Elimina lógicamente a un usuario y desactiva sus rutinas y entrenamientos asociados.
     * Requiere permisos de Administrador.
     *
     * @param userId Identificador único del usuario a eliminar.
     * @return Un ResponseBody confirmando la desactivación.
     */
    @DELETE("users/{id}")
    Call<ResponseBody> deleteUserByAdmin(@Path("id") int userId);

    /**
     * Desactiva (banea) la cuenta de un usuario impidiendo su acceso, modificando su estado activo.
     * Requiere permisos de Administrador.
     *
     * @param userId Identificador único del usuario a banear.
     * @return Un ResponseBody confirmando el baneo.
     */
    @PUT("users/{id}/ban")
    Call<ResponseBody> banUser(@Path("id") int userId);

    // ── EJERCICIOS ────────────────────────────────────────────────────────────

    /**
     * Obtiene el catálogo completo de ejercicios disponibles en la aplicación.
     *
     * @return Una lista de todos los objetos Exercise.
     */
    @GET("exercises")
    Call<List<Exercise>> getExercises();

    /**
     * Obtiene los detalles de un ejercicio específico mediante su ID.
     *
     * @param id Identificador único del ejercicio.
     * @return El objeto Exercise correspondiente.
     */
    @GET("exercises/{id}")
    Call<Exercise> getExerciseById(@Path("id") int id);

    /**
     * Crea un nuevo ejercicio y lo añade al catálogo global de la aplicación.
     * Requiere permisos de Administrador.
     *
     * @param exercise Objeto Exercise con la información del nuevo ejercicio.
     * @return El objeto Exercise recién creado en la base de datos.
     */
    @POST("exercises")
    Call<Exercise> createExercise(@Body Exercise exercise);

    /**
     * Actualiza la información (nombre, descripción, imagen, etc.) de un ejercicio existente.
     * Requiere permisos de Administrador.
     *
     * @param id       Identificador único del ejercicio a editar.
     * @param exercise Objeto Exercise con los datos actualizados.
     * @return Llamada sin contenido de respuesta (Void) que confirma el éxito HTTP.
     */
    @PUT("exercises/{id}")
    Call<Void> updateExercise(@Path("id") int id, @Body Exercise exercise);

    /**
     * Elimina un ejercicio del catálogo global.
     * Requiere permisos de Administrador.
     *
     * @param id Identificador único del ejercicio a borrar.
     * @return Llamada sin contenido de respuesta (Void) que confirma el borrado.
     */
    @DELETE("exercises/{id}")
    Call<Void> deleteExercise(@Path("id") int id);

    @POST("api/exercises/{id}/toggle")
    Call<Exercise> toggleExerciseStatus(@Path("id") int id);

    // ── FAVORITOS ─────────────────────────────────────────────────────────────

    /**
     * Obtiene la lista de ejercicios que el usuario autenticado ha marcado como favoritos.
     *
     * @return Una lista de objetos Exercise correspondientes a los favoritos del usuario.
     */
    @GET("favorites")
    Call<List<Exercise>> getUserFavorites();

    /**
     * Añade o elimina un ejercicio de la lista de favoritos del usuario (comportamiento de interruptor o "toggle").
     *
     * @param request Objeto que contiene el ID del ejercicio sobre el que aplicar el toggle.
     * @return Un ResponseBody confirmando el cambio de estado.
     */
    @POST("favorites")
    Call<ResponseBody> toggleFavorite(@Body FavoriteRequest request);

    // ── RUTINAS ───────────────────────────────────────────────────────────────

    /**
     * Obtiene una lista de rutinas accesibles para el usuario (sus rutinas privadas y las públicas del sistema).
     *
     * @return Lista de objetos Routine.
     */
    @GET("routines")
    Call<List<Routine>> getUserRoutines();

    /**
     * Crea una nueva rutina de entrenamiento asociada al usuario que realiza la petición.
     *
     * @param routine Objeto Routine que contiene el nombre, descripción, privacidad y la lista de ejercicios.
     * @return Un ResponseBody confirmando la creación exitosa de la rutina.
     */
    @POST("routines")
    Call<ResponseBody> createRoutine(@Body Routine routine);

    /**
     * Elimina de forma física una rutina creada previamente.
     * Requiere que el usuario sea el propietario de la rutina o tenga rol de administrador.
     *
     * @param id Identificador único de la rutina a borrar.
     * @return Un ResponseBody confirmando la eliminación física.
     */
    @DELETE("routines/{id}")
    Call<ResponseBody> deleteRoutine(@Path("id") int id);

    /**
     * Obtiene los detalles completos de una rutina específica, incluyendo todos sus ejercicios y configuraciones.
     *
     * @param id Identificador de la rutina.
     * @return El objeto Routine con todos sus datos anidados.
     */
    @GET("routines/{id}")
    Call<Routine> getRoutineById(@Path("id") int id);

    // ── WORKOUTS ──────────────────────────────────────────────────────────────

    /**
     * Obtiene el historial completo de entrenamientos realizados (Workouts) por el usuario autenticado.
     *
     * @return Una lista de objetos Workout.
     */
    @GET("workouts/history")
    Call<List<Workout>> getWorkoutHistory();

    /**
     * Obtiene los detalles de una sesión de entrenamiento específica finalizada previamente.
     *
     * @param id Identificador único de la sesión de entrenamiento.
     * @return El objeto Workout con los detalles y ejercicios realizados.
     */
    @GET("workouts/{id}")
    Call<Workout> getWorkoutById(@Path("id") int id);

    /**
     * Registra una nueva sesión de entrenamiento finalizada en el historial del usuario.
     *
     * @param workout Objeto Workout con la duración, calorías, notas y los detalles de los ejercicios realizados.
     * @return Un ResponseBody confirmando el guardado del entrenamiento.
     */
    @POST("workouts")
    Call<ResponseBody> createWorkout(@Body Workout workout);

    /**
     * Elimina un registro de entrenamiento específico del historial del usuario.
     *
     * @param id Identificador de la sesión de entrenamiento a borrar.
     * @return Un ResponseBody confirmando la eliminación exitosa.
     */
    @DELETE("workouts/{id}")
    Call<ResponseBody> deleteWorkout(@Path("id") int id);
}