package com.shuttlebackend.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "blacklisted_token", schema = "shuttle_backend_new")
public class BlacklistedToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "jti", nullable = false, unique = true)
    private String jti;

    @Column(name = "expires_at")
    private Instant expiresAt;

}

