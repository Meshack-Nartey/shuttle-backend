package com.shuttlebackend.security;

import com.shuttlebackend.services.UserService;
import com.shuttlebackend.services.BlacklistedTokenService;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@AllArgsConstructor
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtHelper jwtHelper;
    private final UserService userService;
    private final BlacklistedTokenService blacklistedTokenService;


    private String extractToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");

        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer "))
            return bearer.substring(7);

        return null;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse resp, FilterChain chain)
            throws ServletException, IOException {

        String token = extractToken(req);

        if (token != null && jwtHelper.validateToken(token) && "access".equals(jwtHelper.getType(token))) {
            String jti = jwtHelper.getJti(token);

            // check blacklist
            if (blacklistedTokenService.findByJti(jti).isPresent()) {
                // token has been blacklisted -- do not authenticate
                chain.doFilter(req, resp);
                return;
            }

            String subject = jwtHelper.getSubject(token);

            UserDetails userDetails = userService.loadUserByUsername(subject);

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());

            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        chain.doFilter(req, resp);
    }
}
