package es.iesagora.fitjourney.utils;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

/**
 * Singleton que actúa como canal de eventos de sesión entre capas desacopladas.
 */
public class SessionEvent {

    private static final MutableLiveData<Boolean> sessionExpired = new MutableLiveData<>();

    private SessionEvent() { /* singleton */ }

    /** LiveData que emite true cuando el token expira o es inválido (401). */
    public static LiveData<Boolean> getSessionExpired() {
        return sessionExpired;
    }

    /**
     * Señaliza que la sesión ha expirado.
     * Puede llamarse desde cualquier hilo (OkHttp callback).
     */
    public static void postSessionExpired() {
        new Handler(Looper.getMainLooper()).post(() -> sessionExpired.setValue(true));
    }

    /** Limpia el evento tras haberlo manejado para evitar re-disparos. */
    public static void clear() {
        new Handler(Looper.getMainLooper()).post(() -> sessionExpired.setValue(null));
    }
}