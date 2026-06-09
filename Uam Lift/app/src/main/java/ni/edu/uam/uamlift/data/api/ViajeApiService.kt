package ni.edu.uam.uamlift.data.api

import ni.edu.uam.uamlift.data.models.Viaje
import retrofit2.http.*

interface ViajeApiService {
    @POST("api/viajes/{conductorCif}")
    suspend fun crearViaje(
        @Path("conductorCif") conductorCif: String,
        @Body viaje: Viaje
    ): Boolean

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

    @PUT("api/viajes/{viajeId}/finalizar")
    suspend fun finalizarViaje(@Path("viajeId") viajeId: Long): Boolean
}