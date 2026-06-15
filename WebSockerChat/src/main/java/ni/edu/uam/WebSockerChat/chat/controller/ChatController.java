package ni.edu.uam.WebSockerChat.chat.controller;

import lombok.RequiredArgsConstructor;
import ni.edu.uam.WebSockerChat.chat.dto.MensajeResponse;
import ni.edu.uam.WebSockerChat.chat.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/historial/{viajeId}")
    public ResponseEntity<List<MensajeResponse>> obtenerHistorialViaje(@PathVariable Long viajeId) {
        return ResponseEntity.ok(chatService.obtenerHistorial(viajeId));
    }
}