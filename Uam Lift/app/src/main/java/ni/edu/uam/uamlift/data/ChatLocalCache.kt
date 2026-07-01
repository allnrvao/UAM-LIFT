package ni.edu.uam.uamlift.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ni.edu.uam.uamlift.data.viewmodels.MensajeUI

/**
 * Guarda en SharedPreferences (persistente entre reinicios de la app) los últimos
 * mensajes de cada chat de viaje. Así, al volver a entrar a un chat, se pueden
 * mostrar de inmediato -aunque todavía no haya respuesta del backend o no haya
 * conexión- al menos los últimos [MAX_MENSAJES_GUARDADOS] mensajes que se tuvieron.
 */
object ChatLocalCache {

    private const val PREFS_NAME = "chat_cache_prefs"
    private const val KEY_PREFIX = "mensajes_viaje_"
    const val MAX_MENSAJES_GUARDADOS = 5

    private val gson = Gson()

    fun obtenerUltimos(context: Context, viajeId: Long): List<MensajeUI> {
        return try {
            val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val json = prefs.getString(KEY_PREFIX + viajeId, null) ?: return emptyList()
            val tipo = object : TypeToken<List<MensajeUI>>() {}.type
            gson.fromJson<List<MensajeUI>>(json, tipo) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun guardarUltimos(context: Context, viajeId: Long, mensajes: List<MensajeUI>) {
        try {
            val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val ultimos = mensajes.takeLast(MAX_MENSAJES_GUARDADOS)
            prefs.edit().putString(KEY_PREFIX + viajeId, gson.toJson(ultimos)).apply()
        } catch (e: Exception) {
            // Si falla el guardado local no interrumpimos el chat, simplemente no
            // habrá historial local disponible la próxima vez.
        }
    }
}