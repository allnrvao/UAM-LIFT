package ni.edu.uam.uamlift.data.enums

import com.google.gson.annotations.SerializedName

enum class EstadoViaje {
    @SerializedName("PROPUESTO") PROPUESTO,
    @SerializedName("PROGRAMADO") PROGRAMADO,
    @SerializedName("EN_CURSO") EN_CURSO,
    @SerializedName("FINALIZADO") FINALIZADO,
    @SerializedName("CANCELADO") CANCELADO
}
