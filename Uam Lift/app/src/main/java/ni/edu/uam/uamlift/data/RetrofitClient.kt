package ni.edu.uam.uamlift.data

import ni.edu.uam.uamlift.data.api.DestinoApiService
import ni.edu.uam.uamlift.data.api.UsuarioApiService
import ni.edu.uam.uamlift.data.api.ViajeApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // 10.0.2.2 es la IP para acceder al localhost de tu PC desde el emulador de Android
    private const val BASE_URL = "http://10.183.141.217:8080/"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val usuarioApi: UsuarioApiService by lazy {
        retrofit.create(UsuarioApiService::class.java)
    }

    val destinoApi: DestinoApiService by lazy {
        retrofit.create(DestinoApiService::class.java)
    }

    val viajeApi: ViajeApiService by lazy {
        retrofit.create(ViajeApiService::class.java)
    }
}
