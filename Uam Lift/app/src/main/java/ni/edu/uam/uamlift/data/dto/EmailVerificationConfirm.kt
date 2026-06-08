package ni.edu.uam.uamlift.data.dto

import com.google.gson.annotations.SerializedName

data class EmailVerificationConfirm(
    @SerializedName("correo")
    val correo: String,
    @SerializedName("code")
    val code: String
)