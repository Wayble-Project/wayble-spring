package com.wayble.server.direction.entity.transportation;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.lang.Nullable;

import com.wayble.server.direction.entity.transportation.*;

import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "facility")
public class Facility {
    @Id
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

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id")
    private Node node;

}
