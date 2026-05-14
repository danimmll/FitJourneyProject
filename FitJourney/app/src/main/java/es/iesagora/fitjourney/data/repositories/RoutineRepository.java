package es.iesagora.fitjourney.data.repositories;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.List;

import es.iesagora.fitjourney.data.model.Routine;
import es.iesagora.fitjourney.data.remote.AppApi;
import es.iesagora.fitjourney.data.remote.Resource;
import es.iesagora.fitjourney.data.remote.RetrofitClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repositorio encargado de gestionar las operaciones
 * relacionadas con las rutinas de entrenamiento.
 * Actúa como intermediario entre la API y la capa de
 * presentación, encapsulando las llamadas de red y
 * devolviendo los resultados mediante Resource.
 */
public class RoutineRepository {

    private final AppApi api;

    private static final String ERROR_CONEXION = "Error de conexión: ";
    private static final String ERROR_CREAR = "Error al crear rutina";
    private static final String ERROR_CARGAR = "Error al cargar rutinas";
    private static final String ERROR_ELIMINAR = "Error al eliminar la rutina";
    private static final String ERROR_DETALLE = "Error al cargar la rutina";
    private static final String ERROR_ACTUALIZAR = "Error al actualizar la rutina";
    private static final String ERROR_FIELD = "error";
    private final Gson gson = new Gson();

    /**
     * Callback para obtener listas de rutinas.
     */
    public interface RoutineListCallback {
        void onResult(Resource<List<Routine>> result);
    }

    /**
     * Callback para operaciones sin retorno estructurado.
     * (crear, actualizar o eliminar rutinas)
     */
    public interface ActionCallback {
        void onResult(Resource<String> result);
    }

    /**
     * Callback para obtener una rutina individual.
     */
    public interface SingleRoutineCallback {
        void onResult(Resource<Routine> result);
    }

    /**
     * Constructor del repositorio.
     *
     * @param context Contexto de la aplicación.
     */
    public RoutineRepository(Context context) {
        api = RetrofitClient.getInstance(context);
    }

    /**
     * Obtiene la lista de rutinas del usuario.
     *
     * @param callback Callback con el resultado.
     */
    public void getRoutines(RoutineListCallback callback) {
        callback.onResult(Resource.loading());

        api.getUserRoutines().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<Routine>> call, @NonNull Response<List<Routine>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onResult(Resource.success(response.body()));
                } else {
                    callback.onResult(Resource.error(getError(response, ERROR_CARGAR)));
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Routine>> call, @NonNull Throwable t) {
                callback.onResult(Resource.error(ERROR_CONEXION + t.getMessage()));
            }
        });
    }

    /**
     * Crea una nueva rutina.
     *
     * @param routine Datos de la rutina.
     * @param callback Callback con el resultado.
     */
    public void createRoutine(Routine routine, ActionCallback callback) {
        callback.onResult(Resource.loading());

        api.createRoutine(routine).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    callback.onResult(Resource.success("Rutina creada con éxito"));
                } else {
                    callback.onResult(Resource.error(getError(response, ERROR_CREAR)));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                callback.onResult(Resource.error(ERROR_CONEXION + t.getMessage()));
            }
        });
    }

    /**
     * Actualiza una rutina existente.
     *
     * @param id ID de la rutina a modificar.
     * @param routine Objeto Routine con los datos actualizados.
     * @param callback Callback con el resultado de la operación.
     */
    public void updateRoutine(int id, Routine routine, ActionCallback callback) {
        callback.onResult(Resource.loading());

        api.updateRoutine(id, routine).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    callback.onResult(Resource.success("Rutina actualizada con éxito"));
                } else {
                    callback.onResult(Resource.error(getError(response, ERROR_ACTUALIZAR)));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                callback.onResult(Resource.error(ERROR_CONEXION + t.getMessage()));
            }
        });
    }

    /**
     * Elimina una rutina existente.
     *
     * @param id ID de la rutina.
     * @param callback Callback con el resultado.
     */
    public void deleteRoutine(int id, ActionCallback callback) {
        callback.onResult(Resource.loading());

        api.deleteRoutine(id).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    callback.onResult(Resource.success("Rutina eliminada correctamente"));
                } else {
                    callback.onResult(Resource.error(getError(response, ERROR_ELIMINAR)));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                callback.onResult(Resource.error(ERROR_CONEXION + t.getMessage()));
            }
        });
    }

    /**
     * Obtiene una rutina por su ID.
     *
     * @param id ID de la rutina.
     * @param callback Callback con el resultado.
     */
    public void getRoutineById(int id, SingleRoutineCallback callback) {
        callback.onResult(Resource.loading());

        api.getRoutineById(id).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Routine> call, @NonNull Response<Routine> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onResult(Resource.success(response.body()));
                } else {
                    callback.onResult(Resource.error(getError(response, ERROR_DETALLE)));
                }
            }

            @Override
            public void onFailure(@NonNull Call<Routine> call, @NonNull Throwable t) {
                callback.onResult(Resource.error(ERROR_CONEXION + t.getMessage()));
            }
        });
    }

    private <T> String getError(Response<T> response, String fallbackMessage) {
        if (response.errorBody() == null) return fallbackMessage;

        try {
            String raw = response.errorBody().string();
            JsonObject jsonObject = gson.fromJson(raw, JsonObject.class);
            if (jsonObject != null && jsonObject.has(ERROR_FIELD)) {
                return jsonObject.get(ERROR_FIELD).getAsString();
            }
            return fallbackMessage;
        } catch (IOException e) {
            return fallbackMessage;
        }
    }
}