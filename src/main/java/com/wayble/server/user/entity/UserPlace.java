package com.wayble.server.user.entity;

import com.wayble.server.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "user_place") // 유져가 저장한 장소
public class UserPlace extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(length = 20) @Builder.Default
    private String color = "GRAY"; // 배지/아이콘 색 (정확히 무슨 색이 있는지 몰라서 일단 자유 문자열 + 기본: 회색)

    @Column(name = "saved_count", nullable = false, columnDefinition = "int default 0")
    @Builder.Default
    private int savedCount = 0; // 리스트에 담긴 웨이블존 수


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public void increaseCount() { this.savedCount++; }
    public void decreaseCount() { if (this.savedCount > 0) this.savedCount--; }

    public void updateTitle(String title) { this.title = title; }
    public void updateColor(String color) { this.color = color; }
}