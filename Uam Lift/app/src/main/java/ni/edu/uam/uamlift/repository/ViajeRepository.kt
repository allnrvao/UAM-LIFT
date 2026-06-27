package ni.edu.uam.uamlift.repository

import ni.edu.uam.uamlift.data.ViajeWebSocketManager

class ViajeRepository(
    private val socket: ViajeWebSocketManager
) {

    val ubicaciones = socket.ubicacion

    fun conectar(idViaje: Long) {

        socket.conectar(idViaje)

    }

    fun enviar(
        idViaje: Long,
        lat: Double,
        lng: Double
    ) {

        socket.enviarUbicacion(idViaje, lat, lng)

    }
}