package ni.edu.uam.uamlift.webSocketApplication.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ni.edu.uam.uamlift.webSocketApplication.model.Mensaje
import ni.edu.uam.uamlift.webSocketApplication.repository.ChatRepository

class ChatViewModel : ViewModel() {

    private val repository = ChatRepository()

    private val _mensajes =
        MutableStateFlow<List<Mensaje>>(emptyList())

    val mensajes: StateFlow<List<Mensaje>>
        get() = _mensajes

    init {

        repository.conectar()

        repository.suscribirse("chat1")

        viewModelScope.launch {

            repository.mensajes.collect { mensaje ->

                _mensajes.value += mensaje
            }
        }
    }

    fun enviarMensaje(texto: String) {

        val mensaje = Mensaje(
            contenido = texto,
            remitente = "Allan",
            chatId = "chat1"
        )

        repository.enviarMensaje(mensaje)
    }

    override fun onCleared() {
        super.onCleared()
        repository.desconectar()
    }
}