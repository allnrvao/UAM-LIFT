package ni.edu.uam.uamlift.data.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ni.edu.uam.uamlift.data.DependencyContainer
import ni.edu.uam.uamlift.data.RetrofitClient

class AppViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(UbicacionViewModel::class.java) -> {
                UbicacionViewModel(DependencyContainer.viajeRepository) as T
            }
            modelClass.isAssignableFrom(ViajeViewModel::class.java) -> {
                ViajeViewModel(RetrofitClient.viajeApi) as T
            }
            modelClass.isAssignableFrom(UsuarioViewModel::class.java) -> {
                UsuarioViewModel() as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
