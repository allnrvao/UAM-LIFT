package ni.edu.uam.UAM_LIFT.controller;

import jakarta.validation.Valid;
import ni.edu.uam.UAM_LIFT.dto.EmailVerificationConfirm;
import ni.edu.uam.UAM_LIFT.dto.EmailVerificationRequest;
import ni.edu.uam.UAM_LIFT.models.Usuario;
import ni.edu.uam.UAM_LIFT.services.UsuarioServicio;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {
    private UsuarioServicio usuarioServicio;
    public UsuarioController(UsuarioServicio usuarioServicio) {
        this.usuarioServicio = usuarioServicio;
    }
    @PostMapping
    public boolean registrarUsuario(@RequestBody Usuario usuario) {
        return usuarioServicio.AddUsuario(usuario);
    }

    @PutMapping("/{cif}")
    public boolean actualizarUsuario(@PathVariable String cif, @RequestBody Usuario usuario) {
        Usuario usuarioExistente = usuarioServicio.findByCif(cif);
        if (usuarioExistente == null) {
            return false;
        }
        usuarioExistente.setNombre(usuario.getNombre());
        usuarioExistente.setCorreo(usuario.getCorreo());
        usuarioExistente.setNombreUsuario(usuario.getNombreUsuario());
        return usuarioServicio.UpdateUsuario(usuarioExistente);
    }

    @DeleteMapping("/{cif}")
    public boolean eliminarUsuario(@PathVariable String cif) {
        Usuario usuarioExistente = usuarioServicio.findByCif(cif);
        if (usuarioExistente == null) {
            return false;
        }
        return usuarioServicio.DeleteUsuario(usuarioExistente);
    }

    @PostMapping("/verificacion/solicitar")
    public boolean solicitarVerificacion(@Valid @RequestBody EmailVerificationRequest request) {
        return usuarioServicio.solicitarVerificacionCorreo(request.getCorreo());
    }

    @PostMapping("/verificacion/confirmar")
    public boolean confirmarVerificacion(@Valid @RequestBody EmailVerificationConfirm request) {
        return usuarioServicio.confirmarVerificacionCorreo(request.getCorreo(), request.getCode());
    }

}
