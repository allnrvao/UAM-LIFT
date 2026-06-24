package ni.edu.uam.uamlift.data.api

import ni.edu.uam.uamlift.data.models.Destino
import retrofit2.http.*

interface DestinoApiService {
    @POST("api/destinos/{nombre}")
    suspend fun agregarDestino(@Path("nombre") nombre: String): Boolean

    @DELETE("api/destinos/{nombre}")
    suspend fun eliminarDestino(@Path("nombre") nombre: String): Boolean

    @GET("api/destinos/defecto")
    suspend fun obtenerDestinoDefecto(): Destino
}