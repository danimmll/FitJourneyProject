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
 * Repositorio encargado de gestionar las operaciones
 * relacionadas con los usuarios.
 * Incluye autenticación, perfil, administración
 * y gestión de cuentas.
 */
public class UserRepository {

    /* CONSTANTES */

    /**
     * Mensaje utilizado para errores de conexión.
     */
    private static final String ERROR_CONEXION = "Error de conexión: ";

    /**
     * Campo "message" utilizado en respuestas del servidor.
     */
    private static final String MESSAGE_STRING = "message";

    /**
     * Campo "error" utilizado en respuestas del servidor.
     */
    private static final String ERROR_STRING = "error";

    /**
     * Instancia de la API para realizar peticiones.
     */
    private final AppApi api;

    /**
     * Instancia de Gson para procesar respuestas JSON.
     */
    private final Gson gson = new Gson();

    /**
     * Callback para operaciones de login.
     */
    public interface LoginCallBack {

        /**
         * Devuelve el resultado de la operación.
         *
         * @param result Resultado del login.
         */
        void onResult(Resource<AccessResponse> result);
    }

    /**
     * Callback para el registro de usuarios.
     */
    public interface RegisterCallback {

        /**
         * Devuelve el resultado de la operación.
         *
         * @param result Resultado del registro.
         */
        void onResult(Resource<AccessResponse> result);
    }

    /**
     * Callback para eliminación o desactivación de cuentas.
     */
    public interface DeleteCallback {

        /**
         * Devuelve el resultado de la operación.
         *
         * @param result Resultado de la acción.
         */
        void onResult(Resource<String> result);
    }

    /**
     * Callback para operaciones relacionadas con el perfil.
     */
    public interface ProfileCallback {

        /**
         * Devuelve el resultado de la operación.
         *
         * @param result Resultado con los datos del usuario.
         */
        void onResult(Resource<User> result);
    }

    /**
     * Callback para obtener listas de usuarios.
     */
    public interface UserListCallback {

        /**
         * Devuelve el resultado de la operación.
         *
         * @param result Resultado con la lista de usuarios.
         */
        void onResult(Resource<List<User>> result);
    }

    /**
     * Callback para acciones de administrador.
     */
    public interface AdminActionCallback {

        /**
         * Devuelve el resultado de la operación.
         *
         * @param result Resultado de la acción administrativa.
         */
        void onResult(Resource<String> result);
    }

    /**
     * Constructor del repositorio.
     *
     * @param context Contexto de la aplicación.
     */
    public UserRepository(Context context) {
        api = RetrofitClient.getInstance(context);
    }

    /**
     * Registra un nuevo usuario.
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
     * Inicia sesión con las credenciales del usuario.
     *
     * @param email Email del usuario.
     * @param password Contraseña del usuario.
     * @param callback Callback con el resultado.
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
     * Reactiva una cuenta previamente desactivada.
     *
     * @param email Email del usuario.
     * @param password Contraseña del usuario.
     * @param callback Callback con el resultado.
     */
    public void reactivateAccount(String email, String password,
                                  LoginCallBack callback) {

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
     *
     * @param email Email del usuario.
     * @param password Contraseña del usuario.
     * @param callback Callback con el resultado.
     */
    public void deleteAccountPhysical(String email, String password,
                                      DeleteCallback callback) {

        callback.onResult(Resource.loading());

        LoginRequest request = new LoginRequest(email, password);

        api.deleteAccountPhysical(request).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call,
                                   @NonNull Response<ResponseBody> response) {

                if (response.isSuccessful()) {
                    callback.onResult(
                            Resource.success("Cuenta eliminada correctamente")
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
     * Obtiene los datos del usuario autenticado.
     *
     * @param token Token del usuario.
     * @param callback Callback con el resultado.
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

                callback.onResult(
                        Resource.error(ERROR_CONEXION + t.getMessage())
                );
            }
        });
    }

    /**
     * Actualiza el perfil del usuario autenticado.
     *
     * @param user Datos actualizados.
     * @param callback Callback con el resultado.
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

                callback.onResult(
                        Resource.error(ERROR_CONEXION + t.getMessage())
                );
            }
        });
    }

    /**
     * Permite a un administrador actualizar un usuario.
     *
     * @param userId ID del usuario.
     * @param user Nuevos datos del usuario.
     * @param callback Callback con el resultado.
     */
    public void updateUserByAdmin(int userId, User user,
                                  ProfileCallback callback) {

        callback.onResult(Resource.loading());

        api.updateUserByAdmin(userId, user).enqueue(new Callback<>() {
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

                callback.onResult(
                        Resource.error(ERROR_CONEXION + t.getMessage())
                );
            }
        });
    }

    /**
     * Obtiene todos los usuarios registrados.
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

                callback.onResult(
                        Resource.error(ERROR_CONEXION + t.getMessage())
                );
            }
        });
    }

    /**
     * Elimina un usuario desde el panel de administración.
     *
     * @param userId ID del usuario.
     * @param callback Callback con el resultado.
     */
    public void deleteUserByAdmin(int userId,
                                  AdminActionCallback callback) {

        callback.onResult(Resource.loading());

        api.deleteUserByAdmin(userId).enqueue(new Callback<>() {

            @Override
            public void onResponse(@NonNull Call<ResponseBody> call,
                                   @NonNull Response<ResponseBody> response) {

                if (response.isSuccessful()) {
                    callback.onResult(
                            Resource.success("Usuario eliminado correctamente")
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
     * Desactiva la cuenta del usuario actual.
     *
     * @param email Email del usuario.
     * @param password Contraseña del usuario.
     * @param callback Callback con el resultado.
     */
    public void deactivateUser(String email, String password,
                               DeleteCallback callback) {

        callback.onResult(Resource.loading());

        LoginRequest request = new LoginRequest(email, password);

        api.deactivateUser(request).enqueue(new Callback<>() {

            @Override
            public void onResponse(@NonNull Call<ResponseBody> call,
                                   @NonNull Response<ResponseBody> response) {

                if (response.isSuccessful()) {
                    callback.onResult(
                            Resource.success("Cuenta eliminada correctamente")
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
     * Obtiene y traduce los errores enviados por el servidor.
     *
     * @param response Respuesta HTTP.
     * @return Mensaje de error procesado.
     */
    private String getError(Response<?> response) {

        try {

            if (response.errorBody() != null) {

                String json = response.errorBody().string();

                JsonObject obj = gson.fromJson(json, JsonObject.class);

                if (obj.has(MESSAGE_STRING)
                        && !obj.get(MESSAGE_STRING).isJsonNull()) {

                    return obj.get(MESSAGE_STRING).getAsString();

                } else if (obj.has(ERROR_STRING)
                        && !obj.get(ERROR_STRING).isJsonNull()) {

                    return obj.get(ERROR_STRING).getAsString();
                }
            }

        } catch (Exception ignored) {
            // EXCEPCIÓN IGNORADA
        }

        int code = response.code();

        switch (code) {

            case 400:
                return "Datos incorrectos";

            case 401:
                return "Email o contraseña incorrectos";

            case 403:
                return "No tienes permiso para realizar esta acción";

            case 404:
                return "Recurso no encontrado";

            case 409:
                return "Email ya registrado";

            case 422:
                return "Revisa los datos introducidos";

            case 500:
                return "Error interno del servidor";

            default:
                return "Error del servidor (" + code + ")";
        }
    }
}