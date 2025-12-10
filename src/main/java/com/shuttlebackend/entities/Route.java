package com.shuttlebackend.entities;

import com.shuttlebackend.persistence.PolylineJsonConverter;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.ArrayList;
import java.util.List;

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

    @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("stopOrder ASC")
    private List<RouteStop> routeStops = new ArrayList<>();

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