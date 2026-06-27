package ni.edu.uam.uamlift.data.api

import ni.edu.uam.uamlift.data.models.Usuario
import ni.edu.uam.uamlift.data.models.Viaje
import retrofit2.http.*

interface ViajeApiService {

    @GET("api/viajes")
    suspend fun obtenerTodosLosViajes(): List<Viaje>

    @POST("api/viajes/{conductorCif}")
    suspend fun crearViaje(
        @Path("conductorCif") conductorCif: String,
        @Body viaje: Viaje
    ): Viaje

    @PUT("api/viajes/{viajeId}/{conductorId}/iniciar")
    suspend fun iniciarViaje(
        @Path("viajeId") viajeId: Long,
        @Path("conductorId") conductorId: Long
    ): Boolean

    @PUT("api/viajes/{viajeId}/finalizar")
    suspend fun finalizarViaje(@Path("viajeId") viajeId: Long): Boolean

    @PUT("api/viajes/{viajeId}/pasajeros/{usuarioCif}")
    suspend fun agregarPasajero(
        @Path("viajeId") viajeId: Long,
        @Path("usuarioCif") usuarioCif: String
    ): Boolean

    @PUT("api/viajes/{viajeId}/cancelar/{usuarioCif}")
    suspend fun cancelarParticipacion(
        @Path("viajeId") viajeId: Long,
        @Path("usuarioCif") usuarioCif: String
    ): Boolean

    @PUT("api/viajes/{viajeId}/cancelar")
    suspend fun cancelarViaje(@Path("viajeId") viajeId: Long): Boolean

    @GET("api/viajes/validar/{usuarioId}/{fechaSalida}/{fechaLlegada}")
    suspend fun validarFechas(
        @Path("usuarioId") usuarioId: Long,
        @Path("fechaSalida") fechaSalida: String,
        @Path("fechaLlegada") fechaLlegada: String
    ): Boolean

    @GET("api/viajes/validar/numviajes/{usuarioId}")
    suspend fun validarNumViajes(@Path("usuarioId") usuarioId: Long): Boolean

    @GET("api/viajes/usuario/{usuarioId}")
    suspend fun obtenerViajesPorUsuario(@Path("usuarioId") usuarioId: Long): List<Viaje>

    @GET("api/viajes/conductor/{usuarioId}")
    suspend fun obtenerViajesPorConductor(@Path("usuarioId") usuarioId: Long): List<Viaje>

    @GET("api/viajes/pasajero/{usuarioId}")
    suspend fun obtenerViajesPorPasajero(@Path("usuarioId") usuarioId: Long): List<Viaje>

    @GET("api/viajes/noconductor/{usuarioId}")
    suspend fun usuarioEsConductor(@Path("usuarioId") usuarioId: Long): Boolean

    @GET("api/viajes/{viajeId}/pasajeros")
    suspend fun obtenerPasajerosPorViaje(@Path("viajeId") viajeId: Long): List<Usuario>
}
