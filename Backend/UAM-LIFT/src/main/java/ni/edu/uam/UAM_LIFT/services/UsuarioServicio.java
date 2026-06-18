package ni.edu.uam.UAM_LIFT.services;

import ni.edu.uam.UAM_LIFT.models.Usuario;
import ni.edu.uam.UAM_LIFT.repositories.InterfazUsuario;
import ni.edu.uam.UAM_LIFT.repositories.RepoUsuario;
import org.springframework.stereotype.Service;

@Service
public class UsuarioServicio implements InterfazUsuario {
    private final RepoUsuario repoUsuario;
    private final EmailVerificationService emailVerificationService;

    public UsuarioServicio(RepoUsuario repoUsuario, EmailVerificationService emailVerificationService) {
        this.repoUsuario = repoUsuario;
        this.emailVerificationService = emailVerificationService;
    }

    @Override
    public Usuario findByCorreo(String correo) {
        return repoUsuario.findByCorreo(correo).orElseThrow(()->new RuntimeException("Usuario no encontrado con correo: " + correo));
    }

    @Override
    public Usuario findByCif(String cif) {
        return repoUsuario.findByCif(cif).orElseThrow(()->new RuntimeException("Usuario no encontrado con CIF: " + cif));
    }
    @Override
    public Usuario findByNombreUsuario(String nombreUsuario) {
        return repoUsuario.findByNombreUsuario(nombreUsuario).orElseThrow(()->new RuntimeException("Usuario no encontrado con nombre de usuario: " + nombreUsuario));
    }

    public boolean AddUsuario(Usuario usuario) {
        try {
            if (usuario.getCorreo() == null || !usuario.getCorreo().endsWith("@uamv.edu.ni")) {
                return false;
            }
            repoUsuario.save(usuario);
        }catch(Exception e){
            System.out.println("Error al agregar usuario: " + e.getMessage());
            return false;
        }
        return true;
    }

    public boolean UpdateUsuario(Usuario usuario) {
        try {
            repoUsuario.save(usuario);
        }catch(Exception e){
            System.out.println("Error al actualizar usuario: " + e.getMessage());
            return false;
        }
        return true;
    }

    public boolean DeleteUsuario(Usuario usuario) {
        try {
            repoUsuario.delete(usuario);
        }catch(Exception e){
            System.out.println("Error al eliminar usuario: " + e.getMessage());
            return false;
        }
        return true;
    }

    public boolean solicitarVerificacionCorreo(String correo) {
        return emailVerificationService.requestVerification(correo);
    }

    public boolean confirmarVerificacionCorreo(String correo, String code) {
        return emailVerificationService.confirmVerification(correo, code);
    }
}
