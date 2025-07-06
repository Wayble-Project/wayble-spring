package com.wayble.server.wayblezone.entity;

import com.wayble.server.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "wayble_zone_image")
@SQLDelete(sql = "UPDATE wayble_zone_image SET deleted_at = now() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class WaybleZoneImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wayble_zone_id", nullable = false)
    private WaybleZone waybleZone;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;
}
