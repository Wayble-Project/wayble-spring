package com.wayble.server.common.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
public class RootController {
    @GetMapping("/")
    public ResponseEntity<Void> redirectToSwagger() {
        return ResponseEntity.status(HttpStatus.FOUND) // 302 Redirect
                .location(URI.create("/swagger-ui/index.html")) // 이동할 주소
                .build();
    }
}