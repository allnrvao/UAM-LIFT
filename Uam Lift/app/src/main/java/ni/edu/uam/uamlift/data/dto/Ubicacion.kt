package ni.edu.uam.uamlift.data.dto

data class Ubicacion (
    val idViaje: Long,
    val latitud: Double?=null,
    val longitud: Double?=null,
    val tipo: String
)