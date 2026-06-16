package ni.edu.uam.uamlift.validator

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

class SesionGoogle: ViewModel() {
    private val correoValidacion = CorreoValidacion()

    fun iniciarSesion(context: Context, onResultado: (String?) -> Unit) {
        val credentialManager = CredentialManager.create(context)

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId("")
            .build()
            
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        viewModelScope.launch {
            try {
                val result = credentialManager.getCredential(context = context, request = request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val correoUsuario = googleIdTokenCredential.id
                    if (correoValidacion.validarEntrada(correoUsuario)) {
                        onResultado(correoUsuario)
                    } else {
                        onResultado(null)
                    }
                } else {
                    onResultado(null)
                }
            }
            catch (e: Exception) {
                e.printStackTrace()
                onResultado(null)
            }
        }
    }
}
