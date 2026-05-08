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

    /**
     * Instancia de la API utilizada para realizar peticiones.
     */
    private final AppApi api;

    /**
     * Gson utilizado para parsear mensajes de error del servidor.
     */
    private final Gson gson = new Gson();

    /**
     * Callback para obtener listas de entrenamientos.
     */
    public interface WorkoutListCallback {
        void onResult(Resource<List<Workout>> result);
    }

    /**
     * Callback para acciones sobre entrenamientos
     * (crear o eliminar).
     */
    public interface WorkoutActionCallback {
        void onResult(Resource<String> result);
    }

    /**
     * Constructor del repositorio.
     *
     * @param context Contexto de la aplicación.
     */
    public WorkoutRepository(Context context) {
        api = RetrofitClient.getInstance(context);
    }

    /**
     * Obtiene el historial de entrenamientos del usuario.
     *
     * @param callback Callback con la lista de entrenamientos.
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

                callback.onResult(Resource.error("Error de conexión: " + t.getMessage()));
            }
        });
    }

    /**
     * Crea un nuevo entrenamiento.
     *
     * @param workout Datos del entrenamiento.
     * @param callback Callback con el resultado.
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

                callback.onResult(Resource.error("Error de conexión: " + t.getMessage()));
            }
        });
    }

    /**
     * Elimina un entrenamiento existente.
     *
     * @param idWorkout ID del entrenamiento.
     * @param callback Callback con el resultado.
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

                callback.onResult(Resource.error("Error de conexión: " + t.getMessage()));
            }
        });
    }

    /**
     * Extrae el mensaje de error del servidor.
     *
     * @param response Respuesta HTTP.
     * @return Mensaje de error formateado.
     */
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
        } catch (Exception ignored) {}

        return "Error del servidor (" + response.code() + ")";
    }
}