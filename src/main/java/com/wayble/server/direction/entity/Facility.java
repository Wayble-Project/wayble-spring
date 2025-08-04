package com.wayble.server.direction.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.lang.Nullable;

import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "facility")
public class Facility {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="stationName")
    private String stationName;

    //for toliet
    @Column(name="ln_cd")
    private String lnCd;

    @Column(name="rail_opr_lstt_cd")
    private String railOprLsttCd;

    @Column(name="stin_cd")
    private String stinCd;

    @OneToMany(mappedBy = "facility", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Lift> lifts;

    @OneToMany(mappedBy = "facility", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Elevator> elevators;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private Node node;

}
