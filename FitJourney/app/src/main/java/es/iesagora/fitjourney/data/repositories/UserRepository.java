package es.iesagora.fitjourney.data.repositories;

import es.iesagora.fitjourney.data.model.User;
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
        void onResult(Resource<User> result);
    }

    public interface RegisterCallback {
        void onResult(Resource<Void> result);
    }

    public void getUser(String userId, LoginCallBack callback) {

        callback.onResult(Resource.loading());

        api.getUser(userId).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onResult(Resource.success(response.body()));
                } else {
                    // Si es 404 u otro error
                    callback.onResult(Resource.error("Usuario no encontrado o error en servidor"));
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                callback.onResult(Resource.error("Error de conexión: " + t.getMessage()));
            }
        });
    }

    public void registerUser(User user, RegisterCallback callback) {

        callback.onResult(Resource.loading());

        api.registerUser(user).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onResult(Resource.success(null));
                } else {
                    callback.onResult(Resource.error("Error al registrar: " + response.code()));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onResult(Resource.error("Error de conexión: " + t.getMessage()));
            }
        });
    }



}
