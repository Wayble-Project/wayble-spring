package com.wayble.server.user.entity;

import com.wayble.server.common.entity.BaseEntity;
import com.wayble.server.review.entity.Review;
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
@Table(
        name = "user",
        uniqueConstraints = @UniqueConstraint(columnNames = {"email", "login_type"})
)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nickname", length = 8)
    private String nickname;

    private String username;

    @Column(nullable = false)
    private String email;

    // TODO: 비밀번호 암호화 필요
    @Column(nullable = false)
    private String password;

    @Column(name = "birth_date", columnDefinition = "DATE")
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(name = "login_type", nullable = false)
    private LoginType loginType;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false)
    private UserType userType; // DISABLED,COMPANION,GENERAL

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @Column(name = "disability_type")
    private String disabilityType; // 장애 유형 (발달장애,시각장애,지체장애,청각장애)

    @Column(name = "mobility_aid")
    private String mobilityAid; // 이동 보조 수단 (안내견,지팡이,동행인,휠체어)

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviewList = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserDisabilityMapping> userDisabilities = new ArrayList<>();


    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserPlace> userPlaces = new ArrayList<>();

    public static User createUser(
            String email,
            String password,
            LoginType loginType
    ) {
        return User.builder()
                .email(email)
                .password(password)
                .loginType(loginType)
                .userType(UserType.GENERAL) // 기본값
                .build();
    }

    public static User createUserWithDetails(
            String name,
            String username,
            String email,
            String password,
            LocalDate birthDate,
            Gender gender,
            LoginType loginType,
            UserType userType
    ) {
        return User.builder()
                .nickname(name)
                .username(username)
                .email(email)
                .password(password)
                .birthDate(birthDate)
                .gender(gender)
                .loginType(loginType)
                .userType(userType) // 기본값
                .build();
    }

    public void updateProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setDisabilityType(String disabilityType) {
        this.disabilityType = disabilityType;
    }
    public void setMobilityAid(String mobilityAid) {
        this.mobilityAid = mobilityAid;
    }
    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }
    public void setGender(Gender gender) {
        this.gender = gender;
    }
    public void setUserType(UserType userType) {
        this.userType = userType;
    }
}
