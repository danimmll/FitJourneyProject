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
 * Actúa como intermediario entre la API y la capa de presentación.
 */
public class RoutineRepository {

    /**
     * Instancia de la API para realizar peticiones.
     */
    private final AppApi api;

    /**
     * Mensajes de error utilizados en el repositorio.
     */
    private static final String ERROR_CONEXION = "Error de conexión: ";
    private static final String ERROR_CREAR = "Error al crear rutina";
    private static final String ERROR_CARGAR = "Error al cargar rutinas";
    private static final String ERROR_ELIMINAR = "Error al eliminar la rutina";
    private static final String ERROR_DETALLE = "Error al cargar la rutina";
    private static final String ERROR_ACTUALIZAR = "Error al actualizar la rutina";

    /**
     * Campo utilizado para obtener errores del backend.
     */
    private static final String ERROR_FIELD = "error";

    /**
     * Instancia de Gson para procesar respuestas JSON.
     */
    private final Gson gson = new Gson();

    /**
     * Callback para obtener listas de rutinas.
     */
    public interface RoutineListCallback {

        /**
         * Devuelve el resultado de la operación.
         *
         * @param result Resultado con la lista de rutinas.
         */
        void onResult(Resource<List<Routine>> result);
    }

    /**
     * Callback para operaciones sin retorno de objetos.
     */
    public interface ActionCallback {

        /**
         * Devuelve el resultado de la operación.
         *
         * @param result Resultado de la acción realizada.
         */
        void onResult(Resource<String> result);
    }

    /**
     * Callback para obtener una rutina individual.
     */
    public interface SingleRoutineCallback {

        /**
         * Devuelve el resultado de la operación.
         *
         * @param result Resultado con la rutina solicitada.
         */
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
     * Obtiene la lista de rutinas disponibles.
     *
     * @param callback Callback con el resultado.
     */
    public void getRoutines(RoutineListCallback callback) {

        callback.onResult(Resource.loading());

        api.getUserRoutines().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<Routine>> call,
                                   @NonNull Response<List<Routine>> response) {

                if (response.isSuccessful() && response.body() != null) {
                    callback.onResult(Resource.success(response.body()));
                } else {
                    callback.onResult(Resource.error(getError(response, ERROR_CARGAR)));
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Routine>> call,
                                  @NonNull Throwable t) {

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
            public void onResponse(@NonNull Call<ResponseBody> call,
                                   @NonNull Response<ResponseBody> response) {

                if (response.isSuccessful()) {
                    callback.onResult(Resource.success("Rutina creada con éxito"));
                } else {
                    callback.onResult(Resource.error(getError(response, ERROR_CREAR)));
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
     * Actualiza una rutina existente.
     *
     * @param id ID de la rutina.
     * @param routine Datos actualizados.
     * @param callback Callback con el resultado.
     */
    public void updateRoutine(int id, Routine routine, ActionCallback callback) {

        callback.onResult(Resource.loading());

        api.updateRoutine(id, routine).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call,
                                   @NonNull Response<ResponseBody> response) {

                if (response.isSuccessful()) {
                    callback.onResult(Resource.success("Rutina actualizada con éxito"));
                } else {
                    callback.onResult(Resource.error(getError(response, ERROR_ACTUALIZAR)));
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
     * Elimina una rutina existente.
     *
     * @param id ID de la rutina.
     * @param callback Callback con el resultado.
     */
    public void deleteRoutine(int id, ActionCallback callback) {

        callback.onResult(Resource.loading());

        api.deleteRoutine(id).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call,
                                   @NonNull Response<ResponseBody> response) {

                if (response.isSuccessful()) {
                    callback.onResult(Resource.success("Rutina eliminada correctamente"));
                } else {
                    callback.onResult(Resource.error(getError(response, ERROR_ELIMINAR)));
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
     * Obtiene una rutina mediante su ID.
     *
     * @param id ID de la rutina.
     * @param callback Callback con el resultado.
     */
    public void getRoutineById(int id, SingleRoutineCallback callback) {

        callback.onResult(Resource.loading());

        api.getRoutineById(id).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Routine> call,
                                   @NonNull Response<Routine> response) {

                if (response.isSuccessful() && response.body() != null) {
                    callback.onResult(Resource.success(response.body()));
                } else {
                    callback.onResult(Resource.error(getError(response, ERROR_DETALLE)));
                }
            }

            @Override
            public void onFailure(@NonNull Call<Routine> call,
                                  @NonNull Throwable t) {

                callback.onResult(Resource.error(ERROR_CONEXION + t.getMessage()));
            }
        });
    }

    /**
     * Obtiene el mensaje de error enviado por el servidor.
     * Si no existe, devuelve un mensaje por defecto.
     *
     * @param response Respuesta HTTP recibida.
     * @param fallbackMessage Mensaje por defecto.
     * @return Mensaje de error procesado.
     */
    private <T> String getError(Response<T> response, String fallbackMessage) {

        if (response.errorBody() == null) {
            return fallbackMessage;
        }

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