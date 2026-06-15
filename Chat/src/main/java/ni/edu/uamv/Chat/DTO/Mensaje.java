package ni.edu.uamv.Chat.DTO;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Mensaje {
    private Long id;
    private Long chatId;
    private Long usuarioId;
    private String contenido;
}
