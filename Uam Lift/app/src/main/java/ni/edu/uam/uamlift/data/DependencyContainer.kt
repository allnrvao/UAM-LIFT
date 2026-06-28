package ni.edu.uam.uamlift.data

import ni.edu.uam.uamlift.repository.ViajeRepository

object DependencyContainer {
    
    private val client by lazy { RetrofitClient.getHttpClient() }

    private val viajeWebSocketManager by lazy {
        ViajeWebSocketManager(client)
    }

    val viajeRepository by lazy {
        ViajeRepository(viajeWebSocketManager)
    }
}
