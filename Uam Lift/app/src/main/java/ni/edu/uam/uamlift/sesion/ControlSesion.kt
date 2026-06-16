package ni.edu.uam.uamlift.sesion

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


val Context.dataStore by preferencesDataStore(name = "ControlSesion")
class ControlSesion (private val context: Context){
    companion object{
        val EstaLogeado = booleanPreferencesKey("estaLogeado")
        val correoInstitucional = stringPreferencesKey("correoInstitucional")
        val nombreUsuario = stringPreferencesKey("nombreUsuario")
        val cif = stringPreferencesKey("cif")
    }

    val estarLogeado: Flow<Boolean> = context.dataStore.data.map { preferencias ->
        preferencias[EstaLogeado] == true
    }

    val obtenerCorreoInstitucional: Flow<String> = context.dataStore.data.map { preferencias ->
        preferencias[correoInstitucional] ?: ""
    }

    val obtenerNombreUsuario: Flow<String> = context.dataStore.data.map { preferencias ->
        preferencias[nombreUsuario] ?: ""
    }

    val obtenerCif: Flow<String> = context.dataStore.data.map { preferencias ->
        preferencias[cif] ?: ""
    }

    suspend fun guardarSesion(estaLogeado: Boolean, correo: String, nombre: String, cifUsuario: String) {
        context.dataStore.edit { preferencias ->
            preferencias[EstaLogeado] = estaLogeado
            preferencias[correoInstitucional] = correo
            preferencias[nombreUsuario] = nombre
            preferencias[cif] = cifUsuario
        }
    }

    suspend fun cerrarSesion() {
        context.dataStore.edit { preferencias ->
            preferencias[EstaLogeado] = false
            preferencias[correoInstitucional] = ""
            preferencias[nombreUsuario] = ""
            preferencias[cif] = ""
        }
    }
}