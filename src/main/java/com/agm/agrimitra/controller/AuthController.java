package com.agm.agrimitra.controller;

import com.agm.agrimitra.dto.AuthResponseDto;
import com.agm.agrimitra.dto.LoginRequestDto;
import com.agm.agrimitra.dto.RegisterAdminRequestDto;
import com.agm.agrimitra.dto.RegisterFarmerRequestDto;
import com.agm.agrimitra.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication Controller", description = "Endpoints for user registration and JWT authentication")
public class AuthController {

    private final AuthService authService;

    // TODO: Implement rate limiting (e.g. Bucket4j or Redis-based rate limiter) on auth endpoints to mitigate brute-force attacks

    @PostMapping("/register-farmer")
    @Operation(summary = "Register a new farmer account with linked profile")
    public ResponseEntity<AuthResponseDto> registerFarmer(
            @Valid @RequestBody RegisterFarmerRequestDto requestDto) {
        AuthResponseDto response = authService.registerFarmer(requestDto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/register-admin")
    @Operation(summary = "Register a new admin account (currently open for bootstrap; consider restricting via @PreAuthorize in production)")
    public ResponseEntity<AuthResponseDto> registerAdmin(
            @Valid @RequestBody RegisterAdminRequestDto requestDto) {
        AuthResponseDto response = authService.registerAdmin(requestDto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user credentials and receive JWT token")
    public ResponseEntity<AuthResponseDto> login(
            @Valid @RequestBody LoginRequestDto requestDto) {
        AuthResponseDto response = authService.login(requestDto);
        return ResponseEntity.ok(response);
    }
}
