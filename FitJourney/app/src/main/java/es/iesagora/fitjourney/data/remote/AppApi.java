package es.iesagora.fitjourney.data.remote;

import es.iesagora.fitjourney.data.model.User;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface AppApi {


    @GET("users/{id}")
    Call<User> getUser(@Path("id") String userId);

    @POST("users")
    Call<Void> registerUser(@Body User user);


}
