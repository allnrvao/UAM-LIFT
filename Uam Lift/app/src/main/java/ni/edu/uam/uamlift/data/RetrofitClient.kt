package ni.edu.uam.uamlift.data

import android.util.Log
import ni.edu.uam.uamlift.data.api.DestinoApiService
import ni.edu.uam.uamlift.data.api.UsuarioApiService
import ni.edu.uam.uamlift.data.api.ViajeApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // 10.0.2.2 es la IP para acceder al localhost de tu PC desde el emulador de Android
<<<<<<< Updated upstream
    private const val BASE_URL = "http://10.162.164.217:8080/"
=======
    private const val BASE_URL = "http://192.168.0.5:8080/"

>>>>>>> Stashed changes

    // Configurar logging para ver todas las peticiones/respuestas
    private val loggingInterceptor = HttpLoggingInterceptor { message ->
        Log.d("OkHttp", message)
    }.apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
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
