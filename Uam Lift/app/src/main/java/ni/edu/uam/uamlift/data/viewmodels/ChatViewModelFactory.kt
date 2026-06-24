package ni.edu.uam.uamlift.data.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ni.edu.uam.uamlift.data.api.ChatApi
import ni.edu.uam.uamlift.data.api.UsuarioApiService

class ChatViewModelFactory(
    private val api: ChatApi,
    private val usuarioApi: UsuarioApiService
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(api, usuarioApi) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
