package es.iesagora.fitjourney.data.repositories;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.List;

import es.iesagora.fitjourney.data.model.Routine;
import es.iesagora.fitjourney.data.remote.AppApi;
import es.iesagora.fitjourney.data.remote.Resource;
import es.iesagora.fitjourney.data.remote.RetrofitClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RoutineRepository {

    private final AppApi api;

    public RoutineRepository(Context context) {
        api = RetrofitClient.getInstance(context);
    }

    public interface RoutineListCallback {
        void onResult(Resource<List<Routine>> result);
    }

    public interface ActionCallback {
        void onResult(Resource<String> result);
    }

    public void getRoutines(RoutineListCallback callback) {
        callback.onResult(Resource.loading());

        api.getUserRoutines().enqueue(new Callback<List<Routine>>() {
            @Override
            public void onResponse(@NonNull Call<List<Routine>> call, @NonNull Response<List<Routine>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onResult(Resource.success(response.body()));
                } else {
                    callback.onResult(Resource.error("Error al obtener rutinas"));
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Routine>> call, @NonNull Throwable t) {
                callback.onResult(Resource.error("Error de conexión: " + t.getMessage()));
            }
        });
    }

    public void createRoutine(Routine routine, ActionCallback callback) {
        callback.onResult(Resource.loading());

        api.createRoutine(routine).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    callback.onResult(Resource.success("Rutina creada con éxito"));
                } else {
                    callback.onResult(Resource.error("Error al crear rutina"));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                callback.onResult(Resource.error("Error de conexión: " + t.getMessage()));
            }
        });
    }

    public void deleteRoutine(int id, ActionCallback callback) {
        callback.onResult(Resource.loading());

        api.deleteRoutine(id).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    callback.onResult(Resource.success("Rutina eliminada correctamente"));
                } else {
                    callback.onResult(Resource.error("Error al eliminar la rutina"));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                callback.onResult(Resource.error("Error de conexión: " + t.getMessage()));
            }
        });
    }
}