package ni.edu.uam.uamlift.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import ni.edu.uam.uamlift.data.models.Notificacion

/**
 * Encargado de crear el canal de notificaciones y mostrar avisos del
 * sistema (en la bandeja del celular) cuando llega una notificación nueva
 * relacionada con un viaje (inicio o cancelación).
 */
object NotificationHelper {

    const val CHANNEL_ID = "uamlift_viajes_channel"

    fun crearCanalNotificaciones(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                CHANNEL_ID,
                "Notificaciones de viajes",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Avisos sobre inicio y cancelación de viajes de UAM Lift"
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.createNotificationChannel(canal)
        }
    }

    /**
     * Muestra una notificación del sistema con el título y mensaje recibidos
     * desde el backend. Si el usuario no ha otorgado el permiso de
     * notificaciones (Android 13+), simplemente no se muestra nada.
     */
    fun mostrarNotificacion(context: Context, notificacion: Notificacion) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(notificacion.titulo)
            .setContentText(notificacion.mensaje)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notificacion.mensaje))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        try {
            val notificationId = (notificacion.id ?: System.currentTimeMillis()).toInt()
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
        } catch (e: SecurityException) {
            // El usuario no concedió el permiso POST_NOTIFICATIONS; no hacemos nada más.
        }
    }
}
