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

/**
 * Repositorio encargado de gestionar las operaciones
 * relacionadas con los ejercicios.
 * Actúa como intermediario entre la API y la capa de
 * presentación, manejando llamadas de red y devolviendo
 * resultados encapsulados en Resource.
 */
public class ExerciseRepository {

    /**
     * Instancia de la API utilizada para realizar peticiones.
     */
    private final AppApi api;

    /**
     * Callback para obtener listas de ejercicios.
     */
    public interface ExerciseCallBack {
        void onResult(Resource<List<Exercise>> result);
    }

    /**
     * Callback para obtener un único ejercicio.
     */
    public interface SingleExerciseCallback {
        void onResult(Resource<Exercise> result);
    }

    /**
     * Callback para operaciones sin retorno de datos.
     * (eliminar o actualizar ejercicios)
     */
    public interface ActionCallback {
        void onResult(Resource<Void> result);
    }

    /**
     * Callback para la creación de ejercicios.
     */
    public interface CreateCallback {
        void onResult(Resource<Exercise> result);
    }

    /**
     * Constructor del repositorio.
     *
     * @param context Contexto de la aplicación.
     */
    public ExerciseRepository(Context context) {
        this.api = RetrofitClient.getInstance(context);
    }

    /**
     * Obtiene la lista de ejercicios disponibles.
     *
     * @param callBack Callback con el resultado.
     */
    public void getExercises(ExerciseCallBack callBack) {

        callBack.onResult(Resource.loading());

        api.getExercises().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<Exercise>> call,
                                   @NonNull Response<List<Exercise>> response) {

                if (response.isSuccessful() && response.body() != null) {
                    callBack.onResult(Resource.success(response.body()));
                } else {
                    callBack.onResult(Resource.error("Error del servidor: " + response.code()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Exercise>> call,
                                  @NonNull Throwable t) {

                callBack.onResult(Resource.error("Error de conexión: " + t.getMessage()));
            }
        });
    }

    /**
     * Obtiene un ejercicio por su ID.
     *
     * @param id ID del ejercicio.
     * @param callback Callback con el resultado.
     */
    public void getExerciseById(int id, SingleExerciseCallback callback) {

        callback.onResult(Resource.loading());

        api.getExerciseById(id).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Exercise> call,
                                   @NonNull Response<Exercise> response) {

                if (response.isSuccessful() && response.body() != null) {
                    callback.onResult(Resource.success(response.body()));
                } else {
                    callback.onResult(Resource.error("No se pudo cargar el detalle del ejercicio"));
                }
            }

            @Override
            public void onFailure(@NonNull Call<Exercise> call,
                                  @NonNull Throwable t) {

                callback.onResult(Resource.error("Error de conexión: " + t.getMessage()));
            }
        });
    }

    /**
     * Elimina un ejercicio existente.
     *
     * @param id ID del ejercicio.
     * @param callback Callback con el resultado.
     */
    public void deleteExercise(int id, ActionCallback callback) {

        callback.onResult(Resource.loading());

        api.deleteExercise(id).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Void> call,
                                   @NonNull Response<Void> response) {

                if (response.isSuccessful()) {
                    callback.onResult(Resource.success(null));
                } else {
                    callback.onResult(Resource.error("Error al borrar: " + response.code()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call,
                                  @NonNull Throwable t) {

                callback.onResult(Resource.error("Error de conexión: " + t.getMessage()));
            }
        });
    }

    /**
     * Crea un nuevo ejercicio.
     *
     * @param exercise Ejercicio a crear.
     * @param callback Callback con el resultado.
     */
    public void createExercise(Exercise exercise, CreateCallback callback) {

        callback.onResult(Resource.loading());

        api.createExercise(exercise).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Exercise> call,
                                   @NonNull Response<Exercise> response) {

                if (response.isSuccessful() && response.body() != null) {
                    callback.onResult(Resource.success(response.body()));
                } else {
                    callback.onResult(Resource.error("Error al crear: " + response.code()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<Exercise> call,
                                  @NonNull Throwable t) {

                callback.onResult(Resource.error("Error de conexión: " + t.getMessage()));
            }
        });
    }

    /**
     * Actualiza un ejercicio existente.
     *
     * @param id ID del ejercicio.
     * @param exercise Datos actualizados.
     * @param callback Callback con el resultado.
     */
    public void updateExercise(int id, Exercise exercise, ActionCallback callback) {

        callback.onResult(Resource.loading());

        api.updateExercise(id, exercise).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Void> call,
                                   @NonNull Response<Void> response) {

                if (response.isSuccessful()) {
                    callback.onResult(Resource.success(null));
                } else {
                    callback.onResult(Resource.error("Error al editar: " + response.code()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call,
                                  @NonNull Throwable t) {

                callback.onResult(Resource.error("Error de conexión: " + t.getMessage()));
            }
        });
    }
}