package com.wayble.server.user.service;

import com.wayble.server.common.exception.ApplicationException;
import com.wayble.server.user.exception.UserErrorCase;
import com.wayble.server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public void makeException() {
        throw new ApplicationException(UserErrorCase.USER_NOT_FOUND);
    }
}
