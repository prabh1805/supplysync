package com.supplysync.auth.controller;

import com.supplysync.auth.dto.AuthResponse;
import com.supplysync.auth.dto.LoginRequest;
import com.supplysync.auth.dto.RegisterRequest;
import com.supplysync.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        AuthResponse res = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    // stub for now — will implement proper auth with JWT later
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        AuthResponse res = userService.findByEmail(request.getEmail());
        return ResponseEntity.ok(res);
    }
}
