package ni.edu.uam.uamlift

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import ni.edu.uam.uamlift.ui.theme.UamLiftTheme

import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import ni.edu.uam.uamlift.notifications.NotificationHelper

class MainActivity : ComponentActivity() {

    // Intent que trajo a esta Activity al frente al tocar una notificación del
    // sistema. Se expone como estado de Compose para que UamLiftApp reaccione
    // y navegue igual que si el usuario hubiera tocado la notificación dentro
    // de la propia app (pantalla de Notificaciones).
    private var pendingNotificationIntent by mutableStateOf<Intent?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotificationHelper.crearCanalNotificaciones(this)

        // Cubre el caso de "cold start": la app estaba cerrada y se abrió
        // directamente al tocar la notificación.
        pendingNotificationIntent = intent.takeIf {
            it.hasExtra(NotificationHelper.EXTRA_NOTIF_TIPO)
        }

        setContent {
            UamLiftTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    UamLiftApp(
                        pendingNotificationIntent = pendingNotificationIntent,
                        onPendingNotificationConsumed = { pendingNotificationIntent = null }
                    )
                }
            }
        }
    }

    // Cubre el caso en que la app ya estaba abierta (o en segundo plano) y el
    // usuario toca la notificación: gracias a FLAG_ACTIVITY_SINGLE_TOP no se
    // crea una nueva instancia, sino que llega aquí.
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.hasExtra(NotificationHelper.EXTRA_NOTIF_TIPO)) {
            pendingNotificationIntent = intent
        }
    }
}