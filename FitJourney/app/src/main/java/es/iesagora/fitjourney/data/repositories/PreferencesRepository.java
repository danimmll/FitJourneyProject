package es.iesagora.fitjourney.data.repositories;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;

public class PreferencesRepository {

    private static final String PREF_NAME = "prefs";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_ROLE = "role";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_THEME = "app_theme";
    private static final String KEY_NOTIFICATIONS = "notifications";

    private final SharedPreferences sharedPreferences;

    public PreferencesRepository(Context context) {
        sharedPreferences = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void clearSession() {
        sharedPreferences.edit()
                .remove(KEY_TOKEN)
                .remove(KEY_ROLE)
                .remove(KEY_USER_ID)
                .apply();
    }

    public void clear() {
        sharedPreferences.edit().clear().apply();
    }

    public void setToken(String token) {
        sharedPreferences.edit().putString(KEY_TOKEN, token).apply();
    }
    public String getToken() {
        return sharedPreferences.getString(KEY_TOKEN, null);
    }

    public void setRole(String role) {
        sharedPreferences.edit().putString(KEY_ROLE, role).apply();
    }
    public String getRole() {
        return sharedPreferences.getString(KEY_ROLE, "user");
    }

    public void setUserId(String userId) {
        sharedPreferences.edit().putString(KEY_USER_ID, userId).apply();
    }
    public String getUserId() {
        return sharedPreferences.getString(KEY_USER_ID, null);
    }

    public void setTheme(int mode) {
        sharedPreferences.edit().putInt(KEY_THEME, mode).apply();
    }

    public int getTheme() {
        return sharedPreferences.getInt(KEY_THEME, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }

    public void setNotifications(boolean enabled) {
        sharedPreferences.edit().putBoolean(KEY_NOTIFICATIONS, enabled).apply();
    }
    public boolean getNotifications() {
        return sharedPreferences.getBoolean(KEY_NOTIFICATIONS, true);
    }
}