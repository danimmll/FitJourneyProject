package es.iesagora.fitjourney.data.repositories;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.List;

import es.iesagora.fitjourney.data.model.Workout;
import es.iesagora.fitjourney.data.remote.AppApi;
import es.iesagora.fitjourney.data.remote.Resource;
import es.iesagora.fitjourney.data.remote.RetrofitClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WorkoutRepository {

    private final AppApi api;
    private final Gson gson = new Gson();

    public WorkoutRepository(Context context) {
        api = RetrofitClient.getInstance(context);
    }

    public interface WorkoutListCallback {
        void onResult(Resource<List<Workout>> result);
    }

    public interface WorkoutActionCallback {
        void onResult(Resource<String> result);
    }

    public void getWorkoutHistory(WorkoutListCallback callback) {
        callback.onResult(Resource.loading());

        api.getWorkoutHistory().enqueue(new Callback<List<Workout>>() {
            @Override
            public void onResponse(@NonNull Call<List<Workout>> call, @NonNull Response<List<Workout>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onResult(Resource.success(response.body()));
                } else {
                    String errorMsg = getError(response);
                    callback.onResult(Resource.error(errorMsg));
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Workout>> call, @NonNull Throwable t) {
                callback.onResult(Resource.error("Error de conexión: " + t.getMessage()));
            }
        });
    }

    public void createWorkout(Workout workout, WorkoutActionCallback callback) {
        callback.onResult(Resource.loading());

        api.createWorkout(workout).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    callback.onResult(Resource.success("Entrenamiento guardado con éxito"));
                } else {
                    String errorMsg = getError(response);
                    callback.onResult(Resource.error(errorMsg));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                callback.onResult(Resource.error("Error de conexión: " + t.getMessage()));
            }
        });
    }

    public void deleteWorkout(int idWorkout, WorkoutActionCallback callback) {
        callback.onResult(Resource.loading());

        api.deleteWorkout(idWorkout).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    callback.onResult(Resource.success("Entrenamiento eliminado"));
                } else {
                    String errorMsg = getError(response);
                    callback.onResult(Resource.error(errorMsg));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
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
                } else if (obj.has("error") && !obj.get("error").isJsonNull()) {
                    return obj.get("error").getAsString();
                }
            }
        } catch (Exception ignored) {
        }
        return "Error del servidor (" + response.code() + ")";
    }
}