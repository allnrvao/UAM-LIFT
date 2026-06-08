package ni.edu.uamv.WebSocker.controllers;


import ni.edu.uamv.WebSocker.models.Ubicacion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;


@Controller
public class SeguimientoWebSocker {

    @MessageMapping("/ubicacion")
    @SendTo("/topic/test")
    public String actualizarUbicacion(String mensaje) {

        System.out.println("RECIBIDO");

        return mensaje;
    }
}
