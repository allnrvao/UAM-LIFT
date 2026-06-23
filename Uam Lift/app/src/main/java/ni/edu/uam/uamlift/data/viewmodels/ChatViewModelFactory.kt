package ni.edu.uam.uamlift.data.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ni.edu.uam.uamlift.data.api.ChatApi

class ChatViewModelFactory(private val api: ChatApi) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(api) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
