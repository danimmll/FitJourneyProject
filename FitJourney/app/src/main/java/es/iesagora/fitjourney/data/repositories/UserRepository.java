package es.iesagora.fitjourney.data.repositories;

import androidx.annotation.NonNull;

import com.google.gson.Gson;

import es.iesagora.fitjourney.data.model.User;
import es.iesagora.fitjourney.data.remote.LoginRequest;
import es.iesagora.fitjourney.data.remote.LoginResponse;
import es.iesagora.fitjourney.data.remote.AppApi;
import es.iesagora.fitjourney.data.remote.Resource;
import es.iesagora.fitjourney.data.remote.RetrofitClient;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserRepository {

    private final AppApi api;
    private final Gson gson = new Gson();

    public UserRepository() {
        api = RetrofitClient.getApiService();
    }

    public interface LoginCallBack {
        void onResult(Resource<LoginResponse> result);
    }

    public interface RegisterCallback {
        void onResult(Resource<LoginResponse> result);
    }

    public void registerUser(User user, RegisterCallback callback) {
        callback.onResult(Resource.loading());

        api.registerUser(user).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onResult(Resource.success(response.body()));
                } else {
                    String errorMsg = parseError(response);
                    callback.onResult(Resource.error(errorMsg));
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                callback.onResult(Resource.error("Error de conexión: " + t.getMessage()));
            }
        });
    }

    public void login(String email, String password, LoginCallBack callback) {
        callback.onResult(Resource.loading());

        LoginRequest request = new LoginRequest(email, password);

        api.login(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onResult(Resource.success(response.body()));
                } else {
                    String errorMsg = parseError(response);
                    callback.onResult(Resource.error(errorMsg));
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

        api.getUser("Bearer " + token).enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse result = new LoginResponse();
                    result.setUser(response.body());
                    result.setToken(token);
                    callback.onResult(Resource.success(result));
                } else {
                    String errorMsg = parseError(response);
                    callback.onResult(Resource.error(errorMsg));
                }
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                callback.onResult(Resource.error("Error de conexión: " + t.getMessage()));
            }
        });
    }

    private String parseError(Response<?> response) {
        if (response.errorBody() != null) {
            try {
                String errorJson = response.errorBody().string();
                ErrorResponse error = gson.fromJson(errorJson, ErrorResponse.class);
                if (error != null && error.getMessage() != null) {
                    return error.getMessage();
                } else {
                    return "Error desconocido: " + response.code();
                }
            } catch (IOException e) {
                return "Error desconocido: " + response.code();
            }
        } else {
            return "Error desconocido: " + response.code();
        }
    }

    private static class ErrorResponse {
        private String message;
        public String getMessage() { return message; }
    }
}
