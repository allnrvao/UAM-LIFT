package ni.edu.uam.uamlift.data.api

import ni.edu.uam.uamlift.data.models.Carro
import retrofit2.Response
import retrofit2.http.*

interface CarroApiService {

    @GET("api/carros")
    suspend fun getAllCarros(): Response<List<Carro>>

    @GET("api/carros/{id}")
    suspend fun getCarroById(
        @Path("id") id: Long
    ): Response<Carro>

    @POST("api/carros")
    suspend fun createCarro(
        @Body carro: Carro
    ): Response<Carro>

    @PUT("api/carros/{id}")
    suspend fun updateCarro(
        @Path("id") id: Long,
        @Body carroDetails: Carro
    ): Response<Carro>

    @DELETE("api/carros/{id}")
    suspend fun deleteCarro(
        @Path("id") id: Long
    ): Response<Void>

    @GET("api/carros/usuario/{usuarioId}")
    suspend fun getCarrosByUsuarioId(
        @Path("usuarioId") usuarioId: Long
    ): Response<List<Carro>>

    @GET("api/carros/placa/{placa}")
    suspend fun obtenerPorPlaca(
        @Path("placa") placa: String
    ): Response<Carro>
}
