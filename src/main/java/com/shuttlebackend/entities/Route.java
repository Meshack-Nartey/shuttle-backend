package com.shuttlebackend.entities;

import com.shuttlebackend.persistence.PolylineJsonConverter;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "route", schema = "shuttle_backend_new")
public class Route {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "route_id", nullable = false)
    private Integer id;

    @Size(max = 100)
    @NotNull
    @Column(name = "route_name", nullable = false, length = 100)
    private String routeName;

    @Lob
    @Column(name = "description")
    private String description;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @OneToMany(mappedBy = "route")
    private Set<RouteStop> routeStops = new LinkedHashSet<>();

    // typed JSON representation of forward/backward polylines: list of [lat, lon]
    @Lob
    @Column(name = "polyline_forward", columnDefinition = "JSON")
    @Convert(converter = PolylineJsonConverter.class)
    private List<List<Double>> polylineForward;

    @Lob
    @Column(name = "polyline_backward", columnDefinition = "JSON")
    @Convert(converter = PolylineJsonConverter.class)
    private List<List<Double>> polylineBackward;

}