package es.iesagora.fitjourney.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import es.iesagora.fitjourney.R;
import es.iesagora.fitjourney.data.repositories.PreferencesRepository;
import es.iesagora.fitjourney.ui.main.MainActivity;

/**
 * Worker encargado de generar notificaciones periódicas de recordatorio de entrenamiento.
 * Este worker se ejecuta en segundo plano mediante WorkManager y se encarga de:
 * - Comprobar las preferencias del usuario sobre notificaciones
 * - Crear y configurar un canal de notificación (en Android Oreo o superior)
 * - Construir y mostrar una notificación motivacional de entrenamiento
 * - Redirigir al usuario a la aplicación al pulsarla
 * Su objetivo es fomentar la constancia del usuario en sus entrenamientos
 * mediante recordatorios diarios automáticos.
 */
public class NotificationWorker extends Worker {

    /**
     * Constructor del worker.
     *
     * @param context contexto de la aplicación
     * @param workerParams parámetros de ejecución del worker
     */
    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    /**
     * Método principal que ejecuta el trabajo en segundo plano.
     * Se encarga de verificar si el usuario tiene activadas las notificaciones.
     * Si están activadas, lanza una notificación de recordatorio de entrenamiento.
     *
     * @return Result.success() si la ejecución fue correcta
     */
    @NonNull
    @Override
    public Result doWork() {
        PreferencesRepository prefs = new PreferencesRepository(getApplicationContext());

        // Si el usuario ha desactivado notificaciones, no se hace nada
        if (!prefs.getNotifications()) {
            return Result.success();
        }

        sendAdvise();
        return Result.success();
    }

    /**
     * Construye y envía la notificación al sistema.
     * Incluye:
     * - Creación del canal de notificación (Android 8+)
     * - Configuración del intent para abrir la app al pulsar
     * - Definición del contenido de la notificación
     * - Publicación en el NotificationManager
     */
    private void sendAdvise() {
        Context context = getApplicationContext();
        String canalID = "canal_fitjourney";

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE
        );

        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Creación del canal en Android Oreo o superior
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel = new NotificationChannel(
                    canalID,
                    "Recordatorios de Entrenamiento",
                    NotificationManager.IMPORTANCE_HIGH
            );

            manager.createNotificationChannel(channel);
        }

        // Construcción de la notificación
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, canalID)
                .setSmallIcon(R.drawable.ic_ejercicios)
                .setContentTitle("FitJourney")
                .setContentText("Oye, toca entrenar un poco 💪")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        // Mostrar notificación
        manager.notify(1, builder.build());
    }
}