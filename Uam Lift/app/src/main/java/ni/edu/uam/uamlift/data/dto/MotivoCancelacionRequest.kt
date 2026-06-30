package ni.edu.uam.uamlift.data.dto

import com.google.gson.annotations.SerializedName

data class MotivoCancelacionRequest(
    @SerializedName("motivo")
    val motivo: String
)
