package com.wayble.server.direction.service;

import com.wayble.server.common.exception.ApplicationException;
import com.wayble.server.direction.exception.DirectionErrorCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DirectionService {

    public void makeException() {
        throw new ApplicationException(DirectionErrorCase.PATH_NOT_FOUND);
    }
}
