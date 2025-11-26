package com.shuttlebackend.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "trip_activity", schema = "shuttle_backend_new")
public class TripActivity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trip_id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shuttle_id", nullable = false)
    private Shuttle shuttle;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "departure_stop_id", nullable = false)
    private RouteStop departureStop;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "arrival_stop_id", nullable = false)
    private RouteStop arrivalStop;

    @Column(name = "route_id")
    private Integer routeId;

    @Column(name = "estimated_time")
    private Instant estimatedTime;

    @Column(name = "actual_time")
    private Instant actualTime;

    @Size(max = 20)
    @ColumnDefault("'Upcoming'")
    @Column(name = "status", length = 20)
    private String status;

}