package com.shuttlebackend.websocket;

import com.shuttlebackend.security.JwtHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.Collections;
import java.util.List;

/**
 * Intercepts STOMP CONNECT messages and authenticates the user based on JWT passed in the CONNECT headers.
 */
@Component
@RequiredArgsConstructor
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtHelper jwtHelper;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {

            List<String> authHeaders = accessor.getNativeHeader("Authorization");
            String token = null;
            if (authHeaders != null && !authHeaders.isEmpty()) {
                token = authHeaders.get(0);
                if (token != null && token.startsWith("Bearer ")) token = token.substring(7);
            }

            if ((token == null || token.isBlank()) && accessor.getFirstNativeHeader("access_token") != null) {
                token = accessor.getFirstNativeHeader("access_token");
            }

            if (token == null || token.isBlank()) {
                return null; // reject connection
            }

            try {
                if (jwtHelper.validateToken(token) && "access".equals(jwtHelper.getType(token))) {
                    String email = jwtHelper.getSubject(token);
                    Authentication auth = new UsernamePasswordAuthenticationToken(email, null, Collections.emptyList());
                    SecurityContextHolder.getContext().setAuthentication(auth);

                    Principal principal = () -> email;
                    accessor.setUser(principal);
                } else {
                    return null;
                }
            } catch (Exception ex) {
                return null;
            }
        }
        return message;
    }
}

