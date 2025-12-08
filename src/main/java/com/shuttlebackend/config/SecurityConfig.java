package com.shuttlebackend.config;

import com.shuttlebackend.security.CustomAccessDeniedHandler;
import com.shuttlebackend.security.JwtAuthenticationEntryPoint;
import com.shuttlebackend.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(UserDetailsService userDetailsService) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            JwtAuthenticationEntryPoint authenticationEntryPoint,
            CustomAccessDeniedHandler accessDeniedHandler
    ) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(c -> c.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Permit websocket handshake endpoints (both native WS and SockJS) before authentication rules
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/ws", "/ws/**", "/ws-stomp", "/ws-stomp/**").permitAll()

                        // other public endpoints
                        .requestMatchers("/auth/login").permitAll()
                        .requestMatchers("/auth/signup/**").permitAll()
                        .requestMatchers("/schools/**").permitAll()
                        .requestMatchers("/routes/**").permitAll()
                        .requestMatchers("/shuttles/**").permitAll()

                        // ADMIN endpoints - only ADMIN role
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // everything else requires authentication
                        .anyRequest().authenticated()
                )

                // JWT filter remains in the chain, but handshake endpoints are permitted above.
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                .exceptionHandling(ex -> {
                    ex.authenticationEntryPoint(authenticationEntryPoint);
                    ex.accessDeniedHandler(accessDeniedHandler);
                });

        return http.build();
    }
}
