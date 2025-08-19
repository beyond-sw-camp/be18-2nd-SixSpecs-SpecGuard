package com.beyond.specguard.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    @GetMapping("/me")
    public ResponseEntity<String> getMyInfo(Authentication authentication) {
        return ResponseEntity.ok("Hello, " + authentication.getName());
    }
}
