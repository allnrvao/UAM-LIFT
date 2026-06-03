package ni.edu.uam.UamLiftWebSocket.controller;

import ni.edu.uam.UamLiftWebSocket.model.Mensaje;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class MessageController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/envio/{chatId}")
    public void enviarMensaje(
            @DestinationVariable String chatId,
            Mensaje mensaje) {

        messagingTemplate.convertAndSend(
                "/tema/" + chatId,
                mensaje
        );
    }
}