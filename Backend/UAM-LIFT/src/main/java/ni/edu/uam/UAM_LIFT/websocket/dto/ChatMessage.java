package ni.edu.uam.UAM_LIFT.websocket.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    private Long emisorId;
    private Long receptorId;
    private String contenido;
    private Long timestamp;
}
