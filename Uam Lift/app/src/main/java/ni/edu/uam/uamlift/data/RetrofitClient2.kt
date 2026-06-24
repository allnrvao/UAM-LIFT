package ni.edu.uam.uamlift.data

import android.util.Log
import ni.edu.uam.uamlift.data.api.ChatApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient2 {
    // Si usas EMULADOR, usa "10.0.2.2". Si usas dispositivo FÍSICO, usa la IP de tu PC.
    private const val HOST = "192.168.1.5" 
    private const val BASE_URL2 = "http://$HOST:8081/"

    val chatHost: String get() = HOST

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
            .baseUrl(BASE_URL2)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val chatApi: ChatApi by lazy {
        retrofit.create(ChatApi::class.java)
    }
}
