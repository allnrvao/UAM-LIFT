// Sample Kotlin snippet for Android real-time map updates using DefaultLifecycleObserver.
// This is a reference file; integrate into your Android module.

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient

// Minimal payload matching backend LocationUpdate

data class LocationUpdate(
    val id: String,
    val latitude: Double,
    val longitude: Double,
    val timestamp: String
)

class ViajeRealtimeObserver(
    private val viajeId: String,
    private val map: GoogleMap,
    private val wsUrl: String
) : DefaultLifecycleObserver {

    private var stompClient: StompClient? = null
    private val disposables = CompositeDisposable()
    private var marker: Marker? = null
    private var topicDisposable: Disposable? = null

    override fun onStart(owner: LifecycleOwner) {
        connectAndSubscribe()
    }

    override fun onStop(owner: LifecycleOwner) {
        topicDisposable?.dispose()
        disposables.clear()
        stompClient?.disconnect()
    }

    private fun connectAndSubscribe() {
        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, wsUrl).apply {
            withClientHeartbeat(10000)
            withServerHeartbeat(10000)
            connect()
        }

        topicDisposable = stompClient!!.topic("/topic/viaje.$viajeId")
            .subscribe({ message ->
                val update = parseLocationUpdate(message.payload)
                renderOnMap(update)
            }, {
                // Log error or show UI hint
            })

        topicDisposable?.let { disposables.add(it) }
    }

    private fun parseLocationUpdate(payload: String): LocationUpdate {
        // Replace with Gson/Moshi in your app
        throw NotImplementedError("Implementa parser JSON")
    }

    private fun renderOnMap(update: LocationUpdate) {
        val position = LatLng(update.latitude, update.longitude)
        if (marker == null) {
            marker = map.addMarker(MarkerOptions().position(position).title("Conductor"))
        } else {
            marker?.position = position
        }
    }
}
