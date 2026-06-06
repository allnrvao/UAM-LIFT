package ni.edu.uam.UAM_LIFT.repositories;

import ni.edu.uam.UAM_LIFT.models.Usuario;

public interface InterfazUsuario {
    public Usuario findByCorreo(String correo);
    public Usuario findByCif(String cif);
    public Usuario findByNombreUsuario(String nombreUsuario);
}
