package es.iesagora.fitjourney.data.repositories;

import androidx.annotation.NonNull;

import es.iesagora.fitjourney.data.model.User;
import es.iesagora.fitjourney.data.remote.LoginRequest;
import es.iesagora.fitjourney.data.remote.LoginResponse;
import es.iesagora.fitjourney.data.remote.AppApi;
import es.iesagora.fitjourney.data.remote.Resource;
import es.iesagora.fitjourney.data.remote.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserRepository {

    private final AppApi api;

    public UserRepository() {
        api = RetrofitClient.getApiService();
    }

    public interface LoginCallBack {
        void onResult(Resource<LoginResponse> result);
    }

    public interface RegisterCallback {
        void onResult(Resource<Void> result);
    }

    public void registerUser(User user, RegisterCallback callback) {
        callback.onResult(Resource.loading());

        api.registerUser(user).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onResult(Resource.success(null));
                } else {
                    callback.onResult(Resource.error("Error al registrar: " + response.code()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                callback.onResult(Resource.error("Error de conexión: " + t.getMessage()));
            }
        });
    }

    public void login(String email, String password, LoginCallBack callback) {
        callback.onResult(Resource.loading());

        LoginRequest request = new LoginRequest(email, password);

        api.login(request).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onResult(Resource.success(response.body()));
                } else {
                    callback.onResult(Resource.error("Error en login: " + response.code()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                callback.onResult(Resource.error("Error de conexión: " + t.getMessage()));
            }
        });
    }

    public void getUser(String token, LoginCallBack callback) {
        callback.onResult(Resource.loading());

        api.getUser("Bearer " + token).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse result = new LoginResponse();
                    result.setUser(response.body());
                    result.setToken(token);
                    callback.onResult(Resource.success(result));
                } else {
                    callback.onResult(Resource.error("Usuario no encontrado o error en servidor"));
                }
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                callback.onResult(Resource.error("Error de conexión: " + t.getMessage()));
            }
        });
    }
}
