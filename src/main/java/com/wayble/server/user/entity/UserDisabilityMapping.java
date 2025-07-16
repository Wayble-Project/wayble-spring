package com.wayble.server.user.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_disability_mapping",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "disability_id"}))
public class UserDisabilityMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "disability_id", nullable = false)
    private Disability disability; // 장애 유형
}