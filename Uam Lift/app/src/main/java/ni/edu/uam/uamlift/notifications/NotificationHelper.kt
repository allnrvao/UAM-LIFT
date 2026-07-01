package ni.edu.uam.uamlift.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import ni.edu.uam.uamlift.MainActivity
import ni.edu.uam.uamlift.data.models.Notificacion

object NotificationHelper {

    const val CHANNEL_ID = "uamlift_viajes_channel"

    // Claves usadas para viajar los datos de la notificación dentro del Intent que
    // abre (o trae al frente) la app cuando el usuario la toca en el panel del
    // sistema. Con esto, UamLiftApp puede navegar exactamente a donde navegaría
    // si el usuario hubiera tocado la misma notificación dentro de la app.
    const val EXTRA_NOTIF_ID = "extra_notificacion_id"
    const val EXTRA_NOTIF_TIPO = "extra_notificacion_tipo"
    const val EXTRA_NOTIF_VIAJE_ID = "extra_notificacion_viaje_id"
    const val EXTRA_NOTIF_TITULO = "extra_notificacion_titulo"
    const val EXTRA_NOTIF_MENSAJE = "extra_notificacion_mensaje"

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
        val notificationId = (notificacion.id ?: System.currentTimeMillis()).toInt()

        // Intent que abre (o trae al frente) MainActivity con los datos de la
        // notificación. FLAG_ACTIVITY_SINGLE_TOP hace que, si la app ya está
        // corriendo, se reutilice la misma Activity (onNewIntent) en vez de
        // crear una nueva instancia.
        val intent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_NOTIF_ID, notificacion.id ?: -1L)
            putExtra(EXTRA_NOTIF_TIPO, notificacion.tipo?.name)
            putExtra(EXTRA_NOTIF_VIAJE_ID, notificacion.viajeId ?: -1L)
            putExtra(EXTRA_NOTIF_TITULO, notificacion.titulo)
            putExtra(EXTRA_NOTIF_MENSAJE, notificacion.mensaje)
        }

        // requestCode único por notificación para que cada PendingIntent guarde
        // sus propios extras (si no, Android reutilizaría los del primero).
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(notificacion.titulo)
            .setContentText(notificacion.mensaje)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notificacion.mensaje))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        try {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
        } catch (e: SecurityException) {
            // El usuario no concedió el permiso POST_NOTIFICATIONS; no hacemos nada más.
        }
    }
}