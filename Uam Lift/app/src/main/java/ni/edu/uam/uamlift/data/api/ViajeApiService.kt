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
    ): Viaje

    // 🌟 CORREGIDO: Cambiado de Boolean a Map<String, Boolean> para manejar las respuestas de Spring Boot {"success": true}
    @PUT("api/viajes/{viajeId}/pasajeros/{usuarioCif}")
    suspend fun agregarPasajero(
        @Path("viajeId") viajeId: Long,
        @Path("usuarioCif") usuarioCif: String
    ): Map<String, Boolean>

    // 🌟 CORREGIDO: Cambiado de Boolean a Map<String, Boolean>
    @PUT("api/viajes/{viajeId}/cancelar/{usuarioCif}")
    suspend fun cancelarParticipacion(
        @Path("viajeId") viajeId: Long,
        @Path("usuarioCif") usuarioCif: String
    ): Map<String, Boolean>

    // 🌟 CORREGIDO: Cambiado de Boolean a Map<String, Boolean>
    @PUT("api/viajes/{viajeId}/finalizar")
    suspend fun finalizarViaje(@Path("viajeId") viajeId: Long): Map<String, Boolean>

    // 🌟 AGREGADO: El método faltante que requería tu ViewModel para arrancar la ruta
    @PUT("api/viajes/{viajeId}/iniciar")
    suspend fun iniciarViaje(@Path("viajeId") viajeId: Long): Map<String, Boolean>
}