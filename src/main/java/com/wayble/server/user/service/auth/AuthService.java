package com.wayble.server.user.service.auth;

import com.wayble.server.common.config.security.jwt.JwtTokenProvider;
import com.wayble.server.common.exception.ApplicationException;
import com.wayble.server.user.dto.UserLoginRequestDto;
import com.wayble.server.user.dto.token.TokenResponseDto;
import com.wayble.server.user.entity.RefreshToken;
import com.wayble.server.user.entity.User;
import com.wayble.server.user.exception.UserErrorCase;
import com.wayble.server.user.repository.RefreshTokenRepository;
import com.wayble.server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder encoder;
    private final JwtTokenProvider jwtProvider;

    public TokenResponseDto login(UserLoginRequestDto req) {
        User user = userRepository.findByEmailAndLoginType(req.email(), req.loginType())
                .orElseThrow(() -> new ApplicationException(UserErrorCase.INVALID_CREDENTIALS));
        if (!encoder.matches(req.password(), user.getPassword())) {
            throw new ApplicationException(UserErrorCase.INVALID_CREDENTIALS);
        }
        String accessToken = jwtProvider.generateToken(user.getId(), user.getUserType().name());
        String refreshToken = jwtProvider.generateRefreshToken(user.getId());
        Long expiry = jwtProvider.getTokenExpiry(refreshToken);

        refreshTokenRepository.deleteByUserId(user.getId());

        RefreshToken entity = RefreshToken.builder()
                .userId(user.getId())
                .token(refreshToken)
                .expiry(expiry)
                .build();
        refreshTokenRepository.save(entity);

        return new TokenResponseDto(accessToken, refreshToken);
    }

    public TokenResponseDto reissue(String refreshToken) {
        if (!jwtProvider.validateToken(refreshToken)) {
            throw new ApplicationException(UserErrorCase.INVALID_CREDENTIALS);
        }
        Long userId = jwtProvider.getUserId(refreshToken);
        RefreshToken saved = refreshTokenRepository.findByUserId(userId)
                .orElseThrow(() -> new ApplicationException(UserErrorCase.INVALID_CREDENTIALS));

        if (!saved.getToken().equals(refreshToken)) {
            throw new ApplicationException(UserErrorCase.INVALID_CREDENTIALS);
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(UserErrorCase.USER_NOT_FOUND));
        String newAccessToken = jwtProvider.generateToken(userId, user.getUserType().name());

        return new TokenResponseDto(newAccessToken, refreshToken);
    }

    public void logout(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }
}
