package ni.edu.uam.UAM_LIFT.websocket.controller;

import ni.edu.uam.UAM_LIFT.websocket.dto.ChatMessage;
import ni.edu.uam.UAM_LIFT.websocket.dto.LocationUpdate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class RealTimeController {

    private final SimpMessagingTemplate messagingTemplate;

    public RealTimeController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/viaje.update")
    public void updateLocation(@Payload LocationUpdate update) {
        String topic = "/topic/viaje." + update.getId();
        messagingTemplate.convertAndSend(topic, update);
    }

    @MessageMapping("/chat.send")
    public void sendPrivateMessage(@Payload ChatMessage message) {
        String queue = "/queue/chat." + message.getReceptorId();
        messagingTemplate.convertAndSend(queue, message);
    }
}
