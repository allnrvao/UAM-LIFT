package ni.edu.uam.uamlift.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import ni.edu.uam.uamlift.data.models.*

class ViajeViewModel : ViewModel() {

    var viaje by mutableStateOf(Viaje())
        private set

    fun cargarViaje(viajeBD: Viaje) {
        viaje = viajeBD
    }

    fun actualizarOrigen(origen: Destino) {
        viaje = viaje.copy(origen = origen)
    }

    fun actualizarDestino(destino: Destino) {
        viaje = viaje.copy(destino = destino)
    }

    fun actualizarFechaHoraSalida(fecha: String) {
        viaje = viaje.copy(fechaHoraSalida = fecha)
    }

    fun actualizarFechaHoraLlegada(fecha: String) {
        viaje = viaje.copy(fechaHoraLlegada = fecha)
    }

    fun actualizarNumeroAsientos(numero: Int) {
        viaje = viaje.copy(numeroAsientosDisponibles = numero)
    }

    fun actualizarPrecio(precio: Double) {
        viaje = viaje.copy(precioPorPersona = precio)
    }

    fun actualizarConductor(conductor: Usuario) {
        viaje = viaje.copy(conductor = conductor)
    }

    fun actualizarEstadoViaje(estado: EstadoViaje) {
        viaje = viaje.copy(estadoViaje = estado)
    }

    fun actualizarPasajeros(pasajeros: List<ViajeUsuario>) {
        viaje = viaje.copy(pasajeros = pasajeros)
    }
}