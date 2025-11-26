package com.shuttlebackend.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "school", schema = "shuttle_backend_new")
public class School {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "school_id", nullable = false)
    private Integer id;

    @Size(max = 100)
    @NotNull
    @Column(name = "school_name", nullable = false, length = 100)
    private String schoolName;

    @Column(name = "map_center_lat", precision = 10, scale = 8)
    private BigDecimal mapCenterLat;

    @Column(name = "map_center_lon", precision = 11, scale = 8)
    private BigDecimal mapCenterLon;

    @Lob
    @Column(name = "map_image_url")
    private String mapImageUrl;

    @Column(name = "created_at")
    private Instant createdAt;

    @OneToMany(mappedBy = "school")
    @JsonIgnore
    private Set<Student> students;

    @OneToMany(mappedBy = "school")
    @JsonIgnore
    private Set<Driver> drivers;

    @OneToMany(mappedBy = "school")
    @JsonIgnore
    private Set<Route> routes;

    @OneToMany(mappedBy = "school")
    @JsonIgnore
    private Set<Shuttle> shuttles;


}