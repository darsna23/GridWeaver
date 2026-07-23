package com.gridweaver.config;

import com.gridweaver.websocket.GridWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final GridWebSocketHandler gridWebSocketHandler;

    @Autowired
    public WebSocketConfig(GridWebSocketHandler gridWebSocketHandler) {
        this.gridWebSocketHandler = gridWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(gridWebSocketHandler, "/ws/grid")
                .setAllowedOriginPatterns("*"); // dev only - restrict in production
    }
}
