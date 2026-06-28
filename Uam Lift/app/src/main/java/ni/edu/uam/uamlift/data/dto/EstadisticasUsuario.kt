package ni.edu.uam.uamlift.data.dto

import com.google.gson.annotations.SerializedName
data class EstadisticasUsuario(
    @SerializedName("totalViajes")
    val totalViajes: Int = 0,

    @SerializedName("kilometrosTotales")
    val kilometrosTotales: Double = 0.0,

    @SerializedName("co2Ahorrado")
    val co2Ahorrado: Double = 0.0
)