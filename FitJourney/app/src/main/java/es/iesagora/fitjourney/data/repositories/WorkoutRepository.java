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

/**
 * Repositorio encargado de gestionar las operaciones
 * relacionadas con los entrenamientos (workouts).
 * Se encarga de comunicarse con la API para obtener,
 * crear y eliminar entrenamientos, encapsulando las
 * respuestas en Resource para manejar estados de UI.
 */
public class WorkoutRepository {

    private static final String ERROR_CONEXION = "Error de conexión: ";
    private static final String KEY_MESSAGE    = "message";
    private static final String KEY_ERROR      = "error";

    private final AppApi api;
    private final Gson gson = new Gson();

    public interface WorkoutListCallback {
        void onResult(Resource<List<Workout>> result);
    }

    public interface WorkoutActionCallback {
        void onResult(Resource<String> result);
    }

    public interface WorkoutSingleCallback {
        void onResult(Resource<Workout> result);
    }

    public WorkoutRepository(Context context) {
        api = RetrofitClient.getInstance(context);
    }

    /**
     * Obtiene el historial de entrenamientos del usuario.
     */
    public void getWorkoutHistory(WorkoutListCallback callback) {
        callback.onResult(Resource.loading());

        api.getWorkoutHistory().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<Workout>> call,
                                   @NonNull Response<List<Workout>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onResult(Resource.success(response.body()));
                } else {
                    callback.onResult(Resource.error(getError(response)));
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Workout>> call,
                                  @NonNull Throwable t) {
                callback.onResult(Resource.error(ERROR_CONEXION + t.getMessage()));
            }
        });
    }

    public void getWorkoutById(int id, WorkoutSingleCallback callback) {
        callback.onResult(Resource.loading());

        api.getWorkoutById(id).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Workout> call,
                                   @NonNull Response<Workout> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onResult(Resource.success(response.body()));
                } else {
                    callback.onResult(Resource.error(getError(response)));
                }
            }

            @Override
            public void onFailure(@NonNull Call<Workout> call,
                                  @NonNull Throwable t) {
                callback.onResult(Resource.error(ERROR_CONEXION + t.getMessage()));
            }
        });
    }

    /**
     * Crea un nuevo entrenamiento.
     */
    public void createWorkout(Workout workout, WorkoutActionCallback callback) {
        callback.onResult(Resource.loading());

        api.createWorkout(workout).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call,
                                   @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    callback.onResult(Resource.success("Entrenamiento guardado con éxito"));
                } else {
                    callback.onResult(Resource.error(getError(response)));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call,
                                  @NonNull Throwable t) {
                callback.onResult(Resource.error(ERROR_CONEXION + t.getMessage()));
            }
        });
    }

    /**
     * Elimina un entrenamiento existente.
     */
    public void deleteWorkout(int idWorkout, WorkoutActionCallback callback) {
        callback.onResult(Resource.loading());

        api.deleteWorkout(idWorkout).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call,
                                   @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    callback.onResult(Resource.success("Entrenamiento eliminado"));
                } else {
                    callback.onResult(Resource.error(getError(response)));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call,
                                  @NonNull Throwable t) {
                callback.onResult(Resource.error(ERROR_CONEXION + t.getMessage()));
            }
        });
    }

    private String getError(Response<?> response) {
        try {
            if (response.errorBody() != null) {
                String json = response.errorBody().string();
                JsonObject obj = gson.fromJson(json, JsonObject.class);
                if (obj.has(KEY_MESSAGE) && !obj.get(KEY_MESSAGE).isJsonNull()) {
                    return obj.get(KEY_MESSAGE).getAsString();
                } else if (obj.has(KEY_ERROR) && !obj.get(KEY_ERROR).isJsonNull()) {
                    return obj.get(KEY_ERROR).getAsString();
                }
            }
        } catch (Exception ignored) {}
        return "Error del servidor (" + response.code() + ")";
    }
}