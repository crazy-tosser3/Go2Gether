package com.example.STOLOTO.Services;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.STOLOTO.DTO.auth.AuthResponse;
import com.example.STOLOTO.DTO.auth.DemoAccountResponse;
import com.example.STOLOTO.DTO.auth.LoginRequest;
import com.example.STOLOTO.DTO.auth.MeResponse;
import com.example.STOLOTO.DTO.auth.RegisterRequest;
import com.example.STOLOTO.Models.Role;
import com.example.STOLOTO.Models.User;
import com.example.STOLOTO.exception.ApiException;
import com.example.STOLOTO.repository.OrganizationRepository;
import com.example.STOLOTO.repository.UserRepository;
import com.example.STOLOTO.security.CurrentUser;
import com.example.STOLOTO.security.JwtService;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

    private static final Map<String, String> DEMO_DESCRIPTIONS = Map.of(
            "user@demo.local", "Обычный пользователь",
            "user2@demo.local", "Второй пользователь",
            "org@demo.local", "Владелец допущенной организации",
            "org2@demo.local", "Владелец организаций на модерации и отклонённой",
            "admin@demo.local", "Администратор");

    private final UserRepository users;
    private final OrganizationRepository organizations;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final CurrentUser currentUser;
    private final DtoMapper mapper;

    @Transactional(readOnly = true)
    @Cacheable("demo-accounts")
    public List<DemoAccountResponse> demoAccounts() {
        return users.findByDemoTrueOrderByIdAsc().stream()
                .map(u -> new DemoAccountResponse(u.getId(), u.getName(), u.getEmail(), u.getRole(),
                        DEMO_DESCRIPTIONS.getOrDefault(u.getEmail(), "")))
                .toList();
    }

    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (users.existsByEmailIgnoreCase(email)) {
            throw ApiException.conflict("EMAIL_TAKEN", "Пользователь с таким email уже существует");
        }
        User u = new User();
        u.setRole(Role.USER);
        u.setName(request.name().trim());
        u.setEmail(email);
        u.setPasswordHash(passwordEncoder.encode(request.password()));
        u.setCreatedAt(Instant.now());
        u.setUpdatedAt(u.getCreatedAt());
        users.save(u);
        return authResponse(u);
    }

    public AuthResponse login(LoginRequest request) {
        if (request.demoAccountId() != null) {
            User demo = users.findById(request.demoAccountId())
                    .filter(User::isDemo)
                    .filter(User::isActive)
                    .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS",
                            "Демо-аккаунт не найден"));
            return authResponse(demo);
        }
        if (request.email() == null || request.password() == null) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_CONTENT, "VALIDATION_ERROR",
                    "Укажите email и password или demo_account_id",
                    Map.of("email", "обязательно", "password", "обязательно"));
        }
        User u = users.findByEmailIgnoreCase(normalizeEmail(request.email()))
                .filter(User::isActive)
                .filter(x -> passwordEncoder.matches(request.password(), x.getPasswordHash()))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS",
                        "Неверный email или пароль"));
        return authResponse(u);
    }

    public void logout() {
        currentUser.tokenId().ifPresent(jwtService::revoke);
    }

    @Transactional(readOnly = true)
    public MeResponse me() {
        User u = currentUser.require();
        var orgs = organizations.findByOwnerIdOrderByCreatedAtDesc(u.getId()).stream()
                .map(mapper::toOrganizationShort)
                .toList();
        return new MeResponse(mapper.toUser(u), orgs);
    }

    private AuthResponse authResponse(User u) {
        return AuthResponse.bearer(jwtService.issue(u), jwtService.ttlSeconds(), mapper.toUser(u));
    }

    public static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
