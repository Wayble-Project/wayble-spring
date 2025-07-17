package com.wayble.server.user.entity;

import com.wayble.server.common.entity.BaseEntity;
import com.wayble.server.review.entity.Review;
import com.wayble.server.user.dto.UserRegisterDto;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE user SET deleted_at = now() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Table(name = "user")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nickname", length = 8, nullable = false)
    private String nickname;

    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    // TODO: 비밀번호 암호화 필요
    @Column(nullable = false)
    private String password;

    @Column(name = "birth_date", columnDefinition = "DATE")
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(name = "login_type", nullable = false)
    private LoginType loginType;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false)
    private UserType userType;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviewList = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserDisabilityMapping> userDisabilities = new ArrayList<>();


    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserPlace> userPlaces = new ArrayList<>();

    public static User createUser(
            String nickname,
            String username,
            String email,
            String password,
            LocalDate birthDate,
            Gender gender,
            LoginType loginType,
            UserType userType
    ) {
        return User.builder()
                .nickname(nickname)
                .username(username)
                .email(email)
                .password(password)
                .birthDate(birthDate)
                .gender(gender)
                .loginType(loginType)
                .userType(userType)
                .build();
    }

    public User from(UserRegisterDto dto) {
        return User.builder()
                .id(dto.userId() != null ? dto.userId() : this.id)
                .nickname(dto.nickname())
                .username(dto.username())
                .email(dto.email())
                .password(dto.password())
                .birthDate(dto.birthDate())
                .gender(dto.gender())
                .build();
    }
}
