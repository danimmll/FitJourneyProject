package es.iesagora.fitjourney.data.remote;

import android.content.Context;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Clase encargada de crear y proporcionar la instancia
 * de Retrofit utilizada en toda la aplicación.
 * Configura la URL base de la API, el cliente HTTP
 * y los conversores necesarios para el intercambio
 * de datos en formato JSON.
 */
public class RetrofitClient {

    /**
     * URL base del servidor de la API.
     */
    private static final String BASE_URL = "https://fitjourneyproject.onrender.com/";

    /**
     * Instancia única de Retrofit (patrón Singleton).
     */
    private static Retrofit retrofit = null;

    /**
     * Obtiene la instancia de la API configurada.
     * Si no existe aún, la crea configurando:
     * - Interceptor de autenticación
     * - Cliente HTTP (OkHttp)
     * - Conversor JSON (Gson)
     *
     * @param context Contexto de la aplicación.
     * @return Interfaz AppApi lista para realizar peticiones.
     */
    public static AppApi getInstance(Context context) {

        if (retrofit == null) {

            // Cliente HTTP con interceptor de autenticación
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(new AuthInterceptor(context))
                    .build();

            // Construcción de Retrofit
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        return retrofit.create(AppApi.class);
    }
}