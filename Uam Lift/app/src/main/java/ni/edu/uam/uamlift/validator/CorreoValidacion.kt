package ni.edu.uam.uamlift.validator



class CorreoValidacion(private val dominio: String ="@uamv.edu.ni") {

    fun validarEntrada(correo: String): Boolean {
        if (correo.isNullOrBlank()) {
            return false
        }
        return correo.endsWith(dominio, ignoreCase = true)

    }

}