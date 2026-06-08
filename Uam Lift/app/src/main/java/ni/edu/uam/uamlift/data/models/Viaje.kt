package ni.edu.uam.uamlift.data.models

import com.google.gson.annotations.SerializedName

data class Viaje(
    @SerializedName("id")
    val id: Long? = null,

    @SerializedName("origen")
    val origen: Destino? = null,

    @SerializedName("destino")
    val destino: Destino? = null,

    @SerializedName("fechaHoraSalida")
    val fechaHoraSalida: String? = null,

    @SerializedName("fechaHoraLlegada")
    val fechaHoraLlegada: String? = null,

    @SerializedName("numeroAsientosDisponibles")
    val numeroAsientosDisponibles: Int = 0,

    @SerializedName("precioPorPersona")
    val precioPorPersona: Double = 0.0,

    @SerializedName("pasajeros")
    val pasajeros: List<ViajeUsuario> = emptyList(),

    @SerializedName("conductor")
    val conductor: Usuario? = null,

    @SerializedName("estadoViaje")
    val estadoViaje: EstadoViaje? = null
)

enum class EstadoViaje {
    @SerializedName("PROGRAMADO") PROGRAMADO,
    @SerializedName("EN_CURSO") EN_CURSO,
    @SerializedName("FINALIZADO") FINALIZADO,
    @SerializedName("CANCELADO") CANCELADO
}
