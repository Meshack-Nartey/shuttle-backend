package com.shuttlebackend.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "refresh_token", schema = "shuttle_backend_new")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "jti", nullable = false, unique = true)
    private String jti;

    @Column(name = "user_email", nullable = false)
    private String userEmail;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "revoked")
    private Boolean revoked = false;

}

