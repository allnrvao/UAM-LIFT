package ni.edu.uam.uamlift.dto

import com.google.gson.annotations.SerializedName

data class EmailVerificationRequest(
    @SerializedName("correo")
    val correo: String
)
