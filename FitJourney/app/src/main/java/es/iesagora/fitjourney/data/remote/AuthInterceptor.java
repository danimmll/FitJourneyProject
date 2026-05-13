package es.iesagora.fitjourney.data.remote;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import java.io.IOException;

import es.iesagora.fitjourney.data.repositories.PreferencesRepository;
import es.iesagora.fitjourney.ui.main.MainActivity;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Interceptor de autenticación para las peticiones HTTP realizadas con OkHttp.
 * Se encarga de adjuntar el token JWT en la cabecera de autorización de cada petición.
 * Además, intercepta respuestas de error de autenticación (401 y 403) para forzar
 * el cierre de sesión y redirigir al usuario a la pantalla principal, excluyendo
 * las rutas de inicio de sesión y registro para permitir una gestión normal de errores.
 */
public class AuthInterceptor implements Interceptor {

    private final Context context;

    /**
     * Constructor del interceptor de autenticación.
     *
     * @param context Contexto de la aplicación necesario para acceder a preferencias e iniciar Intents.
     */
    public AuthInterceptor(Context context) {
        this.context = context;
    }

    /**
     * Intercepta la petición saliente para añadir el token y evalúa la respuesta entrante.
     *
     * @param chain Cadena de ejecución del interceptor.
     * @return La respuesta HTTP del servidor.
     * @throws IOException En caso de error de red.
     */
    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        PreferencesRepository prefs = new PreferencesRepository(context);
        String token = prefs.getToken();

        Request originalRequest = chain.request();
        Request.Builder requestBuilder = originalRequest.newBuilder();

        if (token != null && !token.isEmpty()) {
            requestBuilder.addHeader("Authorization", "Bearer " + token);
        }

        Response response = chain.proceed(requestBuilder.build());

        String path = originalRequest.url().encodedPath();
        boolean isAuthRoute = path.contains("auth/login") || path.contains("auth/register");

        if ((response.code() == 401 || response.code() == 403) && !isAuthRoute) {
            prefs.clearSession();

            Intent intent = new Intent(context, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
        }

        return response;
    }
}