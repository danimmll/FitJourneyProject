package es.iesagora.fitjourney.data.remote;

import android.content.Context;

import androidx.annotation.NonNull;

import java.io.IOException;

import es.iesagora.fitjourney.data.repositories.PreferencesRepository;
import es.iesagora.fitjourney.utils.SessionEvent;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Interceptor encargado de añadir automáticamente
 * el token de autenticación a todas las peticiones HTTP.
 */
public class AuthInterceptor implements Interceptor {

    private final PreferencesRepository preferences;

    public AuthInterceptor(Context context) {
        this.preferences = new PreferencesRepository(context);
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {

        Request originalRequest = chain.request();
        Request.Builder builder = originalRequest.newBuilder();

        String token = preferences.getToken();

        if (token != null && !token.isEmpty()) {
            builder.addHeader("Authorization", "Bearer " + token);
        }

        Response response = chain.proceed(builder.build());

        if (response.code() == 401) {
            preferences.clearSession();
            SessionEvent.postSessionExpired();
        }

        return response;
    }
}