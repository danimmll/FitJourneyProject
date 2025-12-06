package es.iesagora.fitjourney.data.remote;

import es.iesagora.fitjourney.data.model.User;
import es.iesagora.fitjourney.data.remote.LoginRequest;
import es.iesagora.fitjourney.data.remote.LoginResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface AppApi {

    @POST("users")
    Call<LoginResponse> registerUser(@Body User user);

    @POST("login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @GET("profile")
    Call<User> getUser(@Header("Authorization") String token);
}
