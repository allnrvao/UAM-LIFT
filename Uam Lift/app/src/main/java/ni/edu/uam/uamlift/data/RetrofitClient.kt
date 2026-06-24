package ni.edu.uam.uamlift.data

import android.util.Log
import ni.edu.uam.uamlift.data.api.CarroApiService
import ni.edu.uam.uamlift.data.api.ChatApi
import ni.edu.uam.uamlift.data.api.DestinoApiService
import ni.edu.uam.uamlift.data.api.UsuarioApiService
import ni.edu.uam.uamlift.data.api.ViajeApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "http://192.168.0.5:8080/"


    // Configurar logging para ver todas las peticiones/respuestas
    private val loggingInterceptor = HttpLoggingInterceptor { message ->
        Log.d("OkHttp", message)
    }.apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()


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

    val carroApi: CarroApiService by lazy {
        retrofit.create(CarroApiService::class.java)
    }

}
