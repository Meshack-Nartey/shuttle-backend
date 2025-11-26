package com.shuttlebackend.config;

import com.shuttlebackend.security.JwtHelper;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.security.Principal;
import java.util.Collections;
import java.util.List;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketBrokerConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtHelper jwtHelper;

    @Override
    public void configureMessageBroker(org.springframework.messaging.simp.config.MessageBrokerRegistry config) {
        // enable simple in-memory broker for topics and queues
        config.enableSimpleBroker("/topic", "/queue");
        // prefix for messages bound for @MessageMapping handlers (if any)
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // STOMP endpoint for clients (students) to connect via SockJS
        registry.addEndpoint("/ws-stomp").setAllowedOrigins("*").withSockJS();
    }

    // Intercept inbound STOMP messages to validate CONNECT frame tokens
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                    // try Authorization header first
                    List<String> authHeaders = accessor.getNativeHeader("Authorization");
                    String token = null;
                    if (authHeaders != null && !authHeaders.isEmpty()) {
                        token = authHeaders.get(0);
                        if (token != null && token.startsWith("Bearer ")) {
                            token = token.substring(7);
                        }
                    }

                    // fallback to access_token native header
                    if ((token == null || token.isBlank()) && accessor.getFirstNativeHeader("access_token") != null) {
                        token = accessor.getFirstNativeHeader("access_token");
                    }

                    // If no token provided, reject CONNECT
                    if (token == null || token.isBlank()) {
                        return null; // reject connection
                    }

                    // validate token and set principal; reject if invalid
                    try {
                        if (jwtHelper.validateToken(token) && "access".equals(jwtHelper.getType(token))) {
                            String email = jwtHelper.getSubject(token);
                            Principal principal = new Principal() {
                                @Override
                                public String getName() { return email; }
                            };
                            Authentication auth = new UsernamePasswordAuthenticationToken(email, null, Collections.emptyList());
                            accessor.setUser(principal);
                            SecurityContextHolder.getContext().setAuthentication(auth);
                        } else {
                            return null; // invalid token -> reject connection
                        }
                    } catch (Exception ex) {
                        return null; // invalid token -> reject connection
                    }
                }
                return message;
            }
        });
    }
}
