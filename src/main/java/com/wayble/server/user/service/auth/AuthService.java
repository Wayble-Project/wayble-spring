package com.wayble.server.user.service.auth;

import com.wayble.server.common.config.security.jwt.JwtTokenProvider;
import com.wayble.server.common.exception.ApplicationException;
import com.wayble.server.user.dto.UserLoginRequestDto;
import com.wayble.server.user.dto.token.TokenResponseDto;
import com.wayble.server.user.entity.User;
import com.wayble.server.user.exception.UserErrorCase;
import com.wayble.server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final JwtTokenProvider jwtProvider;

    public TokenResponseDto login(UserLoginRequestDto req) {
        User user = userRepository.findByEmailAndLoginType(req.email(), req.loginType())
                .orElseThrow(() -> new ApplicationException(UserErrorCase.INVALID_CREDENTIALS));
        if (!encoder.matches(req.password(), user.getPassword())) {
            throw new ApplicationException(UserErrorCase.INVALID_CREDENTIALS);
        }
        String token = jwtProvider.generateToken(user.getId(), user.getUserType().name());
        return new TokenResponseDto(token);
    }
}
