package es.iesagora.fitjourney.data.remote;

import android.content.Context;

import androidx.annotation.NonNull;

import java.io.IOException;

import es.iesagora.fitjourney.data.repositories.PreferencesRepository;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Interceptor encargado de añadir automáticamente
 * el token de autenticación a todas las peticiones HTTP.
 * También detecta errores de autenticación (401)
 * para limpiar la sesión del usuario cuando el token
 * deja de ser válido.
 */
public class AuthInterceptor implements Interceptor {

    /**
     * Repositorio utilizado para acceder a las preferencias
     * y datos guardados del usuario.
     */
    private final PreferencesRepository preferences;

    /**
     * Constructor del interceptor.
     *
     * @param context Contexto de la aplicación.
     */
    public AuthInterceptor(Context context) {
        this.preferences = new PreferencesRepository(context);
    }

    /**
     * Intercepta cada petición HTTP antes de enviarla.
     * Si existe un token guardado, lo añade en la cabecera
     * Authorization utilizando el formato Bearer Token.
     * También comprueba si la respuesta devuelve un error 401
     * (no autorizado). En ese caso, elimina los datos de sesión.
     *
     * @param chain Cadena de interceptores de OkHttp.
     * @return Respuesta de la petición.
     * @throws IOException Error durante la conexión.
     */
    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {

        // Obtenemos la petición original
        Request originalRequest = chain.request();

        // Creamos una copia para poder modificarla
        Request.Builder builder = originalRequest.newBuilder();

        // Recuperamos el token guardado
        String token = preferences.getToken();

        // Si existe token, lo añadimos a la cabecera Authorization
        if (token != null && !token.isEmpty()) {
            builder.addHeader("Authorization", "Bearer " + token);
        }

        // Continuamos con la petición modificada
        Response response = chain.proceed(builder.build());

        // Si el servidor devuelve 401, limpiamos la sesión
        if (response.code() == 401) {
            preferences.clearSession();
        }

        return response;
    }
}