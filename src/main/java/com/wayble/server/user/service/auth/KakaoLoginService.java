package com.wayble.server.user.service.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wayble.server.common.config.security.jwt.JwtTokenProvider;
import com.wayble.server.common.exception.ApplicationException;
import com.wayble.server.user.dto.KakaoLoginRequestDto;
import com.wayble.server.user.dto.KakaoLoginResponseDto;
import com.wayble.server.user.dto.KakaoUserInfoDto;
import com.wayble.server.user.entity.LoginType;
import com.wayble.server.user.entity.User;
import com.wayble.server.user.exception.UserErrorCase;
import com.wayble.server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class KakaoLoginService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtProvider;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String KAKAO_USERINFO_URL = "https://kapi.kakao.com/v2/user/me";

    public KakaoLoginResponseDto kakaoLogin(KakaoLoginRequestDto request) {
        // 카카오에서 사용자 정보 조회
        KakaoUserInfoDto kakaoUserInfo = fetchKakaoUserInfo(request.accessToken());

        String email = kakaoUserInfo.getKakao_account().getEmail();
        String nickname = kakaoUserInfo.getKakao_account().getProfile().getNickname();
        String profileImage = kakaoUserInfo.getKakao_account().getProfile().getProfile_image_url();
        Long kakaoId = kakaoUserInfo.getId();

        // 유저 검색 (카카오ID + KAKAO 타입)
        Optional<User> userOpt = userRepository.findByEmailAndLoginType(email, LoginType.KAKAO);
        boolean isNewUser = false;
        User user;

        if (userOpt.isEmpty()) {
            // 신규 가입자라면 회원가입 처리
            user = User.createUser(
                    email,
                    "",  // 소셜로그인은 패스워드 X
                    LoginType.KAKAO
            );
            user.setNickname(nickname);
            user.setProfileImageUrl(profileImage);

            user = userRepository.save(user);
            isNewUser = true;
        } else {
            user = userOpt.get();
        }

        // JWT 토큰 발급
        String accessToken = jwtProvider.generateToken(user.getId(), user.getUserType().name());
        String refreshToken = jwtProvider.generateRefreshToken(user.getId());


        return KakaoLoginResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .isNewUser(isNewUser)
                .user(KakaoLoginResponseDto.UserDto.builder()
                        .id(user.getId())
                        .nickname(user.getNickname())
                        .email(user.getEmail())
                        .build())
                .build();
    }

    // 카카오 사용자 정보 요청
    private KakaoUserInfoDto fetchKakaoUserInfo(String kakaoAccessToken) {
        try {
            String response = WebClient.create()
                    .get()
                    .uri(KAKAO_USERINFO_URL)
                    .header("Authorization", "Bearer " + kakaoAccessToken)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            System.out.println("카카오 응답: " + response);

            return objectMapper.readValue(response, KakaoUserInfoDto.class);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ApplicationException(UserErrorCase.KAKAO_AUTH_FAILED);
        }
    }

}