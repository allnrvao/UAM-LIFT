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

    private final UsuarioServicio usuarioServicio;

    public UsuarioController(UsuarioServicio usuarioServicio) {
        this.usuarioServicio = usuarioServicio;
    }

    @PostMapping
    public boolean registrarUsuario(@RequestBody Usuario usuario) {
        return usuarioServicio.AddUsuario(usuario);
    }

    @GetMapping("/cif/{cif}")
    public Usuario obtenerPorCif(@PathVariable String cif) {
        return usuarioServicio.findByCif(cif);
    }

    @GetMapping("/correo/{correo}")
    public Usuario obtenerPorCorreo(@PathVariable String correo) {
        return usuarioServicio.findByCorreo(correo);
    }

    @GetMapping("/correoBol/{correo}")
    public boolean verificarCorreo(@PathVariable String correo) {
        try {
            usuarioServicio.findByCorreo(correo);
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    @GetMapping("/nombreUsuario/{nombreUsuario}")
    public Usuario obtenerPorNombreUsuario(
            @PathVariable String nombreUsuario) {

        return usuarioServicio.findByNombreUsuario(nombreUsuario);
    }

    @PutMapping("/{cif}")
    public boolean actualizarUsuario(
            @PathVariable String cif,
            @RequestBody Usuario usuario) {

        Usuario usuarioExistente = usuarioServicio.findByCif(cif);

        usuarioExistente.setNombre(usuario.getNombre());
        usuarioExistente.setApellido(usuario.getApellido());
        usuarioExistente.setCorreo(usuario.getCorreo());
        usuarioExistente.setNombreUsuario(usuario.getNombreUsuario());
        usuarioExistente.setImagenUrl(usuario.getImagenUrl());

        return usuarioServicio.UpdateUsuario(usuarioExistente);
    }

    @DeleteMapping("/{cif}")
    public boolean eliminarUsuario(@PathVariable String cif) {

        Usuario usuarioExistente = usuarioServicio.findByCif(cif);

        return usuarioServicio.DeleteUsuario(usuarioExistente);
    }

    @PostMapping("/verificacion/solicitar")
    public boolean solicitarVerificacion(
            @Valid @RequestBody EmailVerificationRequest request) {

        return usuarioServicio.solicitarVerificacionCorreo(
                request.getCorreo());
    }

    @PostMapping("/verificacion/confirmar")
    public boolean confirmarVerificacion(
            @Valid @RequestBody EmailVerificationConfirm request) {

        return usuarioServicio.confirmarVerificacionCorreo(
                request.getCorreo(),
                request.getCode());
    }
}