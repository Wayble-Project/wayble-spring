package com.wayble.server.direction.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.lang.Nullable;

@Entity
@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "facility")
public class Facility {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="stationName")
    private String stationName;

    @Column(name = "lift_latitude", columnDefinition = "DECIMAL(10,7)", nullable = true)
    private Double liftLatitude;

    @Column(name = "lift_longitude", columnDefinition = "DECIMAL(10,7)", nullable = true)
    private Double liftLongitude;

    @Column(name = "lift_exit_Num", nullable = true)
    private Integer liftExitNum;

    @Column(name = "elevator_latitude", columnDefinition = "DECIMAL(10,7)", nullable = true)
    private Double elevatorLatitude;

    @Column(name = "elevator_longitude", columnDefinition = "DECIMAL(10,7)", nullable = true)
    private Double elevatorLongitude;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private Node node;

}
