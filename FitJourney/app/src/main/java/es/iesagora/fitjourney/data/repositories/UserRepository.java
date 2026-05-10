package es.iesagora.fitjourney.data.repositories;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.List;

import es.iesagora.fitjourney.data.model.User;
import es.iesagora.fitjourney.data.remote.LoginRequest;
import es.iesagora.fitjourney.data.remote.AccessResponse;
import es.iesagora.fitjourney.data.remote.AppApi;
import es.iesagora.fitjourney.data.remote.Resource;
import es.iesagora.fitjourney.data.remote.RetrofitClient;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repositorio encargado de gestionar todas las operaciones
 * relacionadas con el usuario.
 * Incluye autenticación, registro, reactivación de cuenta,
 * eliminación, obtención de perfil, actualización de datos
 * y obtención de la lista de todos los usuarios.
 * Se comunica con la API y devuelve resultados encapsulados
 * en Resource para manejar estados de carga, éxito y error.
 */
public class UserRepository {

    /*CONSTANTES*/
    private static final String ERROR_CONEXION = "Error de conexión: ";
    private static final String MESSAGE_STRING = "message";
    private static final String ERROR_STRING = "error";

    /**
     * Instancia de la API para realizar peticiones.
     */
    private final AppApi api;

    /**
     * Gson utilizado para parsear errores del servidor.
     */
    private final Gson gson = new Gson();

    /**
     * Callback para login de usuario.
     */
    public interface LoginCallBack {
        void onResult(Resource<AccessResponse> result);
    }

    /**
     * Callback para registro de usuario.
     */
    public interface RegisterCallback {
        void onResult(Resource<AccessResponse> result);
    }

    /**
     * Callback para eliminación de cuenta.
     */
    public interface DeleteCallback {
        void onResult(Resource<String> result);
    }

    /**
     * Callback para obtener o actualizar perfil.
     */
    public interface ProfileCallback {
        void onResult(Resource<User> result);
    }

    /**
     * Callback para obtener la lista de todos los usuarios.
     */
    public interface UserListCallback {
        void onResult(Resource<List<User>> result);
    }

    /**
     * Callback para obtener las acciones de admin.
     */
    public interface AdminActionCallback {
        void onResult(Resource<String> result);
    }

    /**
     * Constructor del repositorio de usuario.
     *
     * @param context Contexto de la aplicación.
     */
    public UserRepository(Context context) {
        api = RetrofitClient.getInstance(context);
    }

    /**
     * Registra un nuevo usuario en la aplicación.
     *
     * @param user Datos del usuario.
     * @param callback Callback con el resultado.
     */
    public void registerUser(User user, RegisterCallback callback) {

        callback.onResult(Resource.loading());

        api.registerUser(user).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<AccessResponse> call,
                                   @NonNull Response<AccessResponse> response) {

                if (response.isSuccessful() && response.body() != null) {
                    callback.onResult(Resource.success(response.body()));

                } else if (response.code() == 409) {
                    callback.onResult(Resource.error("PROMPT_REACTIVATION"));

                } else {
                    callback.onResult(Resource.error(getError(response)));
                }
            }

            @Override
            public void onFailure(@NonNull Call<AccessResponse> call,
                                  @NonNull Throwable t) {

                callback.onResult(Resource.error(ERROR_CONEXION + t.getMessage()));
            }
        });
    }

    /**
     * Inicia sesión del usuario.
     */
    public void login(String email, String password, LoginCallBack callback) {

        callback.onResult(Resource.loading());

        LoginRequest request = new LoginRequest(email, password);

        api.loginUser(request).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<AccessResponse> call,
                                   @NonNull Response<AccessResponse> response) {

                if (response.isSuccessful() && response.body() != null) {
                    callback.onResult(Resource.success(response.body()));
                } else {
                    callback.onResult(Resource.error(getError(response)));
                }
            }

            @Override
            public void onFailure(@NonNull Call<AccessResponse> call,
                                  @NonNull Throwable t) {

                callback.onResult(Resource.error(ERROR_CONEXION + t.getMessage()));
            }
        });
    }

    /**
     * Reactiva una cuenta de usuario.
     */
    public void reactivateAccount(String email, String password, LoginCallBack callback) {

        callback.onResult(Resource.loading());

        LoginRequest request = new LoginRequest(email, password);

        api.reactivateAccount(request).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<AccessResponse> call,
                                   @NonNull Response<AccessResponse> response) {

                if (response.isSuccessful() && response.body() != null) {
                    callback.onResult(Resource.success(response.body()));
                } else {
                    callback.onResult(Resource.error(getError(response)));
                }
            }

            @Override
            public void onFailure(@NonNull Call<AccessResponse> call,
                                  @NonNull Throwable t) {

                callback.onResult(Resource.error(ERROR_CONEXION + t.getMessage()));
            }
        });
    }

    /**
     * Elimina físicamente una cuenta de usuario.
     */
    public void deleteAccountPhysical(String email, String password, DeleteCallback callback) {

        callback.onResult(Resource.loading());

        LoginRequest request = new LoginRequest(email, password);

        api.deleteAccountPhysical(request).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call,
                                   @NonNull Response<ResponseBody> response) {

                if (response.isSuccessful()) {
                    callback.onResult(Resource.success("Cuenta eliminada correctamente"));
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
     * Obtiene los datos del usuario autenticado.
     */
    public void getUser(String token, LoginCallBack callback) {

        callback.onResult(Resource.loading());

        api.getUserProfile().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<User> call,
                                   @NonNull Response<User> response) {

                if (response.isSuccessful() && response.body() != null) {

                    AccessResponse result = new AccessResponse();
                    result.setUser(response.body());
                    result.setToken(token);

                    callback.onResult(Resource.success(result));

                } else {
                    callback.onResult(Resource.error(getError(response)));
                }
            }

            @Override
            public void onFailure(@NonNull Call<User> call,
                                  @NonNull Throwable t) {

                callback.onResult(Resource.error(ERROR_CONEXION + t.getMessage()));
            }
        });
    }

    /**
     * Actualiza el perfil del usuario.
     */
    public void updateUserProfile(User user, ProfileCallback callback) {

        callback.onResult(Resource.loading());

        api.updateUserProfile(user).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<User> call,
                                   @NonNull Response<User> response) {

                if (response.isSuccessful() && response.body() != null) {
                    callback.onResult(Resource.success(response.body()));
                } else {
                    callback.onResult(Resource.error(getError(response)));
                }
            }

            @Override
            public void onFailure(@NonNull Call<User> call,
                                  @NonNull Throwable t) {

                callback.onResult(Resource.error(ERROR_CONEXION + t.getMessage()));
            }
        });
    }

    /**
     * Obtiene todos los usuarios registrados (solo para administradores).
     *
     * @param callback Callback con el resultado.
     */
    public void getAllUsers(UserListCallback callback) {

        callback.onResult(Resource.loading());

        api.getAllUsers().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<User>> call,
                                   @NonNull Response<List<User>> response) {

                if (response.isSuccessful() && response.body() != null) {
                    callback.onResult(Resource.success(response.body()));
                } else {
                    callback.onResult(Resource.error(getError(response)));
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<User>> call,
                                  @NonNull Throwable t) {

                callback.onResult(Resource.error(ERROR_CONEXION + t.getMessage()));
            }
        });
    }

    /**
     * Elimina físicamente a un usuario de la base de datos (Acción de Administrador).
     *
     * @param userId    Id del usuario a eliminar.
     * @param callback Callback con el resultado de la operación.
     */
    public void deleteUserByAdmin(int userId, AdminActionCallback callback) {
        callback.onResult(Resource.loading());
        api.deleteUserByAdmin(userId).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call,
                                   @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    callback.onResult(Resource.success("Usuario eliminado correctamente"));
                } else {
                    callback.onResult(Resource.error(getError(response)));
                }
            }
            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                callback.onResult(Resource.error(ERROR_CONEXION + t.getMessage()));
            }
        });
    }

    /**
     * Banea/Suspende la cuenta de un usuario (Acción de Administrador).
     *
     * @param userId    Id del usuario a banear.
     * @param callback Callback con el resultado de la operación.
     */
    public void banUser(int userId, AdminActionCallback callback) {
        callback.onResult(Resource.loading());
        api.banUser(userId).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call,
                                   @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    callback.onResult(Resource.success("Usuario baneado correctamente"));
                } else {
                    callback.onResult(Resource.error(getError(response)));
                }
            }
            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                callback.onResult(Resource.error(ERROR_CONEXION + t.getMessage()));
            }
        });
    }

    /**
     * Extrae y traduce mensajes de error del servidor.
     */
    private String getError(Response<?> response) {

        try {
            if (response.errorBody() != null) {

                String json = response.errorBody().string();
                JsonObject obj = gson.fromJson(json, JsonObject.class);

                if (obj.has(MESSAGE_STRING) && !obj.get(MESSAGE_STRING).isJsonNull()) {
                    return obj.get(MESSAGE_STRING).getAsString();
                } else if (obj.has(ERROR_STRING) && !obj.get(ERROR_STRING).isJsonNull()) {
                    return obj.get(ERROR_STRING).getAsString();
                }
            }
        } catch (Exception ignored) {
            //EXCEPCION IGNORADA
        }

        int code = response.code();

        switch (code) {
            case 400: return "Datos incorrectos";
            case 401: return "Email o contraseña incorrectos";
            case 403: return "No tienes permiso para realizar esta acción";
            case 404: return "Recurso no encontrado";
            case 409: return "Email ya registrado";
            case 422: return "Revisa los datos introducidos";
            case 500: return "Error interno del servidor";
            default:  return "Error del servidor (" + code + ")";
        }
    }
}