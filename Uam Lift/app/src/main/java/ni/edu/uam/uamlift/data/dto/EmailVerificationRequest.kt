package ni.edu.uam.uamlift.data.dto

import com.google.gson.annotations.SerializedName

data class EmailVerificationRequest(
    @SerializedName("correo")
    val correo: String
)