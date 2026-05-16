package es.iesagora.fitjourney.data.repositories;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

/**
 * Repositorio encargado de gestionar las preferencias
 * locales de la aplicación mediante SharedPreferences.
 * Permite guardar datos persistentes del usuario y
 * configuraciones generales de la app.
 */
public class PreferencesRepository {

    /**
     * Nombre del archivo de preferencias.
     */
    private static final String PREF_NAME = "prefs";

    /**
     * Clave del token de autenticación.
     */
    private static final String KEY_TOKEN = "token";

    /**
     * Clave del rol del usuario.
     */
    private static final String KEY_ROLE = "role";

    /**
     * Clave del ID del usuario.
     */
    private static final String KEY_USER_ID = "user_id";

    /**
     * Clave del tema de la aplicación.
     */
    private static final String KEY_THEME = "app_theme";

    /**
     * Clave de las notificaciones.
     */
    private static final String KEY_NOTIFICATIONS = "notifications";

    /**
     * Instancia de SharedPreferences.
     */
    private final SharedPreferences sharedPreferences;

    /**
     * Constructor del repositorio.
     *
     * @param context Contexto de la aplicación.
     */
    public PreferencesRepository(Context context) {
        sharedPreferences = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Elimina los datos de sesión almacenados.
     */
    public void clearSession() {
        sharedPreferences.edit()
                .remove(KEY_TOKEN)
                .remove(KEY_ROLE)
                .remove(KEY_USER_ID)
                .apply();
    }

    /**
     * Guarda el token de autenticación.
     *
     * @param token Token JWT.
     */
    public void setToken(String token) {
        sharedPreferences.edit().putString(KEY_TOKEN, token).apply();
    }

    /**
     * Obtiene el token almacenado.
     *
     * @return Token o null.
     */
    public String getToken() {
        return sharedPreferences.getString(KEY_TOKEN, null);
    }

    /**
     * Guarda el rol del usuario.
     *
     * @param role Rol del usuario.
     */
    public void setRole(String role) {
        sharedPreferences.edit().putString(KEY_ROLE, role).apply();
    }

    /**
     * Obtiene el rol almacenado.
     *
     * @return Rol del usuario.
     */
    public String getRole() {
        return sharedPreferences.getString(KEY_ROLE, "user");
    }

    /**
     * Guarda el ID del usuario.
     *
     * @param userId ID del usuario.
     */
    public void setUserId(String userId) {
        sharedPreferences.edit().putString(KEY_USER_ID, userId).apply();
    }

    /**
     * Obtiene el ID almacenado.
     *
     * @return ID del usuario o null.
     */
    public String getUserId() {
        return sharedPreferences.getString(KEY_USER_ID, null);
    }

    /**
     * Guarda el modo de tema de la aplicación.
     *
     * @param mode Modo de tema.
     */
    public void setTheme(int mode) {
        sharedPreferences.edit().putInt(KEY_THEME, mode).apply();
    }

    /**
     * Obtiene el modo de tema configurado.
     *
     * @return Modo de tema guardado.
     */
    public int getTheme() {
        return sharedPreferences.getInt(
                KEY_THEME,
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        );
    }

    /**
     * Activa o desactiva las notificaciones.
     *
     * @param enabled Estado de las notificaciones.
     */
    public void setNotifications(boolean enabled) {
        sharedPreferences.edit().putBoolean(KEY_NOTIFICATIONS, enabled).apply();
    }

    /**
     * Obtiene el estado de las notificaciones.
     *
     * @return true si están activadas.
     */
    public boolean getNotifications() {
        return sharedPreferences.getBoolean(KEY_NOTIFICATIONS, true);
    }
}