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
 * relacionadas con los entrenamientos.
 * Se comunica con la API para obtener, crear
 * y eliminar sesiones de entrenamiento.
 */
public class WorkoutRepository {

    /**
     * Mensaje utilizado para errores de conexión.
     */
    private static final String ERROR_CONEXION = "Error de conexión: ";

    /**
     * Campo "message" utilizado en respuestas del servidor.
     */
    private static final String KEY_MESSAGE = "message";

    /**
     * Campo "error" utilizado en respuestas del servidor.
     */
    private static final String KEY_ERROR = "error";

    /**
     * Instancia de la API para realizar peticiones.
     */
    private final AppApi api;

    /**
     * Instancia de Gson para procesar respuestas JSON.
     */
    private final Gson gson = new Gson();

    /**
     * Callback para obtener listas de entrenamientos.
     */
    public interface WorkoutListCallback {

        /**
         * Devuelve el resultado de la operación.
         *
         * @param result Resultado con la lista de entrenamientos.
         */
        void onResult(Resource<List<Workout>> result);
    }

    /**
     * Callback para acciones sobre entrenamientos.
     */
    public interface WorkoutActionCallback {

        /**
         * Devuelve el resultado de la operación.
         *
         * @param result Resultado de la acción realizada.
         */
        void onResult(Resource<String> result);
    }

    /**
     * Callback para obtener un entrenamiento individual.
     */
    public interface WorkoutSingleCallback {

        /**
         * Devuelve el resultado de la operación.
         *
         * @param result Resultado con el entrenamiento solicitado.
         */
        void onResult(Resource<Workout> result);
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
     * @param callback Callback con el resultado.
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

                callback.onResult(
                        Resource.error(ERROR_CONEXION + t.getMessage())
                );
            }
        });
    }

    /**
     * Obtiene un entrenamiento mediante su ID.
     *
     * @param id ID del entrenamiento.
     * @param callback Callback con el resultado.
     */
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

                callback.onResult(
                        Resource.error(ERROR_CONEXION + t.getMessage())
                );
            }
        });
    }

    /**
     * Guarda un nuevo entrenamiento.
     *
     * @param workout Datos del entrenamiento.
     * @param callback Callback con el resultado.
     */
    public void createWorkout(Workout workout,
                              WorkoutActionCallback callback) {

        callback.onResult(Resource.loading());

        api.createWorkout(workout).enqueue(new Callback<>() {

            @Override
            public void onResponse(@NonNull Call<ResponseBody> call,
                                   @NonNull Response<ResponseBody> response) {

                if (response.isSuccessful()) {

                    callback.onResult(
                            Resource.success("Entrenamiento guardado con éxito")
                    );

                } else {
                    callback.onResult(Resource.error(getError(response)));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call,
                                  @NonNull Throwable t) {

                callback.onResult(
                        Resource.error(ERROR_CONEXION + t.getMessage())
                );
            }
        });
    }

    /**
     * Elimina un entrenamiento existente.
     *
     * @param idWorkout ID del entrenamiento.
     * @param callback Callback con el resultado.
     */
    public void deleteWorkout(int idWorkout,
                              WorkoutActionCallback callback) {

        callback.onResult(Resource.loading());

        api.deleteWorkout(idWorkout).enqueue(new Callback<>() {

            @Override
            public void onResponse(@NonNull Call<ResponseBody> call,
                                   @NonNull Response<ResponseBody> response) {

                if (response.isSuccessful()) {

                    callback.onResult(
                            Resource.success("Entrenamiento eliminado")
                    );

                } else {
                    callback.onResult(Resource.error(getError(response)));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call,
                                  @NonNull Throwable t) {

                callback.onResult(
                        Resource.error(ERROR_CONEXION + t.getMessage())
                );
            }
        });
    }

    /**
     * Obtiene y procesa los errores enviados por el servidor.
     *
     * @param response Respuesta HTTP recibida.
     * @return Mensaje de error procesado.
     */
    private String getError(Response<?> response) {

        try {

            if (response.errorBody() != null) {

                String json = response.errorBody().string();

                JsonObject obj = gson.fromJson(json, JsonObject.class);

                if (obj.has(KEY_MESSAGE)
                        && !obj.get(KEY_MESSAGE).isJsonNull()) {

                    return obj.get(KEY_MESSAGE).getAsString();

                } else if (obj.has(KEY_ERROR)
                        && !obj.get(KEY_ERROR).isJsonNull()) {

                    return obj.get(KEY_ERROR).getAsString();
                }
            }

        } catch (Exception ignored) {
            // EXCEPCIÓN IGNORADA
        }

        return "Error del servidor (" + response.code() + ")";
    }
}