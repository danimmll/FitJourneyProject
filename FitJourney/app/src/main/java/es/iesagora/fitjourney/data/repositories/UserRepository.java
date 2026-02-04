package es.iesagora.fitjourney.data.repositories;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import es.iesagora.fitjourney.data.model.User;
import es.iesagora.fitjourney.data.remote.LoginRequest;
import es.iesagora.fitjourney.data.remote.AccessResponse;
import es.iesagora.fitjourney.data.remote.AppApi;
import es.iesagora.fitjourney.data.remote.Resource;
import es.iesagora.fitjourney.data.remote.RetrofitClient;


import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserRepository {

    private final AppApi api;
    private final Gson gson = new Gson();

    public UserRepository(Context context) {
        api = RetrofitClient.getApiService(context);
    }

    public interface LoginCallBack {
        void onResult(Resource<AccessResponse> result);
    }

    public interface RegisterCallback {
        void onResult(Resource<AccessResponse> result);
    }

    public void registerUser(User user, RegisterCallback callback) {
        callback.onResult(Resource.loading());

        api.registerUser(user).enqueue(new Callback<AccessResponse>() {
            @Override
            public void onResponse(@NonNull Call<AccessResponse> call, @NonNull Response<AccessResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onResult(Resource.success(response.body()));
                } else {
                    String errorMsg = getError(response);
                    callback.onResult(Resource.error(errorMsg));
                }
            }

            @Override
            public void onFailure(@NonNull Call<AccessResponse> call, @NonNull Throwable t) {
                callback.onResult(Resource.error("Error de conexión: " + t.getMessage()));
            }
        });
    }

    public void login(String email, String password, LoginCallBack callback) {
        callback.onResult(Resource.loading());

        LoginRequest request = new LoginRequest(email, password);

        api.login(request).enqueue(new Callback<AccessResponse>() {
            @Override
            public void onResponse(@NonNull Call<AccessResponse> call, @NonNull Response<AccessResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onResult(Resource.success(response.body()));
                } else {
                    String errorMsg = getError(response);
                    callback.onResult(Resource.error(errorMsg));
                }
            }

            @Override
            public void onFailure(@NonNull Call<AccessResponse> call, @NonNull Throwable t) {
                callback.onResult(Resource.error("Error de conexión: " + t.getMessage()));
            }
        });
    }

    public void getUser(String token, LoginCallBack callback) {
        callback.onResult(Resource.loading());

        api.getUser().enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AccessResponse result = new AccessResponse();
                    result.setUser(response.body());
                    result.setToken(token);
                    callback.onResult(Resource.success(result));
                } else {
                    String errorMsg = getError(response);
                    callback.onResult(Resource.error(errorMsg));
                }
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                callback.onResult(Resource.error("Error de conexión: " + t.getMessage()));
            }
        });
    }

    private String getError(Response<?> response) {
        try {
            if (response.errorBody() != null) {
                String json = response.errorBody().string();
                JsonObject obj = gson.fromJson(json, JsonObject.class);
                if (obj.has("message") && !obj.get("message").isJsonNull()) {
                    return obj.get("message").getAsString();
                }
            }
        } catch (Exception ignored) {

        }

        int code = response.code();
        switch (code) {
            case 400:
                return "Datos incorrectos";
            case 401:
                return "Email o contraseña incorrectos";
            case 404:
                return "Usuario no encontrado";
            case 409:
                return "Email ya registrado";
            case 422:
                return "Revisa los datos introducidos";
            case 500:
                return "Error interno del servidor";
            default:
                return "Error del servidor (" + code + ")";
        }
    }
}
