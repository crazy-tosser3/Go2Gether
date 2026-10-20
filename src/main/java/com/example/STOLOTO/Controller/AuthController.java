package com.example.STOLOTO.Controller;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.STOLOTO.DTO.auth.AuthResponse;
import com.example.STOLOTO.DTO.auth.DemoAccountResponse;
import com.example.STOLOTO.DTO.auth.LoginRequest;
import com.example.STOLOTO.DTO.auth.MeResponse;
import com.example.STOLOTO.DTO.auth.RegisterRequest;
import com.example.STOLOTO.Services.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String COOKIE_NAME = "jwt";

    private final AuthService authService;

    @GetMapping("/demo-accounts")
    public List<DemoAccountResponse> demoAccounts() {
        return authService.demoAccounts();
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse body = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .header(HttpHeaders.SET_COOKIE, buildCookie(body).toString())
                .body(body);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse body = authService.login(request);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, buildCookie(body).toString())
                .body(body);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        authService.logout();
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, clearCookie().toString())
                .build();
    }

   public static ResponseCookie clearCookie() {
        return ResponseCookie.from(COOKIE_NAME, "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("Strict")
                .maxAge(0)
                .build();
    }

    @GetMapping("/me")
    public MeResponse me() {
        return authService.me();
    }

    private ResponseCookie buildCookie(AuthResponse auth) {
        return ResponseCookie.from(COOKIE_NAME, auth.token())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("Strict")
                .maxAge(auth.expiresIn())
                .build();
    }
}