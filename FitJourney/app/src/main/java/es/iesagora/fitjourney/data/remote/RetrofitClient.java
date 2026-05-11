package es.iesagora.fitjourney.data.remote;

import android.content.Context;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Clase encargada de crear y proporcionar la instancia
 * de Retrofit utilizada en toda la aplicación.
 * CORRECCIÓN: Se añade reset() para que al hacer logout el singleton
 * se destruya y el siguiente login cree un cliente HTTP limpio.
 * Sin esto, el interceptor de autenticación seguía usando el contexto
 * del usuario anterior.
 */
public class RetrofitClient {
    private RetrofitClient() {
        /* This utility class should not be instantiated */
    }


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
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(new AuthInterceptor(context))
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(AppApi.class);
    }

    /**
     * NUEVO: Destruye la instancia singleton.
     * Llamar desde MainViewModel.logout() para que el siguiente
     * usuario que inicie sesión obtenga un cliente HTTP fresco
     * con el interceptor apuntando a sus propias preferencias.
     */
    public static void reset() {
        retrofit = null;
    }
}
