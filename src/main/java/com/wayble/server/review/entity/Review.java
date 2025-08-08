package com.wayble.server.review.entity;

import com.wayble.server.common.entity.BaseEntity;
import com.wayble.server.user.entity.User;
import com.wayble.server.wayblezone.entity.WaybleZone;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE review SET deleted_at = now() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Table(name = "review")
public class Review extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "rating", nullable = false)
    private double rating = 0.0;

    @Column(name = "rating", nullable = false)
    private Integer likeCount = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wayble_zone_id", nullable = false)
    private WaybleZone waybleZone;

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReviewImage> reviewImageList = new ArrayList<>();

    public static Review of(User user, WaybleZone waybleZone, String content, double rating) {
        return Review.builder()
                .user(user)
                .waybleZone(waybleZone)
                .content(content)
                .rating(rating)
                .likeCount(0)
                .build();
    }

    /**
     * TODO: 접근성 정보 관련 구현 필요
     */
}
