package ni.edu.uam.uamlift.data.api

import ni.edu.uam.uamlift.data.models.Notificacion
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

interface NotificacionApiService {

    @GET("api/notificaciones/usuario/{usuarioId}")
    suspend fun obtenerNotificaciones(@Path("usuarioId") usuarioId: Long): List<Notificacion>

    @GET("api/notificaciones/usuario/{usuarioId}/no-leidas")
    suspend fun contarNoLeidas(@Path("usuarioId") usuarioId: Long): Long

    @PUT("api/notificaciones/{id}/leer")
    suspend fun marcarComoLeida(@Path("id") id: Long): Boolean

    @PUT("api/notificaciones/usuario/{usuarioId}/leer-todas")
    suspend fun marcarTodasComoLeidas(@Path("usuarioId") usuarioId: Long): Boolean
}
