package ni.edu.uam.uamlift.data.models

import com.google.gson.annotations.SerializedName

data class EmailVerification(
    @SerializedName("id")
    val id: Long? = null,

    @SerializedName("usuario")
    val usuario: Usuario? = null,

    @SerializedName("code")
    val code: String? = null,

    @SerializedName("expiresAt")
    val expiresAt: String? = null,

    @SerializedName("verifiedAt")
    val verifiedAt: String? = null
)
