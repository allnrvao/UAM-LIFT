package ni.edu.uam.uamlift.data.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ni.edu.uam.uamlift.data.ChatWebSocketManager
import ni.edu.uam.uamlift.data.api.ChatApi
import ni.edu.uam.uamlift.data.dto.MensajeResponse

class ChatViewModel(
    private val api: ChatApi
) : ViewModel() {

    private val ws = ChatWebSocketManager()

    private val _mensajes =
        MutableStateFlow<List<MensajeResponse>>(emptyList())

    val mensajes = _mensajes.asStateFlow()

    fun iniciarChat(
        viajeId: Long
    ) {

        viewModelScope.launch {

            try {

                val historial =
                    api.obtenerHistorial(viajeId)

                _mensajes.value = historial

                ws.conectar(viajeId)

                ws.mensajes.collect { nuevo ->

                    _mensajes.update {
                        it + nuevo
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun enviarMensaje(
        viajeId: Long,
        usuarioId: Long,
        texto: String
    ) {

        ws.enviarMensaje(
            viajeId,
            usuarioId,
            texto
        )
    }

    override fun onCleared() {
        ws.desconectar()
        super.onCleared()
    }
}