package org.pandora.control.domain;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(myHandler1(), "/time_remaining1").setAllowedOrigins("*");
        registry.addHandler(myHandler2(), "/time_remaining2").setAllowedOrigins("*");
    }

    @Bean
    public WebSocketHandler myHandler1() {
        return new CustomWebSocketHandler();
    }

    @Bean
    public WebSocketHandler myHandler2() {
        return new CustomWebSocketHandler();
    }

}
