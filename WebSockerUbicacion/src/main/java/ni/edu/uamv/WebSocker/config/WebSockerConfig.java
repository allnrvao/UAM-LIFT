package ni.edu.uamv.WebSocker.config;

import ni.edu.uamv.WebSocker.handler.ViajeHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;


@Configuration
@EnableWebSocket
public class    WebSockerConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new ViajeHandler(), "/ws/viaje").setAllowedOrigins("*");
    }
}
