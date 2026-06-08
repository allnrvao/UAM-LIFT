package ni.edu.uam.uamlift.api

import ni.edu.uam.uamlift.dto.EmailVerificationConfirm
import ni.edu.uam.uamlift.dto.EmailVerificationRequest
import ni.edu.uam.uamlift.models.Usuario
import retrofit2.http.*

interface UsuarioApiService {
    @POST("api/usuarios")
    suspend fun registrarUsuario(@Body usuario: Usuario): Boolean

    @PUT("api/usuarios/{cif}")
    suspend fun actualizarUsuario(
        @Path("cif") cif: String,
        @Body usuario: Usuario
    ): Boolean

    @DELETE("api/usuarios/{cif}")
    suspend fun eliminarUsuario(@Path("cif") cif: String): Boolean

    @POST("api/usuarios/verificacion/solicitar")
    suspend fun solicitarVerificacion(@Body request: EmailVerificationRequest): Boolean

    @POST("api/usuarios/verificacion/confirmar")
    suspend fun confirmarVerificacion(@Body request: EmailVerificationConfirm): Boolean
}
