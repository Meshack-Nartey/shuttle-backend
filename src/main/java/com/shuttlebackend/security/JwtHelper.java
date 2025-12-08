package com.shuttlebackend.security;

import io.github.cdimascio.dotenv.Dotenv;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtHelper {

    private final Key key;
    private final long accessExpirationMs;
    private final long refreshExpirationMs;

    public JwtHelper(Dotenv dotenv) {

        String secret = dotenv.get("JWT_SECRET");
        if (secret == null || secret.isEmpty()) {
            throw new IllegalStateException("JWT_SECRET is missing in .env file");
        }

        // Default access = 15 minutes
        this.accessExpirationMs = Long.parseLong(dotenv.get("JWT_EXPIRATION", String.valueOf(15 * 60 * 1000L)));
        // Default refresh = 30 days
        this.refreshExpirationMs = Long.parseLong(dotenv.get("JWT_REFRESH_EXPIRATION", String.valueOf(30L * 24 * 60 * 60 * 1000L)));

        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    private String buildToken(String subject, long expirationMs, String type, String role) {
        long now = System.currentTimeMillis();
        String jti = UUID.randomUUID().toString();

        JwtBuilder b = Jwts.builder()
                .setId(jti)
                .setSubject(subject)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + expirationMs))
                .signWith(key, SignatureAlgorithm.HS512)
                .claim("type", type);

        if (role != null) {
            b.claim("role", role);
        }

        return b.compact();
    }

    public String generateAccessToken(String subject, String role) {
        return buildToken(subject, accessExpirationMs, "access", role);
    }

    public String generateRefreshToken(String subject) {
        return buildToken(subject, refreshExpirationMs, "refresh", null);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public String getSubject(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public String getType(String token) {
        try {
            Claims c = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            Object t = c.get("type");
            return t != null ? String.valueOf(t) : null;
        } catch (Exception ex) {
            return null;
        }
    }

    public String getRole(String token) {
        try {
            Claims c = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            Object r = c.get("role");
            return r != null ? String.valueOf(r) : null;
        } catch (Exception ex) {
            return null;
        }
    }

    public String getJti(String token) {
        try {
            Claims c = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return c.getId();
        } catch (Exception ex) {
            return null;
        }
    }

    public long getExpirationMillis(String token) {
        try {
            Claims c = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            Date exp = c.getExpiration();
            return exp != null ? exp.getTime() : 0L;
        } catch (Exception ex) {
            return 0L;
        }
    }
}
