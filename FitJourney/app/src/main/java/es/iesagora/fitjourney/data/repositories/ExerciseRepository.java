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

    public interface ActionCallback {
        void onResult(Resource<Void> result);
    }

    public interface CreateCallback {
        void onResult(Resource<Exercise> result);
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

    public void deleteExercise(int id, ActionCallback callback) {
        callback.onResult(Resource.loading());

        api.deleteExercise(id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onResult(Resource.success(null));
                } else {
                    callback.onResult(Resource.error("Error al borrar: " + response.code()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                callback.onResult(Resource.error("Error de conexión: " + t.getMessage()));
            }
        });
    }

    public void createExercise(Exercise exercise, CreateCallback callback) {
        callback.onResult(Resource.loading());

        api.createExercise(exercise).enqueue(new Callback<Exercise>() {
            @Override
            public void onResponse(@NonNull Call<Exercise> call, @NonNull Response<Exercise> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onResult(Resource.success(response.body()));
                } else {
                    callback.onResult(Resource.error("Error al crear: " + response.code()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<Exercise> call, @NonNull Throwable t) {
                callback.onResult(Resource.error("Error de conexión: " + t.getMessage()));
            }
        });
    }

    public void updateExercise(int id, Exercise exercise, ActionCallback callback) {
        callback.onResult(Resource.loading());

        api.updateExercise(id, exercise).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onResult(Resource.success(null));
                } else {
                    callback.onResult(Resource.error("Error al editar: " + response.code()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                callback.onResult(Resource.error("Error de conexión: " + t.getMessage()));
            }
        });
    }
}