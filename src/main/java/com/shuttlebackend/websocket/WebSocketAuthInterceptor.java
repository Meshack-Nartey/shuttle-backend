package com.shuttlebackend.websocket;

import com.shuttlebackend.security.JwtHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.server.HandshakeInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    private final JwtHelper jwtHelper;

    @Override
    public boolean beforeHandshake(org.springframework.http.server.ServerHttpRequest request,
                                   org.springframework.http.server.ServerHttpResponse response,
                                   org.springframework.web.socket.WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) throws Exception {

        if (request instanceof ServletServerHttpRequest servletReq) {
            HttpServletRequest httpReq = servletReq.getServletRequest();
            String token = httpReq.getParameter("access_token");
            if (token == null) {
                String bearer = httpReq.getHeader("Authorization");
                if (bearer != null && bearer.startsWith("Bearer ")) token = bearer.substring(7);
            }

            if (token != null && jwtHelper.validateToken(token) && "access".equals(jwtHelper.getType(token))) {
                String email = jwtHelper.getSubject(token);
                attributes.put("email", email);
                attributes.put("tokenJti", jwtHelper.getJti(token));
                return true;
            }
        }

        return false;
    }

    @Override
    public void afterHandshake(org.springframework.http.server.ServerHttpRequest request,
                               org.springframework.http.server.ServerHttpResponse response,
                               org.springframework.web.socket.WebSocketHandler wsHandler,
                               Exception exception) {
    }
}
