package ni.edu.uam.UAM_LIFT.services;

import ni.edu.uam.UAM_LIFT.dto.EstadisticasUsuarioDTO;
import ni.edu.uam.UAM_LIFT.enums.EstadoViaje;
import ni.edu.uam.UAM_LIFT.enums.EstadoViajeUsuario;
import ni.edu.uam.UAM_LIFT.models.Destino;
import ni.edu.uam.UAM_LIFT.models.Usuario;
import ni.edu.uam.UAM_LIFT.models.Viaje;
import ni.edu.uam.UAM_LIFT.models.ViajeUsuario;
import ni.edu.uam.UAM_LIFT.repositories.InterfazUsuario;
import ni.edu.uam.UAM_LIFT.repositories.RepoUsuario;
import ni.edu.uam.UAM_LIFT.repositories.RepoViaje;
import ni.edu.uam.UAM_LIFT.repositories.RepoViajeUsuario;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class UsuarioServicio implements InterfazUsuario {

    private final RepoUsuario        repoUsuario;
    private final EmailVerificationService emailVerificationService;
    private final RepoViaje          repoViaje;
    private final RepoViajeUsuario   repoViajeUsuario;

    public UsuarioServicio(
            RepoUsuario repoUsuario,
            EmailVerificationService emailVerificationService,
            RepoViaje repoViaje,
            RepoViajeUsuario repoViajeUsuario
    ) {
        this.repoUsuario             = repoUsuario;
        this.emailVerificationService = emailVerificationService;
        this.repoViaje               = repoViaje;
        this.repoViajeUsuario        = repoViajeUsuario;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CRUD básico
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public Usuario findByCorreo(String correo) {
        return repoUsuario.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con correo: " + correo));
    }

    @Override
    public Usuario findByCif(String cif) {
        return repoUsuario.findByCif(cif)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con CIF: " + cif));
    }

    @Override
    public Usuario findByNombreUsuario(String nombreUsuario) {
        return repoUsuario.findByNombreUsuario(nombreUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con nombre de usuario: " + nombreUsuario));
    }

    public boolean AddUsuario(Usuario usuario) {
        try {
            if (usuario.getCorreo() == null || !usuario.getCorreo().endsWith("@uamv.edu.ni")) {
                return false;
            }
            repoUsuario.save(usuario);
        } catch (Exception e) {
            System.out.println("Error al agregar usuario: " + e.getMessage());
            return false;
        }
        return true;
    }

    public boolean updateUsuario(Usuario usuario, MultipartFile imagen) {
        try {

            if (imagen != null && !imagen.isEmpty()) {

                // Crear carpeta si no existe
                Path carpeta = Paths.get("imagesProfiles");
                Files.createDirectories(carpeta);

                // Obtener extensión
                String nombreOriginal = imagen.getOriginalFilename();
                String extension = nombreOriginal.substring(nombreOriginal.lastIndexOf("."));

                // Nombre único
                String nombreArchivo = UUID.randomUUID() + extension;

                // Guardar imagen
                Path ruta = carpeta.resolve(nombreArchivo);
                Files.copy(imagen.getInputStream(), ruta, StandardCopyOption.REPLACE_EXISTING);

                // Guardar URL en la BD
                usuario.setImagenUrl("/imagesProfiles/" + nombreArchivo);
            }
            repoUsuario.save(usuario);
            return true;

        } catch (Exception e) {
            System.out.println("Error al actualizar usuario: " + e.getMessage());
            return false;
        }
    }

    public boolean DeleteUsuario(Usuario usuario) {
        try {
            repoUsuario.delete(usuario);
        } catch (Exception e) {
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

    // ─────────────────────────────────────────────────────────────────────────
    // ESTADÍSTICAS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Calcula y devuelve las estadísticas del usuario:
     *
     *  VIAJES CONTABILIZADOS
     *  ┌─ Como conductor : viajes con estado EN_CURSO o FINALIZADO
     *  └─ Como pasajero  : registros ViajeUsuario con estado ACEPTADO
     *     (sin duplicar viajes ya contados como conductor)
     *
     *  KM TOTALES
     *  Suma de la distancia Haversine (origen → destino) de cada viaje
     *  contabilizado que tenga coordenadas válidas.
     *
     *  CO₂ AHORRADO
     *  Por cada viaje calculamos cuánto CO₂ habría emitido CADA pasajero
     *  si hubiese viajado solo. El ahorro es ese total menos el CO₂ de
     *  1 solo vehículo (el que realmente se usó).
     *      co2_viaje = emision_1_auto * (nPasajeros + 1) - emision_1_auto
     *                = emision_1_auto * nPasajeros
     *  Suma de todos los viajes → co2Ahorrado total.
     */
    public EstadisticasUsuarioDTO calcularEstadisticas(Long usuarioId) {

        Usuario usuario = repoUsuario.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + usuarioId));

        // ── 1. Viajes como CONDUCTOR que llegaron a ejecutarse ──────────────
        List<Viaje> viajesConductor = repoViaje.findByConductor(usuario)
                .stream()
                .filter(v -> v.getEstadoViaje() == EstadoViaje.EN_CURSO
                        || v.getEstadoViaje() == EstadoViaje.FINALIZADO)
                .toList();

        // ── 2. Viajes como PASAJERO ACEPTADO ────────────────────────────────
        List<ViajeUsuario> registrosPasajero =
                repoViajeUsuario.findByUsuarioIdAndEstado(usuarioId, EstadoViajeUsuario.ACEPTADO);

        // Usamos un Set para no contar dos veces el mismo viajeId
        Set<Long> idsContados = new HashSet<>();
        List<Viaje> viajesContabilizados = new ArrayList<>();

        for (Viaje v : viajesConductor) {
            if (idsContados.add(v.getId())) {
                viajesContabilizados.add(v);
            }
        }
        for (ViajeUsuario vu : registrosPasajero) {
            Viaje v = vu.getViaje();
            // Solo contamos viajes que realmente se iniciaron o finalizaron
            if (v.getEstadoViaje() == EstadoViaje.EN_CURSO
                    || v.getEstadoViaje() == EstadoViaje.FINALIZADO) {
                if (idsContados.add(v.getId())) {
                    viajesContabilizados.add(v);
                }
            }
        }

        int totalViajes = viajesContabilizados.size();

        double kmTotales  = 0.0;
        double co2Ahorrado = 0.0;

        for (Viaje v : viajesContabilizados) {
            Destino origen  = v.getOrigen();
            Destino destino = v.getDestino();

            // Solo calculamos si ambos destinos tienen coordenadas
            if (origen  != null && origen.getLatitud()  != null && origen.getLongitud()  != null
                    && destino != null && destino.getLatitud() != null && destino.getLongitud() != null) {

                double km = haversine(
                        origen.getLatitud(),  origen.getLongitud(),
                        destino.getLatitud(), destino.getLongitud()
                );
                kmTotales += km;

                // Número de pasajeros ACEPTADOS en este viaje (sin contar al conductor)
                long nPasajeros = repoViajeUsuario
                        .countByViajeIdAndEstado(v.getId(), EstadoViajeUsuario.ACEPTADO);

                // Tipo de vehículo (usamos la marca como heurística simple)
                String marca = v.getCarro() != null
                        ? v.getCarro().getMarca().toLowerCase()
                        : "";
                double emision1Auto = co2PorKm(marca);   // g/km → lo pasamos a kg abajo

                // CO₂ que habría emitido cada persona si viajara sola (todos = nPasajeros + conductor)
                // Ahorro = emisiones de (nPasajeros + 1) autos - emisiones de 1 auto
                //        = emision1Auto * nPasajeros
                // Resultado en kg
                double co2ViajeSolo = (km * emision1Auto * nPasajeros) / 1000.0;
                co2Ahorrado += co2ViajeSolo;
            }
        }

        return new EstadisticasUsuarioDTO(totalViajes, kmTotales, co2Ahorrado);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Fórmula de Haversine — distancia entre dos puntos GPS en km
    // ─────────────────────────────────────────────────────────────────────────
    private static double haversine(double lat1, double lon1,
                                    double lat2, double lon2) {
        final double R = 6371.0; // Radio de la Tierra en km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Factor de emisión en gramos de CO₂ por km según la marca/tipo de vehículo
    // ─────────────────────────────────────────────────────────────────────────
    private static double co2PorKm(String marca) {
        // Marcas típicamente asociadas a SUV
        if (marca.contains("toyota") || marca.contains("ford") || marca.contains("chevrolet")
                || marca.contains("jeep") || marca.contains("nissan") || marca.contains("kia")
                || marca.contains("hyundai")) {
            return 180.0; // SUV/pickup promedio
        }
        // Híbridos conocidos
        if (marca.contains("prius") || marca.contains("hibrido") || marca.contains("hybrid")) {
            return 80.0;
        }
        // Compactos comunes
        if (marca.contains("honda") || marca.contains("suzuki") || marca.contains("mazda")) {
            return 120.0;
        }
        // Por defecto: sedán genérico
        return 150.0;
    }
}