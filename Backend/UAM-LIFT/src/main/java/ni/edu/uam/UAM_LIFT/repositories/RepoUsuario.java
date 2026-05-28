package ni.edu.uam.UAM_LIFT.repositories;

import ni.edu.uam.UAM_LIFT.models.Usuario;

public interface RepoUsuario extends RepoGeneral {
     public boolean AddUsuario(Usuario usuario);
     public boolean UpdateUsuario(Usuario usuario);
     public boolean DeleteUsuario(Usuario usuario);
     public Usuario findByUsername(String username);
     public Usuario findByEmail(String email);
}
