package com.wayble.server.user.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "disability")
public class Disability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "disability_id")
    private Long id;

    @Column(name = "name", nullable = false, unique = true)
    private String name; // ex: 발달장애,시각장애,지체장애,청각장애

    @OneToMany(mappedBy = "disability", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserDisabilityMapping> userDisabilityMappings = new ArrayList<>();
}