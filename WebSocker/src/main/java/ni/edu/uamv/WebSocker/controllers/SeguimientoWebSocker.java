package ni.edu.uamv.WebSocker.controllers;


import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;


@Controller
public class SeguimientoWebSocker {

    @MessageMapping("/ubicacion")
    @SendTo("/topic/test")
    public String actualizarUbicacion(String mensaje) {
        return mensaje;
    }
}
