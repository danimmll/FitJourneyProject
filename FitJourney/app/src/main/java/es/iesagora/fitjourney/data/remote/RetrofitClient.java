package es.iesagora.fitjourney.data.remote;

import android.content.Context;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Clase encargada de crear y proporcionar la instancia
 * de Retrofit utilizada en toda la aplicación.
 */
public class RetrofitClient {
    private RetrofitClient() { /* utility class */ }

    private static final String BASE_URL = "https://fitjourneyproject.onrender.com/api/";

    private static Retrofit retrofit = null;

    /**
     * Obtiene la instancia de la API configurada.
     * Si no existe aún la crea; si ya existe la reutiliza.
     *
     * @param context Contexto de la aplicación.
     * @return Interfaz AppApi lista para realizar peticiones.
     */
    public static AppApi getInstance(Context context) {
        if (retrofit == null) {

            Gson gson = new GsonBuilder()
                    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                    .create();

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(new AuthInterceptor(context))
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return retrofit.create(AppApi.class);
    }

    /**
     * Destruye la instancia singleton.
     * Llamar desde SecondViewModel.logout() para que el siguiente
     * usuario obtenga un cliente HTTP fresco.
     */
    public static void reset() {
        retrofit = null;
    }
}