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
 * Interfaz principal de Retrofit.
 * CORRECCIONES:
 *  - auth/physical-delete → auth/delete  (ruta real del backend)
 *  - favorites/toggle     → favorites    (ruta real del backend)
 *  - Rutas de admin ajustadas al backend real
 */
public interface AppApi {

    // ── AUTH ────────────────────────────────────────────────────────────────

    @POST("auth/register")
    Call<AccessResponse> registerUser(@Body User user);

    @POST("auth/login")
    Call<AccessResponse> loginUser(@Body LoginRequest loginRequest);

    @POST("auth/reactivate")
    Call<AccessResponse> reactivateAccount(@Body LoginRequest loginRequest);

    /**
     * CORREGIDO: era "auth/physical-delete", el backend usa "auth/delete".
     */
    @POST("auth/delete")
    Call<ResponseBody> deleteAccountPhysical(@Body LoginRequest loginRequest);

    // ── USUARIOS ─────────────────────────────────────────────────────────────

    @GET("users/profile")
    Call<User> getUserProfile();

    @PUT("users/profile")
    Call<User> updateUserProfile(@Body User user);

    @GET("users")
    Call<List<User>> getAllUsers();

    /**
     * Elimina un usuario desde el panel de administrador.
     * Ruta real del backend: DELETE /api/users/:id
     * Aquí usamos email como identificador — ajusta si tu backend usa id.
     */
    @DELETE("users/{id}")
    Call<ResponseBody> deleteUserByAdmin(@Path("id") int userId);

    /**
     * Desactiva (banea) un usuario desde el panel de administrador.
     * Ruta real del backend: PUT /api/users/:id  con active=0 en body.
     * Ajusta la ruta si tu backend tiene un endpoint específico de ban.
     */
    @PUT("users/{id}/ban")
    Call<ResponseBody> banUser(@Path("id") int userId);

    // ── EJERCICIOS ────────────────────────────────────────────────────────────

    @GET("exercises")
    Call<List<Exercise>> getExercises();

    @GET("exercises/{id}")
    Call<Exercise> getExerciseById(@Path("id") int id);

    @POST("exercises")
    Call<Exercise> createExercise(@Body Exercise exercise);

    @PUT("exercises/{id}")
    Call<Void> updateExercise(@Path("id") int id, @Body Exercise exercise);

    @DELETE("exercises/{id}")
    Call<Void> deleteExercise(@Path("id") int id);

    // ── FAVORITOS ─────────────────────────────────────────────────────────────

    @GET("favorites")
    Call<List<Exercise>> getUserFavorites();

    /**
     * CORREGIDO: era "favorites/toggle", el backend usa "favorites" (POST directo).
     */
    @POST("favorites")
    Call<ResponseBody> toggleFavorite(@Body FavoriteRequest request);

    // ── RUTINAS ───────────────────────────────────────────────────────────────

    @GET("routines")
    Call<List<Routine>> getUserRoutines();

    @POST("routines")
    Call<ResponseBody> createRoutine(@Body Routine routine);

    @DELETE("routines/{id}")
    Call<ResponseBody> deleteRoutine(@Path("id") int id);

    @GET("routines/{id}")
    Call<Routine> getRoutineById(@Path("id") int id);

    // ── WORKOUTS ──────────────────────────────────────────────────────────────

    @GET("workouts/history")
    Call<List<Workout>> getWorkoutHistory();

    @GET("workouts/{id}")
    Call<Workout> getWorkoutById(@Path("id") int id);

    @POST("workouts")
    Call<ResponseBody> createWorkout(@Body Workout workout);

    @DELETE("workouts/{id}")
    Call<ResponseBody> deleteWorkout(@Path("id") int id);
}