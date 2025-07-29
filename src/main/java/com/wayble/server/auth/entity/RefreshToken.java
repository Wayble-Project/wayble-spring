package com.wayble.server.auth.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "refresh_token")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true) // 1인 1토큰
    private Long userId;

    @Column(nullable = false)
    private String token;

    @Column(nullable = false)
    private Long expiry; // 만료 시간

    public void setToken(String token) {
        this.token = token;
    }

    public void setExpiry(Long expiry) {
        this.expiry = expiry;
    }
}