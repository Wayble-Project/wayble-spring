package com.wayble.server.user.service;

import com.wayble.server.common.exception.ApplicationException;
import com.wayble.server.logging.service.UserActionLogService;
import com.wayble.server.user.dto.UserRegisterRequestDto;
import com.wayble.server.user.entity.User;
import com.wayble.server.user.exception.UserErrorCase;
import com.wayble.server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserActionLogService userActionLogService;

    // 회원가입
    public void signup(UserRegisterRequestDto req) {
        if (userRepository.existsByEmailAndLoginType(req.email(), req.loginType())) {
            throw new ApplicationException(UserErrorCase.USER_ALREADY_EXISTS);
        }
        User user = User.createUser(
                req.email(),
                passwordEncoder.encode(req.password()),
                req.loginType()
        );
        User savedUser = userRepository.save(user);
        
        // 회원가입 로그 저장 (비동기)
        userActionLogService.logUserRegister(
                savedUser.getId(), 
                req.loginType().name(), 
                null
        );
    }

    public void makeException() {
        throw new ApplicationException(UserErrorCase.USER_NOT_FOUND);
    }
}