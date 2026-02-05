package es.iesagora.fitjourney.data.repositories;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.List;

import es.iesagora.fitjourney.data.model.Exercise;
import es.iesagora.fitjourney.data.remote.AppApi;
import es.iesagora.fitjourney.data.remote.Resource;
import es.iesagora.fitjourney.data.remote.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExerciseRepository {
    private final AppApi api;

    public ExerciseRepository(Context context) {
        this.api = RetrofitClient.getInstance(context);
    }

    public interface ExerciseCallBack {
        void onResult(Resource<List<Exercise>> result);
    }

    public void getExercises(ExerciseCallBack callBack) {

        callBack.onResult(Resource.loading());

        api.getExercises().enqueue(new Callback<List<Exercise>>() {
            @Override
            public void onResponse(@NonNull Call<List<Exercise>> call, @NonNull Response<List<Exercise>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callBack.onResult(Resource.success(response.body()));
                } else {
                    callBack.onResult(Resource.error("Error del servidor: " + response.message()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Exercise>> call, @NonNull Throwable t) {
                callBack.onResult(Resource.error("Error de conexión: " + t.getMessage()));
            }
        });
    }
}