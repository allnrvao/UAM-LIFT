package ni.edu.uam.uamlift.data.api

import ni.edu.uam.uamlift.data.dto.EmailVerificationConfirm
import ni.edu.uam.uamlift.data.dto.EmailVerificationRequest
import ni.edu.uam.uamlift.data.models.Usuario
import okhttp3.ResponseBody
import retrofit2.http.*

interface UsuarioApiService {


    @POST("api/usuarios")
    suspend fun registrarUsuario(@Body usuario: Usuario): ResponseBody


    @GET("api/usuarios/correoBol/{correo}")
    suspend fun verificarCorreo(@Path("correo") correo: String): ResponseBody

    @GET("api/usuarios/cif/{cif}")
    suspend fun obtenerPorCif(
        @Path("cif") cif: String
    ): Usuario

    @GET("api/usuarios/correo/{correo}")
    suspend fun obtenerPorCorreo(
        @Path("correo") correo: String
    ): Usuario

    @GET("api/usuarios/nombreUsuario/{nombreUsuario}")
    suspend fun obtenerPorNombreUsuario(
        @Path("nombreUsuario") nombreUsuario: String
    ): Usuario

    @PUT("api/usuarios/{cif}")
    suspend fun actualizarUsuario(
        @Path("cif") cif: String?,
        @Body usuario: Usuario
    ): Boolean

    @DELETE("api/usuarios/{cif}")
    suspend fun eliminarUsuario(
        @Path("cif") cif: String?
    ): Boolean

    @POST("api/usuarios/verificacion/solicitar")
    suspend fun solicitarVerificacion(
        @Body request: EmailVerificationRequest
    ): Boolean

    @POST("api/usuarios/verificacion/confirmar")
    suspend fun confirmarVerificacion(
        @Body request: EmailVerificationConfirm
    ): Boolean
}