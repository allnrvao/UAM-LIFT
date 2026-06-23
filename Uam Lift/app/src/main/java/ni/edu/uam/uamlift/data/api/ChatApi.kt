package ni.edu.uam.uamlift.data.api

import ni.edu.uam.uamlift.data.dto.MensajeResponse
import retrofit2.http.*

interface ChatApi {
    @GET("api/chat/historial/{viajeId}")
    suspend fun obtenerHistorial(
        @Path("viajeId")
        viajeId: Long
    ): List<MensajeResponse>
}