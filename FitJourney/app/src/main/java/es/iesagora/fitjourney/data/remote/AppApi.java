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

public interface AppApi {

    @POST("auth/register")
    Call<AccessResponse> registerUser(@Body User user);

    @POST("auth/login")
    Call<AccessResponse> loginUser(@Body LoginRequest loginRequest);

    @POST("auth/reactivate")
    Call<AccessResponse> reactivateAccount(@Body LoginRequest loginRequest);

    @POST("auth/physical-delete")
    Call<ResponseBody> deleteAccountPhysical(@Body LoginRequest loginRequest);

    @GET("users/profile")
    Call<User> getUserProfile();

    @PUT("users/profile")
    Call<User> updateUserProfile(@Body User user);

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

    @GET("favorites")
    Call<List<Exercise>> getUserFavorites();

    @POST("favorites/toggle")
    Call<ResponseBody> toggleFavorite(@Body FavoriteRequest request);

    @GET("routines")
    Call<List<Routine>> getUserRoutines();

    @GET("routines/{id}")
    Call<Routine> getRoutineById(@Path("id") int id);

    @POST("routines")
    Call<ResponseBody> createRoutine(@Body Routine routine);

    @DELETE("routines/{id}")
    Call<ResponseBody> deleteRoutine(@Path("id") int id);

    @GET("workouts/history")
    Call<List<Workout>> getWorkoutHistory();

    @GET("workouts/{id}")
    Call<Workout> getWorkoutById(@Path("id") int id);

    @POST("workouts")
    Call<ResponseBody> createWorkout(@Body Workout workout);

    @DELETE("workouts/{id}")
    Call<ResponseBody> deleteWorkout(@Path("id") int id);
}