package com.wayble.server.auth.service;

import com.wayble.server.common.config.security.jwt.JwtTokenProvider;
import com.wayble.server.common.exception.ApplicationException;
import com.wayble.server.logging.service.UserActionLogService;
import com.wayble.server.user.dto.UserLoginRequestDto;
import com.wayble.server.auth.dto.TokenResponseDto;
import com.wayble.server.auth.entity.RefreshToken;
import com.wayble.server.user.entity.User;
import com.wayble.server.user.exception.UserErrorCase;
import com.wayble.server.auth.repository.RefreshTokenRepository;
import com.wayble.server.user.repository.UserRepository;
import jakarta.transaction.Transactional;
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
    private final UserActionLogService userActionLogService;

    public TokenResponseDto login(UserLoginRequestDto req) {
        User user = userRepository.findByEmailAndLoginType(req.email(), req.loginType())
                .orElseThrow(() -> new ApplicationException(UserErrorCase.INVALID_CREDENTIALS));
        if (!encoder.matches(req.password(), user.getPassword())) {
            throw new ApplicationException(UserErrorCase.INVALID_CREDENTIALS);
        }
        String accessToken = jwtProvider.generateToken(user.getId(),user.getUserType() != null ? user.getUserType().name() : null);
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

        if (saved.getExpiry() < System.currentTimeMillis()) {
            refreshTokenRepository.delete(saved);
            throw new ApplicationException(UserErrorCase.INVALID_CREDENTIALS);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(UserErrorCase.USER_NOT_FOUND));

        String newRefreshToken = jwtProvider.generateRefreshToken(userId);
        Long newExpiry = jwtProvider.getTokenExpiry(newRefreshToken);

        saved.setToken(newRefreshToken);
        saved.setExpiry(newExpiry);
        refreshTokenRepository.save(saved);

        String newAccessToken = jwtProvider.generateToken(userId, user.getUserType() != null ? user.getUserType().name() : null);
        
        // 토큰 갱신 로그 저장 (비동기, 하루 1회만)
        userActionLogService.logTokenRefresh(
                userId, 
                user.getUserType() != null ? user.getUserType().name() : null
        );
        
        return new TokenResponseDto(newAccessToken, newRefreshToken);
    }

    @Transactional
    public void logout(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }
}
