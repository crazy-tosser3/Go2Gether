package com.example.STOLOTO.Services;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.STOLOTO.DTO.auth.AuthResponse;
import com.example.STOLOTO.DTO.auth.DemoAccountResponse;
import com.example.STOLOTO.DTO.auth.LoginRequest;
import com.example.STOLOTO.DTO.auth.MeResponse;
import com.example.STOLOTO.DTO.auth.RegisterRequest;
import com.example.STOLOTO.exception.ApiException;
import com.example.STOLOTO.Models.Role;
import com.example.STOLOTO.Models.User;
import com.example.STOLOTO.repository.OrganizationRepository;
import com.example.STOLOTO.repository.UserRepository;
import com.example.STOLOTO.security.CurrentUser;
import com.example.STOLOTO.security.JwtService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    public static final Map<String, String> DEMO_DESCRIPTIONS = Map.of(
            "user", "Обычный пользователь",
            "user2", "Второй пользователь",
            "org_pending", "Владелец организации на модерации",
            "org_approved", "Владелец допущенной организации",
            "admin", "Администратор");
    private static final List<String> DEMO_ORDER = List.of("user", "user2", "org_pending", "org_approved", "admin");

    private final UserRepository users;
    private final OrganizationRepository organizations;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final CurrentUser currentUser;
    private final DtoMapper mapper;

    public List<DemoAccountResponse> demoAccounts() {
        return users.findAll().stream()
                .filter(User::isDemo)
                .sorted(Comparator.comparingInt(u -> DEMO_ORDER.indexOf(u.getId())))
                .map(u -> new DemoAccountResponse(u.getId(), u.getName(), u.getEmail(), u.getRole(),
                        DEMO_DESCRIPTIONS.getOrDefault(u.getId(), "")))
                .toList();
    }

    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (users.findByEmail(email).isPresent()) {
            throw ApiException.conflict("EMAIL_TAKEN", "Пользователь с таким email уже существует");
        }
        User u = new User();
        u.setId(UUID.randomUUID().toString());
        u.setRole(Role.USER);
        u.setName(request.name().trim());
        u.setEmail(email);
        u.setPasswordHash(passwordEncoder.encode(request.password()));
        u.setCreatedAt(Instant.now());
        users.save(u);
        return authResponse(u);
    }

    public AuthResponse login(LoginRequest request) {
        if (request.demoAccountId() != null && !request.demoAccountId().isBlank()) {
            User demo = users.findById(request.demoAccountId())
                    .filter(User::isDemo)
                    .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS",
                            "Демо-аккаунт не найден"));
            return authResponse(demo);
        }
        if (request.email() == null || request.password() == null) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_CONTENT, "VALIDATION_ERROR",
                    "Укажите email и password или demo_account_id",
                    Map.of("email", "обязательно", "password", "обязательно"));
        }
        User u = users.findByEmail(normalizeEmail(request.email()))
                .filter(x -> passwordEncoder.matches(request.password(), x.getPasswordHash()))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS",
                        "Неверный email или пароль"));
        return authResponse(u);
    }

    public void logout() {
        currentUser.tokenId().ifPresent(jwtService::revoke);
    }

    public MeResponse me() {
        User u = currentUser.require();
        var org = organizations.findByOwnerId(u.getId()).stream().findFirst()
                .map(mapper::toOrganizationShort)
                .orElse(null);
        return new MeResponse(mapper.toUser(u), org);
    }

    private AuthResponse authResponse(User u) {
        return AuthResponse.bearer(jwtService.issue(u), jwtService.ttlSeconds(), mapper.toUser(u));
    }

    public static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
