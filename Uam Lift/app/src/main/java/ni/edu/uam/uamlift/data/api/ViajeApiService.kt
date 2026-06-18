package ni.edu.uam.uamlift.data.api

import ni.edu.uam.uamlift.data.models.Viaje
import retrofit2.http.*

interface ViajeApiService {
    @GET("api/viajes")
    suspend fun obtenerTodosLosViajes(): List<Viaje>

    @POST("api/viajes/{conductorCif}")
    suspend fun crearViaje(
        @Path("conductorCif") conductorCif: String,
        @Body viaje: Viaje
    ): Boolean

    @PUT("api/viajes/{viajeId}/iniciar")
    suspend fun iniciarViaje(@Path("viajeId") viajeId: Long): Boolean

    @PUT("api/viajes/{viajeId}/finalizar")
    suspend fun finalizarViaje(@Path("viajeId") viajeId: Long): Boolean

    @PUT("api/viajes/{viajeId}/pasajeros/{usuarioCif}")
    suspend fun agregarPasajero(
        @Path("viajeId") viajeId: Long,
        @Path("usuarioCif") usuarioCif: String
    ): Boolean
}
