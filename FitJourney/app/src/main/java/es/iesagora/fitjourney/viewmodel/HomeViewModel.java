package es.iesagora.fitjourney.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import es.iesagora.fitjourney.data.model.User;
import es.iesagora.fitjourney.data.remote.AppApi;
import es.iesagora.fitjourney.data.remote.RetrofitClient;
import es.iesagora.fitjourney.data.remote.Resource;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeViewModel extends ViewModel {

    private final MutableLiveData<Resource<User>> userLiveData = new MutableLiveData<>();
    private final AppApi api;

    public HomeViewModel() {
        api = RetrofitClient.getApiService();
    }

    public LiveData<Resource<User>> getUserLiveData() {
        return userLiveData;
    }

    public void fetchUser(String token) {
        userLiveData.setValue(Resource.loading());
        String authHeader = "Bearer " + token;

        api.getUser(authHeader).enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    userLiveData.setValue(Resource.success(response.body()));
                } else {
                    userLiveData.setValue(Resource.error("Error al cargar perfil"));
                }
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                userLiveData.setValue(Resource.error("Error de conexión: " + t.getMessage()));
            }
        });
    }
}
