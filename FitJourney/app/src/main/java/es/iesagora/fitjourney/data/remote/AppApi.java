package es.iesagora.fitjourney.data.remote;

import java.util.List;

import es.iesagora.fitjourney.data.model.Exercise;
import es.iesagora.fitjourney.data.model.User;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface AppApi {

    @POST("users")
    Call<AccessResponse> registerUser(@Body User user);

    @POST("login")
    Call<AccessResponse> login(@Body LoginRequest request);

    @GET("profile")
    Call<User> getUser();

    @GET("exercises")
    Call<List<Exercise>> getExercises();


}
